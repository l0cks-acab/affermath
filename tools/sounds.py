#!/usr/bin/env python3
"""Synthesises every Affermath sound (wasteland ambience and mob voices) and writes
sounds.json. Run from the repo root: python3 tools/sounds.py   (needs numpy and ffmpeg)

Everything is built from noise, glottal pulses, formant filters, damped modes and
FFT reverb, so there are no recorded samples in the repo."""
import json
import os
import subprocess
import tempfile
import wave

import numpy as np

SR = 44100
OUT = "src/main/resources/assets/affermath/sounds"
NS = "affermath"


# ------------------------------------------------------------------ building blocks
def t(sec):
    return np.arange(int(sec * SR)) / SR


def noise(sec, rng):
    return rng.standard_normal(int(sec * SR))


def fft_filter(x, gain):
    """Circular FFT filter: gain(freqs) -> per-bin multiplier. Loops stay seamless."""
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    return np.fft.irfft(X * gain(f), len(x))


def band(lo, hi, soft=1.0):
    def g(f):
        return 1 / (1 + (lo / np.maximum(f, 1)) ** (2 * soft)) / (1 + (f / hi) ** (2 * soft))
    return g


def formants(fs):
    """fs: [(centre, bandwidth, gain)] -> resonant peaks like a vocal tract."""
    def g(f):
        out = np.zeros_like(f)
        for c, bw, k in fs:
            out += k / (1 + ((f - c) / (bw / 2)) ** 2)
        return out
    return g


def pink(sec, rng):
    return fft_filter(noise(sec, rng), lambda f: 1 / np.sqrt(np.maximum(f, 20)))


def glottal(f0, rng, jitter=0.01, shimmer=0.1):
    """Sawtooth-like pulse train following the f0 curve (array, Hz) with jitter and shimmer."""
    j = 1 + jitter * smooth_rand(len(f0), 30, rng)
    phase = np.cumsum(f0 * j / SR)
    saw = 2 * (phase % 1) - 1
    pulse = saw - np.roll(saw, 1) * 0.6
    return pulse * (1 + shimmer * smooth_rand(len(f0), 40, rng))


def smooth_rand(n, rate, rng):
    """Smooth random curve in about [-1,1], about `rate` wiggles per second."""
    k = max(2, int(n / SR * rate) + 2)
    pts = rng.uniform(-1, 1, k)
    return np.interp(np.linspace(0, k - 1, n), np.arange(k), pts)


def env(n, attack, release, hold=None, curve=2.0):
    e = np.ones(n)
    a = max(1, int(attack * SR))
    r = max(1, int(release * SR))
    e[:a] = np.linspace(0, 1, a) ** 0.7
    if hold is None:
        e[-r:] *= np.linspace(1, 0, r) ** curve
    else:
        h = int(hold * SR)
        e[a + h:a + h + r] *= np.linspace(1, 0, min(r, n - a - h)) ** curve
        e[a + h + r:] = 0
    return e


def decay(n, t60):
    return np.exp(-6.9 * np.arange(n) / (t60 * SR))


def reverb(x, t60, mix, rng, pre=0.02):
    n = int((t60 + 0.1) * SR)
    ir = rng.standard_normal(n) * decay(n, t60)
    ir = fft_filter(ir, band(80, 5000))
    ir[: int(pre * SR)] = 0
    ir /= np.sqrt(np.sum(ir ** 2))
    y = np.fft.irfft(np.fft.rfft(np.pad(x, (0, n))) * np.fft.rfft(np.pad(ir, (0, len(x)))), len(x) + n)
    return np.pad(x, (0, n)) * (1 - mix) + y * mix


def modes(sec, freqs, t60s, rng, strike=None):
    """Damped inharmonic modes (metal, struts). strike: excitation signal, else one impulse."""
    n = int(sec * SR)
    tt = np.arange(n) / SR
    out = np.zeros(n)
    for f, t60 in zip(freqs, t60s):
        out += np.sin(2 * np.pi * f * tt + rng.uniform(0, 6.28)) * np.exp(-6.9 * tt / t60)
    if strike is not None:
        out = np.fft.irfft(np.fft.rfft(np.pad(strike, (0, n))) * np.fft.rfft(np.pad(out, (0, len(strike)))), n + len(strike))[:n]
    return out


def norm(x, peak=0.89):
    m = np.max(np.abs(x)) or 1
    return x / m * peak


def trim(x, thresh=0.002):
    idx = np.where(np.abs(x) > thresh)[0]
    if len(idx) == 0:
        return x
    end = min(len(x), idx[-1] + int(0.05 * SR))
    y = x[:end].copy()
    f = min(len(y), int(0.03 * SR))
    y[-f:] *= np.linspace(1, 0, f)
    return y


