#!/usr/bin/env python3
"""Generates every Affermath texture (blocks, items, entities, armour, particles).
Run from the repo root: python3 tools/textures.py   (needs pillow)"""
import os, random, math
from PIL import Image

T = "src/main/resources/assets/affermath/textures"


def save(img, rel):
    p = f"{T}/{rel}.png"
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)


def jitter(c, r, amt):
    d = r.randint(-amt, amt)
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)


def noisy(base, amt, seed, size=16):
    r = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            img.putpixel((x, y), jitter(base, r, amt))
    return img, r


# ------------------------------------------------------------------ entity painting
def box(img, u, v, w, h, d, fn):
    """Paint a model cube's UV net. fn(face, x, y, fw, fh) -> colour or None."""
    faces = {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
             "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}
    for name, (fx, fy, fw, fh) in faces.items():
        for y in range(fh):
            for x in range(fw):
                c = fn(name, x, y, fw, fh)
                if c is not None:
                    img.putpixel((fx + x, fy + y), c)


def armor(layer2=False, mat="warforged"):
    """Warforged plate on the vanilla humanoid armour layout: riveted plates, lit top
    edges, dark seams and ember vents, all from one gunmetal ramp."""
    from palette import ramp
    from rigs import EMBER
    M = ramp("3F4148", step=0.22)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def metal(face, x, y, w, h):
        if face == "top":
            return M[4]
        if face == "bottom":
            return M[1]
        if y == 0:
            return M[5]
        if y == h - 1:
            return M[1]
        if face in ("front", "back") and h >= 8 and y % 4 == 3:
            return M[1]  # plate seams
        if face in ("front", "back") and y % 4 == 0 and x % 3 == 1:
            return M[5]  # rivets
        if x == 0:
            return M[4]
        return M[3] if (x + y // 4) % 5 else M[2]

    if not layer2:
        def helm(face, x, y, w, h):
            if face == "front" and y == 3 and 1 <= x <= 6:
                return EMBER[5] if x in (2, 5) else EMBER[2]
            if face == "front" and y in (2, 4) and 1 <= x <= 6:
                return M[0]
            if face == "bottom":
                return None
            return metal(face, x, y, w, h)

        def chest(face, x, y, w, h):
            if face == "front" and y in (3, 4) and x in (3, 4):
                return EMBER[5] if y == 3 else EMBER[3]
            return metal(face, x, y, w, h)
        box(img, 0, 0, 8, 8, 8, helm)
        box(img, 16, 16, 8, 12, 4, chest)
        box(img, 40, 16, 4, 12, 4, metal)
        box(img, 0, 16, 4, 12, 4, lambda face, x, y, w, h: metal(face, x, y, w, h) if y >= 7 else None)
    else:
        box(img, 16, 16, 8, 12, 4, lambda face, x, y, w, h: metal(face, x, y, w, h) if y >= 6 else None)
        box(img, 0, 16, 4, 12, 4, lambda face, x, y, w, h: metal(face, x, y, w, h) if y < 9 else None)
    return img


def gas_mask_layer():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    r = random.Random(40)
    rub = hexc("3B3A35")

    def mask(face, x, y, w, h):
        if face == "front":
            if y in (2, 3) and x in (1, 2, 5, 6):
                return hexc("BFD8E0") if y == 2 else hexc("7E98A0")
            if y >= 5 and 2 <= x <= 5:
                return hexc("55534C") if (x + y) % 2 else hexc("2E2D29")
            if y >= 1:
                return jitter(rub, r, 4)
            return None
        if face in ("right", "left") and y in (2, 3):
            return hexc("26251F")
        if face == "back" and y in (2, 3):
            return hexc("26251F")
        return None
    box(img, 32, 0, 8, 8, 8, mask)
    return img


# ------------------------------------------------------------------ particles
def particles():
    r = random.Random(50)
    for i in range(3):
        img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
        pts = [(3, 3), (4, 3), (3, 4), (4, 4)]
        for _ in range(2 + i):
            x, y = r.choice(pts)
            pts.append((max(1, min(6, x + r.choice([-1, 1]))), max(1, min(6, y + r.choice([-1, 1])))))
        for (x, y) in pts:
            g = r.randint(215, 255)
            img.putpixel((x, y), (g, g, g, 255))
        save(img, f"particle/ash_flake_{i}")
    for i in range(2):
        img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
        x = 3 + i
        for y in range(1, 7):
            a = 255 if 2 <= y <= 5 else 150
            img.putpixel((x, y), (230, 230, 230, a))
        save(img, f"particle/ash_streak_{i}")


def logo():
    """256x256 pack icon: ruined church tower in grey fog with falling ash."""
    r = random.Random(60)
    img = Image.new("RGBA", (256, 256))
    for y in range(256):
        for x in range(256):
            g = int(150 - y * 0.18)
            img.putpixel((x, y), (g, g, g - 2, 255))
    dark = (46, 44, 42, 255)
    # ground
    for x in range(256):
        h = 200 + int(6 * math.sin(x / 23)) + r.randint(0, 2)
        for y in range(h, 256):
            img.putpixel((x, y), (60, 58, 55, 255))
    # tower with jagged top and window holes
    for x in range(70, 118):
        top = 60 + (abs(x - 94) // 3) * 2 + r.randint(0, 10)
        for y in range(top, 205):
            img.putpixel((x, y), dark)
    for (wx, wy) in [(84, 90), (100, 90), (84, 130), (100, 130)]:
        for y in range(wy, wy + 22):
            for x in range(wx, wx + 6):
                img.putpixel((x, y), (120, 119, 117, 255))
    # broken wall and dead tree
    for x in range(118, 200):
        top = 150 + r.randint(0, 25) + (x - 118) // 4
        for y in range(top, 205):
            img.putpixel((x, y), (54, 52, 50, 255))
    for y in range(110, 205):
        for x in range(212, 217):
            img.putpixel((x, y), (30, 29, 28, 255))
    for i in range(30):
        img.putpixel((217 + i, 140 - i // 2), (30, 29, 28, 255))
        img.putpixel((211 - i // 2, 125 - i), (30, 29, 28, 255))
    # ash
    for _ in range(500):
        x, y = r.randrange(256), r.randrange(256)
        c = r.randint(170, 230)
        img.putpixel((x, y), (c, c, c, 255))
        if r.random() < 0.3 and y < 254:
            img.putpixel((x, y + 1), (30, 30, 30, 255))
    # ember eye
    for y in range(-3, 4):
        for x in range(-3, 4):
            if x * x + y * y <= 9:
                img.putpixel((94 + x, 75 + y), (255, 140 - (x * x + y * y) * 8, 50, 255))
    return img


EGGS = {
    "hollow_soldier": (0x4A4A44, 0x7A6E5A),
    "ash_wraith": (0xB8B8B4, 0x5E5E5C),
    "cinder_crow": (0x1C1C1E, 0xC2551E),
    "cratomancer": (0x2B2B2B, 0xE0782A),
}


def spawn_egg(base, spot):
    """Egg silhouette in the mob's two colours (26.1 dropped tinted eggs)."""
    from tex_items import render
    rows = [
        "......kkkk......",
        ".....kBBBBk.....",
        "....kBBSBBBk....",
        "...kBBBBBBBBk...",
        "...kBSBBBBSBk...",
        "..kBBBBBBBBBBk..",
        "..kBBBBSBBBBBk..",
        "..kBBBBBBBBSBk..",
        "..kBSBBBBBBBBk..",
        "..kBBBBBSBBBBk..",
        "..kBBBBBBBBBBk..",
        "...kBBSBBBBBk...",
        "...kBBBBBSBBk...",
        "....kBBBBBBk....",
        ".....kkkkkk.....",
        "................",
    ]
    def rgb(c):
        return ((c >> 16) & 255, (c >> 8) & 255, c & 255, 255)
    from palette import ramp
    hx = lambda c: "%06X" % c
    rb, rs = ramp(hx(base)), ramp(hx(spot))
    return render("egg", rows, {"k": (0, 0, 0, 255), "B": rb[3], "S": rs[3]}, ramps=[rb, rs])


def main():
    import tex_blocks, tex_items, rigs
    mc26 = os.path.isdir("src/main/resources/assets/affermath/items")
    rigs.HAT_PARENT = "head" if mc26 else None
    for name, fn in tex_blocks.ALL.items():
        save(fn(), f"block/{name}")
    save(tex_blocks.portal(), "block/affermath_portal")
    with open(f"{T}/block/affermath_portal.png.mcmeta", "w") as f:
        f.write('{"animation": {"frametime": 2}}\n')
    for name, img in tex_items.all_items().items():
        save(img, f"item/{name}")
    for make in rigs.RIGS:
        rig = make()
        img, glow = rig.paint()
        save(img, f"entity/{rigs.TEXTURE[rig.name]}")
        save(glow, f"entity/{rigs.TEXTURE[rig.name]}_glow")
    with open("src/main/java/com/lach/affermath/client/RigLayers.java", "w") as f:
        f.write(rigs.java_source("com.lach.affermath.client"))
    if mc26:
        save(armor(), "entity/equipment/humanoid/warforged")
        save(armor(True), "entity/equipment/humanoid_leggings/warforged")
        save(gas_mask_layer(), "entity/equipment/humanoid/gas_mask")
        for e, (base, spot) in EGGS.items():
            save(spawn_egg(base, spot), f"item/{e}_spawn_egg")
    else:
        save(armor(), "models/armor/warforged_layer_1")
        save(armor(True), "models/armor/warforged_layer_2")
        save(gas_mask_layer(), "models/armor/gas_mask_layer_1")
    particles()
    logo().save("src/main/resources/logo.png")
    print("ok")


if __name__ == "__main__":
    import sys
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    main()
