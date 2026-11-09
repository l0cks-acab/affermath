"""Block textures: shapes first (bricks, planks, stones, cracks), each lit from the top-left,
coloured from one small hue-shifted ramp per material (4-8 colours like vanilla)."""
import math
import random
from PIL import Image

from palette import hexc, ramp, fbm, hash2, quant

ASH = ramp("8E8C88", step=0.11)
SOIL = ramp("5E5A55", step=0.16)
STONE = ramp("6E6A66", step=0.17)
BRICK = ramp("6B5C53", step=0.16)
CONCRETE = ramp("8A8984", step=0.12)
CHAR = ramp("2C2927", step=0.24)
RUST = ramp("7A4A2E", step=0.2)
CANVAS = ramp("7F7563", step=0.15)
EMBER = [hexc(h) for h in ("5A1E0E", "7E2C10", "A8401A", "D0601F", "EE8A2E", "FFB24A", "FFE08A")]


def blank(c=(0, 0, 0, 0), w=16, h=16):
    return Image.new("RGBA", (w, h), c)


def px(img, x, y, c):
    img.putpixel((x % img.width, y % img.height), c)


# ------------------------------------------------------------------ loose materials
def ash_block():
    """Fine settled ash: soft drifts of three greys, a few dark cinders and pale flakes."""
    n = fbm(16, 16, 101, cells=(8, 4), weights=(0.6, 0.4))
    img = blank()
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), ASH[quant(n[y][x], [(0.36, 2), (0.6, 3), (1.1, 4)])])
    r = random.Random(1)
    for _ in range(7):  # cinders: dark speck with a lit pixel above-left
        x, y = r.randrange(16), r.randrange(16)
        px(img, x, y, ASH[1])
        if r.random() < 0.5:
            px(img, x + 1, y, ASH[2])
    for _ in range(6):
        px(img, r.randrange(16), r.randrange(16), ASH[5])
    return img


def ashen_soil():
    """Dirt-like base with small two-tone clods (lit top-left, shadowed bottom-right)."""
    n = fbm(16, 16, 202, cells=(4, 2), weights=(0.7, 0.3))
    img = blank()
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), SOIL[quant(n[y][x], [(0.4, 2), (0.66, 3), (1.1, 3)])])
    r = random.Random(2)
    for _ in range(9):
        x, y = r.randrange(16), r.randrange(16)
        px(img, x, y, SOIL[4])
        px(img, x + 1, y, SOIL[4] if r.random() < 0.5 else SOIL[3])
        px(img, x, y + 1, SOIL[1])
        px(img, x + 1, y + 1, SOIL[1])
    for _ in range(5):  # ash dusting
        px(img, r.randrange(16), r.randrange(16), ASH[3])
    return img


def rubble():
    """Packed broken stone and brick: irregular chunks, lit top-left edges, dark gaps."""
    img = blank(STONE[0])
    r = random.Random(33)
    pieces = [(r.uniform(0, 16), r.uniform(0, 16)) for _ in range(11)]
    stretch = [(r.uniform(0.7, 1.4), r.uniform(0.7, 1.4)) for _ in pieces]
    owner = {}
    for y in range(16):
        for x in range(16):
            ds = []
            for i, (cx, cy) in enumerate(pieces):
                dx = min(abs(x + 0.5 - cx), 16 - abs(x + 0.5 - cx)) * stretch[i][0]
                dy = min(abs(y + 0.5 - cy), 16 - abs(y + 0.5 - cy)) * stretch[i][1]
                ds.append((max(dx, dy) * 0.5 + math.hypot(dx, dy) * 0.5, i))
            ds.sort()
            owner[(x, y)] = (ds[0][1], ds[1][0] - ds[0][0])
    mats = [(BRICK if i % 4 == 1 else (CONCRETE if i % 4 == 2 else STONE), 3 + (i % 3 == 0) - (i % 5 == 0)) for i in range(len(pieces))]
    for y in range(16):
        for x in range(16):
            i, gap = owner[(x, y)]
            m, t = mats[i]
            if gap < 0.7:
                c = STONE[0]
            else:
                up, lf = owner[(x, (y - 1) % 16)], owner[((x - 1) % 16, y)]
                dn, rt = owner[(x, (y + 1) % 16)], owner[((x + 1) % 16, y)]
                if up[0] != i or up[1] < 0.7 or lf[0] != i or lf[1] < 0.7:
                    c = m[min(6, t + 1)]
                elif dn[0] != i or dn[1] < 0.7 or rt[0] != i or rt[1] < 0.7:
                    c = m[t - 1]
                else:
                    c = m[t]
            img.putpixel((x, y), c)
    return img


# ------------------------------------------------------------------ masonry
BRICK_ROWS = 4  # 3px brick + 1px mortar, like vanilla bricks