def write(path, x, peak=0.89, keep=False):
    x = norm(x, peak) if not keep else np.clip(x, -1, 1)
    full = f"{OUT}/{path}.ogg"
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        with wave.open(tmp.name, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(SR)
            w.writeframes((x * 32767).astype("<i2").tobytes())
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", tmp.name, "-map_metadata", "-1",
                        "-c:a", "libvorbis", "-q:a", "4", full], check=True)
    os.unlink(tmp.name)


# ------------------------------------------------------------------ ambience
def wind_loop(rng, sec=24.0):
    """Seamless howling wind: pink noise through drifting resonant bands, with gusts.
    Every LFO does a whole number of cycles over the loop, and filtering is circular."""
    n = int(sec * SR)
    tt = np.arange(n) / SR
    base = fft_filter(pink(sec, rng), band(60, 900))
    gust = 0.55 + 0.25 * np.sin(2 * np.pi * 2 * tt / sec + 1) + 0.2 * np.sin(2 * np.pi * 5 * tt / sec)
    out = base * gust
    for c, cyc, amt in ((420, 3, 0.5), (660, 4, 0.35), (980, 7, 0.2)):
        whistle = fft_filter(noise(sec, rng), formants([(c, 40, 1.0)]))
        lfo = np.clip(np.sin(2 * np.pi * cyc * tt / sec + rng.uniform(0, 6.28)), 0, 1) ** 2
        out += norm(whistle, 1) * lfo * amt * gust
    return out


def mood(rng):
    """Low uneasy drone with a far-off groan of twisting steel."""
    sec = 7.0
    tt = t(sec)
    drone = sum(np.sin(2 * np.pi * f * tt) * a for f, a in ((41, 1), (43.3, 0.8), (82.5, 0.4), (61.7, 0.3)))
    drone += fft_filter(noise(sec, rng), band(30, 160)) * 0.6
    groan_f = 180 * (1 - 0.25 * tt / sec) * (1 + 0.03 * smooth_rand(len(tt), 3, rng))
    groan = fft_filter(glottal(groan_f, rng, 0.03, 0.4), formants([(360, 90, 1), (870, 140, 0.6), (1500, 200, 0.3)]))
    x = drone * env(len(tt), 2.0, 3.5) + norm(groan, 0.5) * env(len(tt), 1.5, 2.5) * 0.6
    return trim(reverb(x, 3.5, 0.5, rng))


def shell(rng, dist=1.0):
    """Distant artillery: a dull thump, a rolling rumble, the echo off far hills."""
    sec = 4.5
    n = int(sec * SR)
    thump = fft_filter(noise(sec, rng), band(20, 180 / dist)) * decay(n, 0.7)
    crack = fft_filter(noise(sec, rng), band(200, 1200 / dist)) * decay(n, 0.12) * 0.25
    rumble = fft_filter(noise(sec, rng), band(25, 110)) * env(n, 0.15, 3.5) * (0.5 + 0.5 * np.abs(smooth_rand(n, 12, rng)))
    x = thump + crack + rumble * 0.7
    echo = np.zeros_like(x)
    d = int(rng.uniform(0.6, 1.1) * SR)
    echo[d:] = x[:-d] * 0.35
    return trim(reverb(fft_filter(x + echo, band(20, 900)), 2.8, 0.45, rng))


def creak(rng):
    """Wind-loaded steel: stick-slip clicks ringing a girder's inharmonic modes."""
    sec = 2.6
    n = int(sec * SR)
    rate = 18 + 40 * (0.5 + 0.5 * smooth_rand(n, 2, rng))
    phase = np.cumsum(rate / SR)
    clicks = np.diff(np.floor(phase), prepend=0) * rng.uniform(0.4, 1.0, n)
    clicks *= env(n, 0.35, 0.8, curve=1.2)
    base = rng.uniform(170, 260)
    fr = [base * k for k in (1, 2.63, 4.12, 6.9, 9.3)]
    ring = modes(0.5, fr, [0.4, 0.25, 0.18, 0.1, 0.07], rng, strike=clicks)
    ring = fft_filter(ring, band(120, 2500))
    return trim(reverb(ring, 2.2, 0.4, rng))


def gunfire(rng):
    """A far-off burst of small-arms fire, mostly reverb."""
    sec = 3.5
    n = int(sec * SR)
    x = np.zeros(n)
    tpos = rng.uniform(0.05, 0.2)
    for _ in range(rng.integers(4, 9)):
        i = int(tpos * SR)
        m = int(0.08 * SR)
        x[i:i + m] += fft_filter(noise(0.08, rng), band(150, 1500))[: len(x[i:i + m])] * decay(m, 0.05)[: len(x[i:i + m])] * rng.uniform(0.6, 1)
        tpos += rng.uniform(0.07, 0.16)
    return trim(reverb(x, 2.5, 0.65, rng, pre=0.05))


