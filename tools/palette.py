"""Hue-shifted colour ramps and small pixel helpers shared by the texture generators.

Every material gets one ramp of 7 colours, index 0 darkest to 6 lightest, 3 is the base.
Shadows drift towards cool blue-violet, highlights towards warm yellow, the way vanilla
deepslate, basalt and netherite do, instead of just getting darker or lighter."""
import colorsys
import math


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def _mix_hue(h, target, t):
    d = ((target - h + 0.5) % 1.0) - 0.5
    return (h + d * t) % 1.0


COOL = (52, 56, 92)
WARM = (255, 244, 214)


def ramp(base, n=7, step=0.17, cool=0.22, warm=0.18, **_):
    """Build an n-colour ramp around base (hex string). Index n//2 is the base colour.
    Darker steps scale value down and lean towards a cool blue-violet; lighter steps
    lift towards a warm off-white. Blending in RGB keeps greys from drifting green."""
    r, g, b, _a = hexc(base)
    mid = n // 2
    out = []
    for i in range(n):
        k = i - mid
        if k < 0:
            f = max(0.05, 1 + step * k * 1.2)
            t = cool * (-k / mid)
            c = [v * f for v in (r, g, b)]
            lum = sum(c) / 3
            tint = [lum * (cv / 70) for cv in COOL]
            c = [cv * (1 - t) + tv * t for cv, tv in zip(c, tint)]
        elif k > 0:
            t = step * 1.15 * k
            c = [v + (255 - v) * t for v in (r, g, b)]
            lum = sum(c) / 3
            tw = warm * (k / mid)
            tint = [lum * (wv / 238) for wv in WARM]
            c = [cv * (1 - tw) + tv * tw for cv, tv in zip(c, tint)]
        else:
            c = [r, g, b]
        out.append(tuple(max(0, min(255, round(v))) for v in c) + (255,))
    return out


def hash2(x, y, seed):
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0


def value_noise(w, h, cell, seed, wrap=True):
    """Smooth tileable value noise in [0,1], w x h, lattice spacing `cell`."""
    gw, gh = max(1, w // cell), max(1, h // cell)

    def lat(ix, iy):
        if wrap:
            ix, iy = ix % gw, iy % gh
        return hash2(ix, iy, seed)

    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            fx, fy = x / cell, y / cell
            ix, iy = int(math.floor(fx)), int(math.floor(fy))
            tx, ty = fx - ix, fy - iy
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = lat(ix, iy) + (lat(ix + 1, iy) - lat(ix, iy)) * tx
            b = lat(ix, iy + 1) + (lat(ix + 1, iy + 1) - lat(ix, iy + 1)) * tx
            out[y][x] = a + (b - a) * ty
    return out


def fbm(w, h, seed, cells=(8, 4, 2), weights=(0.55, 0.3, 0.15)):
    layers = [value_noise(w, h, c, seed + i * 31) for i, c in enumerate(cells)]
    return [[sum(l[y][x] * wt for l, wt in zip(layers, weights)) for x in range(w)] for y in range(h)]


def quant(v, levels):
    """Map v in [0,1] to an index from a list of thresholds -> ramp indices."""
    for thr, idx in levels:
        if v < thr:
            return idx
    return levels[-1][1]


def count_colours(img):
    return len({p for p in img.getdata() if p[3] > 0})