def bricks(cracked=False):
    img = blank()
    mortar, mortar_lit = STONE[1], STONE[2]
    for y in range(16):
        row = y // BRICK_ROWS
        off = 4 if row % 2 else 0
        ly = y % BRICK_ROWS
        for x in range(16):
            lx = (x + off) % 8
            if ly == 3 or lx == 7:
                c = mortar_lit if (ly == 3 and lx in (2, 3)) else mortar
            else:
                bi = ((x + off) // 8 + row * 5) % 7
                tone = (3, 3, 2, 3, 4, 3, 2)[bi]
                c = BRICK[tone]
                if ly == 0 and lx < 6:
                    c = BRICK[min(6, tone + 1)]
                if ly == 2 and lx > 0:
                    c = BRICK[max(0, tone - 1)]
                # scorch: soot climbs from the bottom row of some bricks
                if bi in (1, 5) and ly == 2:
                    c = BRICK[0]
                if bi == 5 and ly == 1 and lx in (2, 3, 4):
                    c = BRICK[1]
            img.putpixel((x, y), c)
    # chips knocked off the corners
    for (x, y) in [(1, 0), (12, 4), (5, 9), (14, 13)]:
        img.putpixel((x, y), BRICK[1])
    if cracked:
        crack(img, BRICK, [(5, 1), (6, 2), (6, 3), (7, 4), (8, 5), (8, 6), (9, 7), (10, 7), (11, 8), (11, 9)])
        crack(img, BRICK, [(1, 10), (2, 11), (2, 12), (3, 13), (3, 14)])
        img.putpixel((9, 8), STONE[0])
    return img


def crack(img, rmp, path):
    """Vanilla-style crack: darkest core, a lit lip on the pixel below/right."""
    for (x, y) in path:
        px(img, x, y, rmp[0])
    for (x, y) in path:
        if (x, y + 1) not in path:
            c = img.getpixel((x % 16, (y + 1) % 16))
            if c != rmp[0]:
                px(img, x, y + 1, rmp[4])


def concrete(cracked=False):
    """Poured concrete: large soft tone patches, form-tie holes, rain/soot streaks."""
    n = fbm(16, 16, 505, cells=(8, 4), weights=(0.7, 0.3))
    img = blank()
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), CONCRETE[quant(n[y][x], [(0.42, 2), (0.7, 3), (1.1, 4)])])
    # panel seam on the top edge and left edge
    for i in range(16):
        img.putpixel((i, 0), CONCRETE[4])
        img.putpixel((0, i), CONCRETE[4])
        img.putpixel((i, 15), CONCRETE[1])
        img.putpixel((15, i), CONCRETE[2])
    for (x, y) in [(4, 5), (11, 10)]:  # tie holes
        img.putpixel((x, y), CONCRETE[0])
        img.putpixel((x, y + 1), CONCRETE[5])
    for (x, y0, ln) in [(7, 5, 6), (13, 6, 5), (2, 12, 3)]:  # streaks
        for y in range(y0, min(15, y0 + ln)):
            img.putpixel((x, y), CONCRETE[2])
    if cracked:
        crack(img, CONCRETE, [(9, 1), (9, 2), (8, 3), (8, 4), (7, 5), (6, 6), (6, 7), (5, 8), (5, 9), (6, 10), (6, 11)])
        crack(img, CONCRETE, [(12, 9), (13, 10), (13, 11), (14, 12)])
        for (x, y) in [(2, 3), (3, 3), (2, 4)]:  # spalled patch showing aggregate
            img.putpixel((x, y), STONE[2])
        img.putpixel((3, 4), STONE[4])
    return img


# ------------------------------------------------------------------ wood
def charred_log():
    """Burnt bark: alligator-cracked charcoal plates, lit on top edges, a few ember seams."""
    img = blank(CHAR[1])
    r = random.Random(7)
    # plates: columns 4 wide, rows of random height, offset per column
    widths = [4, 3, 5, 4]
    x0 = 0
    for col, wd in enumerate(widths):
        y = r.randrange(4)
        while y < 16 + 4:
            hgt = r.randint(3, 5)
            for yy in range(y, y + hgt - 1):
                for xx in range(x0, x0 + wd - 1):
                    c = CHAR[3] if xx != x0 + wd - 2 else CHAR[2]
                    if yy == y:
                        c = CHAR[4]
                    px(img, xx, yy, c)
            y += hgt
        x0 += wd
    for (x, y) in [(7, 11), (15, 3)]:
        px(img, x, y, EMBER[1])
    return img


def charred_log_top():
    img = blank()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 6.6 or x in (0, 15) or y in (0, 15):
                c = CHAR[1] if (x + y) % 3 else CHAR[2]
            elif d < 1.6:
                c = CHAR[4]
            else:
                c = CHAR[4] if int(d) % 2 == 0 else CHAR[3]
            img.putpixel((x, y), c)
    for (x, y) in [(5, 3), (6, 4), (6, 5), (7, 6)]:  # radial heat split
        img.putpixel((x, y), CHAR[0])
    img.putpixel((11, 10), EMBER[2])
    return img