# ------------------------------------------------------------------ voices
def voice(rng, sec, f0a, f0b, vowel, breath=0.25, rasp=0.3, attack=0.05, release=0.3, wobble=0.04, jitter=0.02):
    tt = t(sec)
    f0 = np.geomspace(f0a, f0b, len(tt)) * (1 + wobble * smooth_rand(len(tt), 6, rng))
    src = glottal(f0, rng, jitter, 0.3)
    # rasp: amplitude-modulate with a sub-harmonic, like a damaged throat
    src *= 1 + rasp * np.sign(np.sin(np.pi * np.cumsum(f0) / SR))
    voiced = fft_filter(src, formants(vowel))
    br = fft_filter(noise(sec, rng), formants([(c, bw * 2, g) for c, bw, g in vowel]))
    x = norm(voiced, 1) * (1 - breath) + norm(br, 1) * breath
    return x * env(len(tt), attack, release)


UH = [(520, 110, 1.0), (920, 130, 0.6), (2400, 220, 0.25)]
OH = [(430, 100, 1.0), (760, 120, 0.7), (2500, 240, 0.2)]
AH = [(750, 130, 1.0), (1200, 150, 0.6), (2600, 250, 0.25)]
AA = [(1100, 220, 1.0), (1900, 300, 0.7), (3100, 400, 0.3)]


def soldier_ambient(rng):
    a = voice(rng, rng.uniform(1.1, 1.6), rng.uniform(92, 110), rng.uniform(70, 82), UH, breath=0.35, rasp=0.45, attack=0.25, release=0.6)
    return trim(reverb(a, 0.9, 0.25, rng))


def soldier_hurt(rng):
    a = voice(rng, 0.38, rng.uniform(150, 175), rng.uniform(105, 120), AH, breath=0.3, rasp=0.5, attack=0.01, release=0.2)
    return trim(reverb(a, 0.6, 0.2, rng))


def soldier_death(rng):
    a = voice(rng, 1.9, 125, 55, OH, breath=0.45, rasp=0.6, attack=0.03, release=1.2, wobble=0.07)
    rattle = fft_filter(noise(1.9, rng), band(300, 1500)) * (np.abs(smooth_rand(len(a), 25, rng)) > 0.6) * env(len(a), 0.6, 1.0) * 0.2
    return trim(reverb(a + rattle, 1.0, 0.3, rng))


def wraith_whisper(rng, sec, f_hi, f_lo, loud=1.0):
    tt = t(sec)
    vowel = [AH, OH, UH][rng.integers(0, 3)]
    br = fft_filter(noise(sec, rng), formants([(c, bw * 2.5, g) for c, bw, g in vowel] + [(5200, 2000, 0.5)]))
    br *= 0.6 + 0.4 * np.abs(smooth_rand(len(tt), 9, rng))
    f = np.geomspace(f_hi, f_lo, len(tt)) * (1 + 0.02 * np.sin(2 * np.pi * 5.5 * tt))
    tone = np.sin(2 * np.pi * np.cumsum(f) / SR) + 0.3 * np.sin(2 * np.pi * np.cumsum(f * 1.5) / SR)
    return (norm(br, 1) * 0.7 + tone * 0.3 * loud) * env(len(tt), sec * 0.3, sec * 0.5)


def wraith_ambient(rng):
    return trim(reverb(wraith_whisper(rng, rng.uniform(1.6, 2.2), rng.uniform(760, 900), rng.uniform(600, 680), 0.6), 2.5, 0.55, rng))


def wraith_hurt(rng):
    tt = t(0.5)
    shriek = np.sin(2 * np.pi * np.cumsum(np.geomspace(1500, 900, len(tt))) / SR) * env(len(tt), 0.005, 0.4)
    hiss = fft_filter(noise(0.5, rng), band(2500, 9000)) * decay(len(tt), 0.25)
    return trim(reverb(norm(shriek, 1) * 0.6 + norm(hiss, 1) * 0.5, 1.6, 0.5, rng))


def wraith_death(rng):
    a = wraith_whisper(rng, 2.6, 1100, 260, 1.4)
    return trim(reverb(a, 3.0, 0.6, rng))


def caw(rng, sec, f0a, f0b, n=1, gap=0.12):
    parts = []
    for i in range(n):
        tt = t(sec)
        f0 = np.geomspace(f0a, f0b, len(tt)) * (1 + 0.05 * smooth_rand(len(tt), 20, rng))
        src = glottal(f0, rng, 0.04, 0.5)
        src *= 1 + 0.7 * np.sign(np.sin(np.pi * np.cumsum(f0) / SR))
        v = fft_filter(src, formants(AA)) + fft_filter(noise(sec, rng), formants(AA)) * 0.4
        parts.append(norm(v, 1) * env(len(tt), 0.01, sec * 0.5, curve=1.5))
        parts.append(np.zeros(int(gap * SR)))
        f0a *= 0.95
        f0b *= 0.95
    return np.concatenate(parts)


