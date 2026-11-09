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


# ------------------------------------------------------------------ blocks
def ash_block():
    img, r = noisy(hexc("8C8B88"), 9, 1)
    for _ in range(26):
        x, y = r.randrange(16), r.randrange(16)
        img.putpixel((x, y), shade(hexc("8C8B88"), r.choice([0.72, 0.8, 1.12, 1.18])))
    return img


def ashen_soil():
    img, r = noisy(hexc("5C5955"), 8, 2)
    for _ in range(14):
        x, y = r.randrange(15), r.randrange(15)
        c = shade(hexc("6E6A64"), r.uniform(0.9, 1.2))
        img.putpixel((x, y), c)
        img.putpixel((x + 1, y), shade(c, 0.8))
    for _ in range(10):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("3E3C39"))
    return img


def rubble():
    r = random.Random(3)
    img = Image.new("RGBA", (16, 16), hexc("3A3836"))
    cols = [hexc("6B6763"), hexc("7C7671"), hexc("5A5550"), hexc("8A8580"), hexc("6E5F55")]
    for _ in range(14):
        cx, cy, rad = r.randrange(16), r.randrange(16), r.randint(2, 4)
        c = r.choice(cols)
        for y in range(cy - rad, cy + rad + 1):
            for x in range(cx - rad, cx + rad + 1):
                if abs(x - cx) + abs(y - cy) <= rad:
                    xx, yy = x % 16, y % 16
                    edge = abs(x - cx) + abs(y - cy) == rad
                    img.putpixel((xx, yy), shade(c, 0.7 if edge else (1.15 if y < cy else 1.0)))
    return img


def bricks(seed, cracked=False):
    r = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    mortar = hexc("2E2C2A")
    for y in range(16):
        row = y // 4
        off = 4 if row % 2 else 0
        for x in range(16):
            if y % 4 == 3 or (x + off) % 8 == 7:
                img.putpixel((x, y), jitter(mortar, r, 4))
            else:
                bi = ((x + off) // 8) + row * 3
                base = [hexc("6A5D55"), hexc("5E534C"), hexc("736659"), hexc("554B45")][bi % 4]
                soot = 0.65 + 0.35 * (y % 4) / 3 if (bi * 7) % 3 == 0 else 1.0
                img.putpixel((x, y), shade(jitter(base, r, 7), soot))
    if cracked:
        crack(img, r, 3)
    return img


def crack(img, r, n):
    for _ in range(n):
        x, y = r.randrange(16), r.randrange(16)
        for _ in range(r.randint(5, 9)):
            img.putpixel((x % 16, y % 16), hexc("1E1D1C"))
            x += r.choice([-1, 0, 1])
            y += r.choice([0, 1, 1])


def concrete(seed, cracked=False):
    img, r = noisy(hexc("8C8B87"), 5, seed)
    for _ in range(4):  # water/soot stains
        cx, cy = r.randrange(16), r.randrange(16)
        for y in range(cy, min(16, cy + r.randint(3, 8))):
            img.putpixel((cx, y), shade(img.getpixel((cx, y)), 0.82))
    for x in range(16):
        img.putpixel((x, 0), shade(img.getpixel((x, 0)), 0.85))
    for y in range(16):
        img.putpixel((0, y), shade(img.getpixel((0, y)), 0.88))
    for _ in range(3):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("6E6D69"))
    if cracked:
        crack(img, r, 4)
        for _ in range(2):  # exposed aggregate
            x, y = r.randrange(14), r.randrange(14)
            for dx in range(2):
                for dy in range(2):
                    img.putpixel((x + dx, y + dy), hexc("5C5A57"))
    return img


def charred_log():
    r = random.Random(7)
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        base = hexc("1E1C1B") if x % 4 in (0, 3) else hexc("2B2826")
        for y in range(16):
            c = jitter(base, r, 5)
            if r.random() < 0.06:
                c = hexc("454140")
            img.putpixel((x, y), c)
    for _ in range(5):  # alligator cracks
        y = r.randrange(16)
        for x in range(r.randrange(10), 16):
            if r.random() < 0.6:
                img.putpixel((x, y), hexc("100F0E"))
    return img