def charred_planks():
    """Four 4px planks like vanilla: lit top row, grain, dark seam, end joints and nails."""
    img = blank()
    joints = [5, 13, 2, 9]
    for y in range(16):
        p, ly = y // 4, y % 4
        for x in range(16):
            if ly == 3:
                c = CHAR[0]
            elif x == joints[p]:
                c = CHAR[1]
            else:
                c = CHAR[3]
                if ly == 0:
                    c = CHAR[4]
                if ly == 1 and (x * 7 + p * 3) % 6 == 0:
                    c = CHAR[2]
                if ly == 2 and (x * 5 + p) % 5 == 0:
                    c = CHAR[2]
            img.putpixel((x, y), c)
    for p in range(4):
        for dx in (-2, 2):
            x = (joints[p] + dx) % 16
            img.putpixel((x, p * 4 + 1), CHAR[5])
    return img


# ------------------------------------------------------------------ misc
def rebar():
    img = blank()
    for x0 in (1, 7, 13):
        for y in range(16):
            rib = (y + x0) % 3 == 0
            img.putpixel((x0, y), RUST[5] if rib else RUST[4])
            img.putpixel((x0 + 1, y), RUST[2] if rib else RUST[1])
    for y0 in (3, 12):
        for x in range(16):
            if img.getpixel((x, y0))[3] == 0:
                img.putpixel((x, y0), RUST[3])
                img.putpixel((x, y0 + 1), RUST[1])
    return img


def shattered_glass():
    img = blank()
    frame = STONE[1]
    for i in range(16):
        for p in [(i, 0), (i, 15), (0, i), (15, i)]:
            img.putpixel(p, frame)
        img.putpixel((i, 1), STONE[3]) if 0 < i < 15 else None
    pane = (196, 206, 212, 90)
    edge = (232, 240, 244, 200)
    # jagged shards left in the frame corners
    shards = [[(1, 2), (2, 2), (3, 2), (4, 2), (1, 3), (2, 3), (3, 3), (1, 4), (2, 4), (1, 5)],
              [(11, 2), (12, 2), (13, 2), (14, 2), (13, 3), (14, 3), (14, 4), (14, 5), (14, 6)],
              [(1, 10), (1, 11), (2, 11), (1, 12), (2, 12), (3, 12), (1, 13), (2, 13), (3, 13), (4, 13), (5, 14), (1, 14), (2, 14), (3, 14), (4, 14)],
              [(13, 12), (14, 11), (14, 12), (12, 13), (13, 13), (14, 13), (11, 14), (12, 14), (13, 14), (14, 14)]]
    for s in shards:
        for p in s:
            img.putpixel(p, pane)
        img.putpixel(s[-1], edge)
        img.putpixel(s[len(s) // 2], edge)
    return img


def sandbags(top=False):
    """Stacked bags: rounded, lit on top, shadowed underneath, stitched seams."""
    img = blank(CANVAS[0])
    rows = [(0, 5), (5, 5), (10, 6)] if not top else [(0, 8), (8, 8)]
    for ri, (y0, h) in enumerate(rows):
        off = 4 if ri % 2 else 0
        for y in range(y0, y0 + h):
            ly = y - y0
            for x in range(16):
                lx = (x + off) % 8
                if lx == 7 or ly == h - 1:
                    c = CANVAS[0] if ly == h - 1 else CANVAS[1]
                elif ly == 0:
                    c = CANVAS[5] if 1 <= lx <= 5 else CANVAS[3]
                elif ly == 1:
                    c = CANVAS[4]
                elif ly == h - 2:
                    c = CANVAS[2]
                else:
                    c = CANVAS[3]
                if lx == 0 and 0 < ly < h - 1:
                    c = CANVAS[2]
                if top and lx == 3 and ly % 2 == 1 and 0 < ly < h - 2:
                    c = CANVAS[2]  # tie seam
                img.putpixel((x, y), c)
    return img


def portal():
    """Churning smoke with ash and embers drifting down; tiles vertically so it loops."""
    greys = ramp("6E6E6A", step=0.17)
    frames = 32
    r = random.Random(14)
    img = Image.new("RGBA", (16, 16 * frames))
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
                c = greys[quant(v, [(0.25, 1), (0.45, 2), (0.65, 3), (0.85, 4), (1.1, 5)])]
                img.putpixel((x, y + f * 16), c[:3] + (205,))
        for (sx, sy, ember) in specks:
            yy = (sy + f * 2) % 32
            if yy < 16:
                c = EMBER[5] if ember else greys[6]
                img.putpixel((sx, yy + f * 16), c[:3] + (235,))
    return img


ALL = {
    "ash_block": ash_block, "ashen_soil": ashen_soil, "rubble": rubble,
    "scorched_bricks": lambda: bricks(), "cracked_scorched_bricks": lambda: bricks(True),
    "scorched_concrete": lambda: concrete(), "cracked_scorched_concrete": lambda: concrete(True),
    "charred_log": charred_log, "charred_log_top": charred_log_top, "charred_planks": charred_planks,
    "rebar": rebar, "shattered_glass": shattered_glass,
    "sandbags": lambda: sandbags(), "sandbags_top": lambda: sandbags(True),
}