def crow_ambient(rng):
    return trim(reverb(caw(rng, rng.uniform(0.2, 0.28), rng.uniform(560, 640), rng.uniform(430, 480), int(rng.integers(2, 4))), 1.8, 0.35, rng))


def crow_hurt(rng):
    return trim(reverb(caw(rng, 0.16, 820, 640, 1), 1.0, 0.25, rng))


def crow_death(rng):
    return trim(reverb(caw(rng, 0.7, 700, 300, 1), 1.6, 0.35, rng))


def boss_growl(rng, sec, fa, fb, release):
    a = voice(rng, sec, fa, fb, OH, breath=0.3, rasp=0.9, attack=0.2, release=release, wobble=0.06, jitter=0.04)
    sub = np.sin(2 * np.pi * np.cumsum(np.geomspace(fa / 2, fb / 2, len(a))) / SR) * env(len(a), 0.2, release)
    return norm(a, 1) + 0.5 * sub


def boss_ambient(rng):
    a = boss_growl(rng, rng.uniform(1.6, 2.2), rng.uniform(58, 66), rng.uniform(44, 50), 0.9)
    return trim(reverb(a, 2.4, 0.4, rng))


def boss_hurt(rng):
    base = rng.uniform(140, 200)
    clang = modes(1.4, [base * k for k in (1, 2.76, 5.4, 8.9, 13.3)], [1.0, 0.7, 0.4, 0.25, 0.15], rng)
    grunt = boss_growl(rng, 0.5, 95, 70, 0.3)
    n = max(len(clang), len(grunt))
    x = np.pad(norm(clang, 1), (0, n - len(clang))) * 0.6 + np.pad(grunt, (0, n - len(grunt))) * 0.6
    return trim(reverb(x, 1.8, 0.35, rng))


def boss_death(rng):
    roar = boss_growl(rng, 3.2, 90, 32, 2.0)
    rumble = fft_filter(noise(3.2, rng), band(20, 120)) * env(len(roar), 0.4, 2.4)
    return trim(reverb(norm(roar, 1) + norm(rumble, 1) * 0.6, 3.5, 0.45, rng))


# ------------------------------------------------------------------ catalogue
def main():
    rng = np.random.default_rng(1945)
    sounds = {}

    def event(name, files, subtitle=None, stream=False, **extra):
        entry = {"sounds": [dict({"name": f"{NS}:{f}"}, **({"stream": True} if stream else {}), **extra) for f in files]}
        if subtitle:
            entry["subtitle"] = subtitle
        sounds[name] = entry

    write("ambient/wasteland_wind", wind_loop(rng), peak=0.5)
    event("ambient.wasteland.loop", ["ambient/wasteland_wind"], stream=True)
    for i in range(3):
        write(f"ambient/mood{i + 1}", mood(rng), peak=0.6)
    event("ambient.wasteland.mood", [f"ambient/mood{i + 1}" for i in range(3)])

    adds = []
    for i in range(4):
        write(f"ambient/shell{i + 1}", shell(rng, dist=1 + i * 0.4), peak=0.7)
        adds.append(f"ambient/shell{i + 1}")
    for i in range(3):
        write(f"ambient/creak{i + 1}", creak(rng), peak=0.45)
        adds.append(f"ambient/creak{i + 1}")
    for i in range(2):
        write(f"ambient/gunfire{i + 1}", gunfire(rng), peak=0.4)
        adds.append(f"ambient/gunfire{i + 1}")
    event("ambient.wasteland.additions", adds, subtitle=f"subtitles.{NS}.ambient.wasteland.additions")

    mobs = {
        "hollow_soldier": (soldier_ambient, soldier_hurt, soldier_death),
        "ash_wraith": (wraith_ambient, wraith_hurt, wraith_death),
        "cinder_crow": (crow_ambient, crow_hurt, crow_death),
        "cratomancer": (boss_ambient, boss_hurt, boss_death),
    }
    for mob, fns in mobs.items():
        for kind, fn, count in zip(("ambient", "hurt", "death"), fns, (3, 2, 1)):
            files = []
            for i in range(count):
                write(f"entity/{mob}/{kind}{i + 1}", fn(rng))
                files.append(f"entity/{mob}/{kind}{i + 1}")
            event(f"entity.{mob}.{kind}", files, subtitle=f"subtitles.{NS}.entity.{mob}.{kind}")

    with open(f"{OUT}/../sounds.json", "w") as f:
        json.dump(sounds, f, indent=2)
        f.write("\n")
    print("ok", len(sounds), "events")


if __name__ == "__main__":
    main()