def charred_log_top():
    img = Image.new("RGBA", (16, 16))
    r = random.Random(8)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 7:
                c = hexc("1A1817")
            else:
                ring = int(d) % 2
                c = hexc("3A3532") if ring else hexc("2E2A28")
                if d < 1.5:
                    c = hexc("4A4440")
            img.putpixel((x, y), jitter(c, r, 4))
    return img


def charred_planks():
    r = random.Random(9)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                c = hexc("121110")
            else:
                c = jitter(hexc("2F2B28") if (y // 4) % 2 else hexc("27231F"), r, 5)
                if (x + (y // 4) * 5) % 16 == 0:
                    c = hexc("161413")
            img.putpixel((x, y), c)
    return img


def rebar():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rust = [hexc("6B3E26"), hexc("7D4A2C"), hexc("53311F"), hexc("8A5634")]
    r = random.Random(10)
    for x0 in (1, 7, 13):
        for y in range(16):
            for dx in range(2):
                c = r.choice(rust)
                if (y + x0) % 3 == 0:
                    c = shade(c, 1.25)
                img.putpixel((x0 + dx, y), c if dx == 0 else shade(c, 0.75))
    for y0 in (3, 12):
        for x in range(16):
            if img.getpixel((x, y0))[3] == 0:
                img.putpixel((x, y0), shade(r.choice(rust), 0.85))
    return img


def shattered_glass():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    frame = hexc("4A4846")
    for i in range(16):
        for p in [(i, 0), (i, 15), (0, i), (15, i)]:
            img.putpixel(p, frame)
    r = random.Random(11)
    shard = (200, 205, 205, 120)
    for (x0, y0) in [(1, 1), (10, 1), (1, 11), (12, 12)]:
        for y in range(4):
            for x in range(4 - y):
                img.putpixel((min(14, x0 + x), min(14, y0 + y)), shard)
    for _ in range(4):
        x, y = r.randrange(2, 14), r.randrange(2, 14)
        for _ in range(6):
            if 0 < x < 15 and 0 < y < 15:
                img.putpixel((x, y), (230, 232, 232, 200))
            x += r.choice([-1, 1])
            y += r.choice([-1, 0, 1])
    return img


def sandbags(top=False):
    r = random.Random(12 + top)
    img = Image.new("RGBA", (16, 16))
    base = hexc("8B8170")
    for y in range(16):
        row = y // 5 if not top else y // 8
        off = 3 if row % 2 else 0
        for x in range(16):
            h = 5 if not top else 8
            edge = y % h in (0, h - 1) or (x + off) % 8 in (0,)
            c = jitter(base, r, 6)
            if edge:
                c = shade(c, 0.6)
            elif y % h == 1:
                c = shade(c, 1.12)
            img.putpixel((x, y), shade(c, 0.85))
    return img


def portal():
    """Churning smoke with ash and embers drifting down; tiles vertically so it loops."""
    frames = 32
    r = random.Random(14)
    img = Image.new("RGBA", (16, 16 * frames))
    # fixed smoke field, 16 wide x 32 tall, scrolled down one pixel per frame
    field = [[0.0] * 16 for _ in range(32)]
    for _ in range(40):
        cx, cy, rad = r.uniform(0, 16), r.uniform(0, 32), r.uniform(2, 5)
        for y in range(32):
            for x in range(16):
                dx = min(abs(x - cx), 16 - abs(x - cx))
                dy = min(abs(y - cy), 32 - abs(y - cy))
                field[y][x] += max(0.0, 1 - math.hypot(dx, dy) / rad) * 0.35
    specks = [(r.randrange(16), r.randrange(32), r.random() < 0.25) for _ in range(14)]
    for f in range(frames):
        for y in range(16):
            for x in range(16):
                v = min(1.0, field[(y - f) % 32][x])
                g = int(60 + 120 * v)
                img.putpixel((x, y + f * 16), (g, g, g - 4, 200))
        for (sx, sy, ember) in specks:
            yy = (sy + f * 2) % 32
            if yy < 16:
                c = (255, 150, 60, 230) if ember else (215, 215, 212, 230)
                img.putpixel((sx, yy + f * 16), c)
    return img


# ------------------------------------------------------------------ items (ascii art)
def art(rows, pal):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != "." and ch != " ":
                img.putpixel((x, y), pal[ch])
    return img


STEEL = {"k": hexc("1A1A1C"), "d": hexc("33343A"), "m": hexc("4C4E56"), "l": hexc("6E717B"), "h": hexc("9A9EA8"),
         "e": hexc("E0782A"), "E": hexc("FFB25C"), "r": hexc("8A3A1A")}

ITEMS = {
    "ash": (["................", "................", "................", "................", "................",
             "................", "................", "......hh........", ".....hllh.......", "....hllmlh......",
             "...hlmllmmh.....", "..hlmmlmmmlh....", "..lmmdmmmdmml...", ".lmmdmmdmmmdml..", ".dmmmdddmmmmmd..",
             "................"],
            {"h": hexc("C4C4C0"), "l": hexc("A6A6A2"), "m": hexc("8A8A86"), "d": hexc("6A6A66")}),
    "spectral_ash": (["................", "........w.......", "......w.........", "...........w....", ".....w..........",
                      "........w.......", "......hh....w...", ".....hllh.......", "....hllclh......", "...hlcllcch.....",
                      "..hlcclcclch....", "..lccdcccdccl...", ".lccdccdcccdcl..", ".dcccdddccccd...", "................",
                      "................"],
                     {"w": hexc("E8F4FF"), "h": hexc("E2EEF6"), "l": hexc("BCD0DC"), "c": hexc("94AAB8"), "d": hexc("6C8090")}),
    "warforged_plating": (["................", "................", "................", "...kkkkkkkkkk...", "..kllhhhhhhllk..",
                           "..klhmmmmmmhlk..", "..klm.eeee.mlk..", "..klmlEEEElmlk..", "..klm.eeee.mlk..", "..klmmmmmmmmlk..",
                           "..kmdmmmmmmdmk..", "..kdddddddddd k.", "...kkkkkkkkkk...", "................", "................",
                           "................"], {**STEEL, ".": None}),
    "warforged_upgrade_smithing_template": (["................", "..kkkkkkkkkkk...", "..kdddddddddk...", "..kdmmmmmmmdk...",
                                             "..kdm.eee.mdk...", "..kdmeEEEemdk...", "..kdm.eee.mdk...", "..kdmmmmmmmdk...",
                                             "..kdmlhhhlmdk...", "..kdmllllmmdk...", "..kdmmmmmmmdk...", "..kdddddddddk...",
                                             "..kkkkkkkkkkk...", "................", "................", "................"],
                                            {**STEEL, ".": None}),
    "ash_igniter": (["................", "................", "..........f.....", ".........fFf....", "........fFf.....",
                     "........ff......", ".......kmk......", "......kmhmk.....", ".....kmhmk......", "....kmhmk.......",
                     "...kddmk........", "..kddk..........", ".kddk...........", ".kdk............", "..k.............",
                     "................"],
                    {**STEEL, "f": hexc("FF9A3C"), "F": hexc("FFE08A")}),
    "eye_of_the_cratomancer": (["................", "................", "................", "......kkkk......", "....kkmlllkk....",
                                "...kmllhhllmk...", "..kmlhhrrhhlmk..", "..kllhreEerhllk.", "..kllhrEEErhllk.", "..kmlhhrrhhlmk..",
                                "...kmllhhllmk...", "....kkmmmmkk....", "......kkkk......", "................", "................",
                                "................"],
                               {**STEEL, "l": hexc("8E8E8C"), "h": hexc("B8B8B4"), "m": hexc("5E5E5C")}),
    "dog_tag": (["................", "....c...........", "...c.c..........", "...c..c.........", "....c..c........",
                 "........c.......", ".......kkkk.....", "......khhhhk....", "......khllhk....", "......khlllk....",
                 "......khlllk....", "......khlllk....", "......khlllk....", "......kmmmmk....", ".......kkkk.....",
                 "................"],
                {"c": hexc("8A8C90"), "k": hexc("3A3B3E"), "h": hexc("C9CCD2"), "l": hexc("A9ACB3"), "m": hexc("82858C")}),
    "dog_tags": (["................", "..cccccccccc....", ".c..........c...", ".c..........c...", "..c........c....",
                  "...c......c.....", "....c....kkkk...", ".....kkkkhhhhk..", "....khhhhkllhk..", "....khlllkllhk..",
                  "....khlllklllk..", "....khlllklllk..", "....khlllkmmmk..", "....kmmmmkkkk...", ".....kkkk.......",
                  "................"],
                 {"c": hexc("8A8C90"), "k": hexc("3A3B3E"), "h": hexc("C9CCD2"), "l": hexc("A9ACB3"), "m": hexc("82858C")}),
    "shrapnel_charm": (["................", ".......ss.......", "......s..s......", "......s..s......", ".......ss.......",
                        ".......k........", "......kmk.......", ".....kmhlk......", "...kkmhlmk.k....", "..kmhlmmlkkhk...",
                        "...kmlmmmlhmk...", "....kmlmdmlk....", ".....kdmdk......", "......kdk.......", ".......k........",
                        "................"],
                       {**STEEL, "s": hexc("6B5A44")}),
    "field_lantern": (["................", "......kkkk......", ".....k....k.....", "......kkkk......", ".....kmmmmk.....",
                       "....kmhhhhmk....", "....kmEEEemk....", "....kmEEEemk....", "....kmeEeemk....", "....kmeeeemk....",
                       "....kmhhhhmk....", "....kddddddk....", ".....kkkkkk.....", "................", "................",
                       "................"],
                      {**STEEL, "m": hexc("4A5038"), "d": hexc("343826"), "h": hexc("6A7250")}),
    "ration_tin": (["................", "................", "................", "....kkkkkkkk....", "...khhhhhhhhk...",
                    "...klllllllllk..", "...kmmmmmmmmk...", "...kgggggggg k..", "...kgGGGGGGgk...", "...kgGkkkkGgk...",
                    "...kgGGGGGGgk...", "...kggggggggk...", "...kmmmmmmmmk...", "....kkkkkkkk....", "................",
                    "................"],
                   {**STEEL, "g": hexc("4F5A3A"), "G": hexc("67744C")}),
    "gas_mask": (["................", "................", "....kkkkkkkk....", "...kddddddddk...", "..kdmmmmmmmmdk..",
                  "..kdkkkmmkkkdk..", "..kdkEekkeEkdk..", "..kdkeekkeekdk..", "..kdmkkmmkkmdk..", "...kdmmkkmmdk...",
                  "....kdkllkdk....", ".....kkhhkk.....", ".....kmllmk.....", ".....kmmmmk.....", "......kkkk......",
                  "................"],
                 {**STEEL, "d": hexc("3B3A35"), "m": hexc("55534C"), "E": hexc("BFD8E0"), "e": hexc("7E98A0")}),
    "warforged_helmet": (["................", "................", "................", "....kkkkkkkk....", "...kmhhhhhhmk...",
                          "..kmhllllllhmk..", "..kmlmmmmmmlmk..", "..kmlkkkkkkkmk..", "..kmlkeeeeekmk..", "..kmlk....kmmk..",
                          "..kmk......kmk..", "..kk........kk..", "................", "................", "................",
                          "................"], {**STEEL, ".": None}),
    "warforged_chestplate": (["................", "..kkk......kkk..", ".kmlmkkkkkkmlmk.", ".kmllmmhhmmllmk.", ".kmlmmmhhmmmlmk.",
                              "..kkmmmeemmmkk..", "...kmmeEEemmk...", "...kmmmeemmmk...", "...kmlmmmmlmk...", "...kmmlmmlmmk...",
                              "...kmmmmmmmmk...", "...kdmmmmmmdk...", "...kddddddddk...", "....kkkkkkkk....", "................",
                              "................"], {**STEEL, ".": None}),
    "warforged_leggings": (["................", "...kkkkkkkkkk...", "...kmhhhhhhmk...", "...kmmmeemmmk...", "...kmmk..kmmk...",
                            "...kmlk..klmk...", "...kmlk..klmk...", "...kmmk..kmmk...", "...kmek..kemk...", "...kmlk..klmk...",
                            "...kmlk..klmk...", "...kmmk..kmmk...", "...kddk..kddk...", "...kkkk..kkkk...", "................",
                            "................"], {**STEEL, ".": None}),
    "warforged_boots": (["................", "................", "................", "................", "................",
                         "...kkkk..kkkk...", "...kmlk..klmk...", "...kmlk..klmk...", "...kmek..kemk...", "...kmlk..klmk...",
                         "..kmmlk..klmmk..", ".kmmmmk..kmmmmk.", ".kddddk..kddddk.", ".kkkkkk..kkkkkk.", "................",
                         "................"], {**STEEL, ".": None}),
    "warforged_sword": (["................", ".............kk.", "............khk.", "...........khlk.", "..........khlk..",
                         ".........khlk...", "........khlk....", ".......khlk.....", "..k...khlk......", "..kek.klk.......",
                         "...kEkkk........", "....kdk.........", "...kdkek........", "..kdk..k........", ".kek............",
                         ".kk............."], {**STEEL, ".": None}),
    "warforged_pickaxe": (["................", "....kkkkkkk.....", "...khhllllmk....", "..khlkkkkkllk...", "..kk.....kkhlk..",
                           "........kdkkhk..", ".......kdk..kk..", "......kdk.......", ".....kek........", "....kdk.........",
                           "...kdk..........", "..kdk...........", ".kek............", ".kk.............", "................",
                           "................"], {**STEEL, ".": None}),
    "warforged_axe": (["................", "......kkk.......", ".....khhlk......", "....khllmmk.....", "....khlmmkdk....",
                       ".....kkmkdk.....", "......kkdk......", ".....kek........", "....kdk.........", "...kdk..........",
                       "..kdk...........", ".kek............", ".kk.............", "................", "................",
                       "................"], {**STEEL, ".": None}),
    "warforged_shovel": (["................", "...........kkk..", "..........khhlk.", ".........khllmk.", "..........klmk..",
                          ".........kdkk...", "........kdk.....", ".......kek......", "......kdk.......", ".....kdk........",
                          "....kdk.........", "...kdk..........", "..kek...........", "..kk............", "................",
                          "................"], {**STEEL, ".": None}),
    "warforged_hoe": (["................", "......kkkkk.....", ".....khhllmk....", "......kkkkdk....", ".........kdk....",
                       "........kdk.....", ".......kek......", "......kdk.......", ".....kdk........", "....kdk.........",
                       "...kdk..........", "..kek...........", "..kk............", "................", "................",
                       "................"], {**STEEL, ".": None}),
}


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


def hollow_soldier():
    r = random.Random(20)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    skin, uni, belt, boot = hexc("3B3633"), hexc("4E4C41"), hexc("2A2723"), hexc("221F1C")

    def head(face, x, y, w, h):
        c = jitter(skin, r, 6)
        if face == "front":
            if y in (3, 4) and x in (1, 2, 5, 6):
                return hexc("E06A22") if y == 4 and x in (2, 5) else hexc("0E0D0C")
            if y == 6 and 2 <= x <= 5:
                return hexc("161413")
        if face == "top" and r.random() < 0.2:
            return hexc("55504B")
        return c

    def helmet(face, x, y, w, h):
        if face == "bottom":
            return None
        if face in ("front", "right", "left", "back") and y >= 3:
            return None
        return jitter(hexc("4A4F3E"), r, 6) if r.random() > 0.08 else hexc("2A2D23")

    def body(face, x, y, w, h):
        if y in (8, 9):
            return jitter(belt, r, 4)
        c = jitter(uni, r, 6)
        if face == "front" and x in (3, 4) and y < 8 and y % 3 == 1:
            return hexc("6A6555")
        if r.random() < 0.1:
            return hexc("2B2925")
        return c

    def arm(face, x, y, w, h):
        if y >= 9:
            return jitter(skin, r, 6)
        return jitter(uni, r, 6) if r.random() > 0.08 else hexc("2B2925")

    def leg(face, x, y, w, h):
        if y >= 8:
            return jitter(boot, r, 4)
        return jitter(shade(uni, 0.85), r, 6)

    box(img, 0, 0, 8, 8, 8, head)
    box(img, 32, 0, 8, 8, 8, helmet)
    box(img, 16, 16, 8, 12, 4, body)
    box(img, 40, 16, 4, 12, 4, arm)
    box(img, 0, 16, 4, 12, 4, leg)
    return img


def ash_wraith():
    r = random.Random(21)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    pale = hexc("B9B8B3")

    def head(face, x, y, w, h):
        if face == "front" and y in (3, 4, 5) and x in (1, 2, 5, 6):
            return hexc("101010")
        if face == "front" and y == 7 and 2 <= x <= 5:
            return hexc("2A2A2A")
        return jitter(pale, r, 10)

    def hood(face, x, y, w, h):
        if face == "bottom" or (face == "front" and y >= 2):
            return None
        return jitter(hexc("7D7C78"), r, 8)

    def robe(face, x, y, w, h):
        c = jitter(hexc("8E8D88"), r, 12)
        if y > h - 3 and r.random() < 0.5:
            return None
        return c

    box(img, 0, 0, 8, 8, 8, head)
    box(img, 32, 0, 8, 8, 8, hood)
    box(img, 16, 16, 8, 12, 4, robe)
    box(img, 40, 16, 4, 12, 4, lambda f, x, y, w, h: jitter(pale, r, 10) if y > 8 else jitter(hexc("8E8D88"), r, 10))
    box(img, 0, 16, 4, 12, 4, robe)
    box(img, 0, 32, 4, 10, 4, robe)
    return img


def cratomancer():
    r = random.Random(22)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    coat, mask, rub = hexc("2A2826"), hexc("3D3B36"), hexc("1B1A19")

    def head(face, x, y, w, h):
        if face == "front":
            if y in (2, 3, 4) and x in (1, 2) or y in (2, 3, 4) and x in (5, 6):
                return hexc("FF9A3C") if y == 3 else hexc("C2551E")
            if y in (2, 3, 4) and x in (0, 3, 4, 7):
                return hexc("111111")
            if y >= 5 and 2 <= x <= 5:
                return hexc("5C5A52") if (x + y) % 2 else hexc("47453F")
        return jitter(mask, r, 5)

    def cap(face, x, y, w, h):
        if face == "bottom":
            return None
        if face != "top" and y >= 3:
            return None
        if face == "front" and y == 2 and x in (3, 4):
            return hexc("C2551E")
        return jitter(hexc("1F1E1C"), r, 4)

    def body(face, x, y, w, h):
        if y == 7:
            return hexc("3A2C20")
        if face == "front" and x in (3, 4):
            return hexc("1A1918") if y % 3 else hexc("8A8C90")
        if face == "front" and y in (1, 2) and x in (1, 6):
            return hexc("C2551E")
        return jitter(coat, r, 5)

    def arm(face, x, y, w, h):
        if y >= 10:
            return jitter(rub, r, 3)
        if y == 2:
            return hexc("C2551E") if face in ("right", "left", "front", "back") and x % 2 == 0 else jitter(coat, r, 5)
        return jitter(coat, r, 5)

    def leg(face, x, y, w, h):
        if y >= 9:
            return jitter(rub, r, 3)
        return jitter(shade(coat, 1.1), r, 5)

    box(img, 0, 0, 8, 8, 8, head)
    box(img, 32, 0, 8, 8, 8, cap)
    box(img, 16, 16, 8, 12, 4, body)
    box(img, 40, 16, 4, 12, 4, arm)
    box(img, 0, 16, 4, 12, 4, leg)
    return img


def cinder_crow():
    r = random.Random(23)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    black = hexc("1C1C1E")
    f = lambda face, x, y, w, h: jitter(black, r, 6) if r.random() > 0.08 else hexc("C2551E")
    box(img, 0, 0, 4, 4, 6, f)            # body
    box(img, 0, 10, 3, 3, 3, lambda face, x, y, w, h: hexc("E07A2A") if face == "front" and y == 1 and x in (0, 2) else jitter(black, r, 5))
    box(img, 12, 10, 1, 1, 2, lambda *a: hexc("3A3836"))
    box(img, 20, 0, 1, 4, 5, f)
    box(img, 20, 9, 1, 4, 5, f)
    box(img, 0, 16, 3, 1, 4, f)
    box(img, 14, 16, 1, 3, 1, lambda *a: hexc("2E2C2A"))
    return img


def armor(layer2=False, mat="warforged"):
    r = random.Random(30 + layer2)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    plate, dark, ember = hexc("3E4048"), hexc("23242A"), hexc("E0782A")

    def metal(face, x, y, w, h):
        c = jitter(plate, r, 6)
        if y == 0 or x == 0:
            c = shade(c, 1.3)
        if (x + y) % 5 == 0 and r.random() < 0.3:
            c = dark
        return c

    if not layer2:
        def helm(face, x, y, w, h):
            if face == "front" and y in (3, 4) and 1 <= x <= 6:
                return ember if y == 3 and x in (2, 5) else hexc("0E0E10")
            if face == "bottom":
                return None
            return metal(face, x, y, w, h)
        box(img, 0, 0, 8, 8, 8, helm)
        box(img, 16, 16, 8, 12, 4, lambda face, x, y, w, h: ember if face == "front" and y == 3 and x in (3, 4) else metal(face, x, y, w, h))
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


def main():
    save(ash_block(), "block/ash_block")
    save(ashen_soil(), "block/ashen_soil")
    save(rubble(), "block/rubble")
    save(bricks(4), "block/scorched_bricks")
    save(bricks(4, True), "block/cracked_scorched_bricks")
    save(concrete(5), "block/scorched_concrete")
    save(concrete(5, True), "block/cracked_scorched_concrete")
    save(charred_log(), "block/charred_log")
    save(charred_log_top(), "block/charred_log_top")
    save(charred_planks(), "block/charred_planks")
    save(rebar(), "block/rebar")
    save(shattered_glass(), "block/shattered_glass")
    save(sandbags(), "block/sandbags")
    save(sandbags(True), "block/sandbags_top")
    save(portal(), "block/affermath_portal")
    with open(f"{T}/block/affermath_portal.png.mcmeta", "w") as f:
        f.write('{"animation": {"frametime": 2}}\n')
    for name, (rows, pal) in ITEMS.items():
        pal = {k: v for k, v in pal.items() if v is not None}
        rows = [row.ljust(16, ".")[:16] for row in rows]
        save(art(rows, pal), f"item/{name}")
    save(hollow_soldier(), "entity/hollow_soldier")
    save(ash_wraith(), "entity/ash_wraith")
    save(cratomancer(), "entity/cratomancer")
    save(cinder_crow(), "entity/cinder_crow")
    save(armor(), "models/armor/warforged_layer_1")
    save(armor(True), "models/armor/warforged_layer_2")
    save(gas_mask_layer(), "models/armor/gas_mask_layer_1")
    particles()
    logo().save("src/main/resources/logo.png")
    print("ok")


if __name__ == "__main__":
    main()
