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
def ash_block(variant=0):
    """Fine settled ash. Variant 0 plain; 1 wind ripples; 2 cinder cluster; 3 fresh pale fall."""
    n = fbm(16, 16, 101 + variant * 17, cells=(4, 2), weights=(0.75, 0.25))
    img = blank()
    lift = 1 if variant == 3 else 0
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), ASH[quant(n[y][x], [(0.36, 2 + lift), (0.6, 3 + lift), (1.1, 4 + lift)])])
    r = random.Random(1 + variant * 5)
    for _ in range(7 if variant != 3 else 3):
        x, y = r.randrange(16), r.randrange(16)
        px(img, x, y, ASH[1])
        if r.random() < 0.5:
            px(img, x + 1, y, ASH[2])
    for _ in range(6):
        px(img, r.randrange(16), r.randrange(16), ASH[5])
    if variant == 1:  # soot settled into the hollows
        m = fbm(16, 16, 909, cells=(4, 2), weights=(0.8, 0.2))
        for y in range(16):
            for x in range(16):
                if m[y][x] < 0.3:
                    c = img.getpixel((x, y))
                    i = ASH.index(c) if c in ASH else 3
                    img.putpixel((x, y), ASH[max(1, i - 1)])
    elif variant == 2:
        for (x, y, c) in [(6, 9, 0), (7, 9, 1), (6, 10, 1), (8, 10, 0), (7, 10, 0), (9, 11, 1), (5, 11, 1)]:
            img.putpixel((x, y), ASH[c] if c else CHAR[1])
        img.putpixel((7, 11), EMBER[2])
    elif variant == 4:  # charred stick half buried in ash
        for (x, y) in [(3, 5), (4, 5), (5, 6), (6, 6), (7, 7), (8, 7), (9, 8), (10, 8), (11, 9)]:
            px(img, x, y, CHAR[2])
            px(img, x, y - 1, CHAR[4])
            px(img, x, y + 1, ASH[2])
        for (x, y) in [(5, 7), (8, 8)]:
            px(img, x, y, ASH[4])  # ash drifted over it
    return img


def burnt_branch(img, pts, twigs=()):
    """A charred stick lying on the ground: dark core, lit upper edge, glowing split, shadow below."""
    for (x, y) in pts:
        px(img, x, y, CHAR[2])
        px(img, x, y - 1, CHAR[4])
        px(img, x, y + 1, SOIL[0])
    for (x, y) in pts[len(pts) // 3: len(pts) // 3 + 2]:
        px(img, x, y, EMBER[2])
    for tw in twigs:
        for (x, y) in tw:
            px(img, x, y, CHAR[3])
            px(img, x + 1, y + 1, SOIL[1])


def dead_leaf(img, x0, y0):
    """A curled leaf rotted down to its veins."""
    leaf = ramp("5E4A33", step=0.2)
    shape = ["..ab..", ".abbc.", "abvbbc", "bbvbc.", ".bvc..", "..v..."]
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            c = {"a": leaf[4], "b": leaf[3], "c": leaf[1], "v": leaf[5]}[ch]
            if ch == "b" and hash2(x, y, 77) < 0.3:
                c = SOIL[2]  # holes eaten through
            px(img, x0 + x, y0 + y, c)


def hardpan(img, seed):
    """Sun-baked cracked crust: polygon plates, lit on top edges, dark gaps (tileable Voronoi)."""
    r = random.Random(seed)
    pts = [(r.uniform(0, 16), r.uniform(0, 16)) for _ in range(11)]
    plate = ramp("625C53", step=0.12)
    for y in range(16):
        for x in range(16):
            ds = sorted((math.hypot(min(abs(x + .5 - cx), 16 - abs(x + .5 - cx)), min(abs(y + .5 - cy), 16 - abs(y + .5 - cy))), i)
                        for i, (cx, cy) in enumerate(pts))
            gap = ds[1][0] - ds[0][0]
            if gap < 0.7:
                c = SOIL[1]
            elif gap < 1.5 and ds[0][1] % 2 == 0:
                c = plate[4]
            else:
                c = plate[3 - (ds[0][1] % 3 == 0)]
            img.putpixel((x, y), c)


def dead_grass(img, x0, y0):
    straw = ramp("8A7A52", step=0.18)
    for (dx, dy, c) in [(0, 0, 4), (2, -1, 5), (1, -2, 3), (3, 0, 3), (4, -2, 4), (2, 1, 2), (1, 1, 1), (3, 1, 1)]:
        px(img, x0 + dx, y0 + dy, straw[c])


def rusted_can(img, x0, y0):
    """A crushed, rusted tin lying on its side."""
    for (dx, dy, c) in [(0, 0, 4), (1, 0, 5), (2, 0, 4), (3, 0, 3), (0, 1, 3), (1, 1, 3), (2, 1, 2), (3, 1, 1), (4, 1, 2),
                        (1, 2, 1), (2, 2, 0), (3, 2, 1)]:
        px(img, x0 + dx, y0 + dy, RUST[c])
    px(img, x0 + 4, y0, CONCRETE[5])  # bare metal on the torn lip


def ashen_soil(variant=0):
    """Ash-choked dirt. Variant 0 plain; 1 dried mud cracks; 2 scorched patch; 3 buried shrapnel and bone."""
    n = fbm(16, 16, 202 + variant * 13, cells=(4, 2), weights=(0.7, 0.3))
    img = blank()
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), SOIL[quant(n[y][x], [(0.34, 2), (0.62, 3), (1.1, 4)])])
    r = random.Random(2 + variant * 7)
    for _ in range(5):  # clods: lit top, shadow below
        x, y = r.randrange(16), r.randrange(16)
        px(img, x, y, SOIL[5])
        px(img, x + 1, y, SOIL[4])
        px(img, x, y + 1, SOIL[1])
        px(img, x + 1, y + 1, SOIL[1])
    for _ in range(4):  # ash dusting
        px(img, r.randrange(16), r.randrange(16), ASH[3])
    if variant == 1:
        crack(img, SOIL, [(1, 6), (2, 6), (3, 7), (4, 7), (5, 7), (6, 8), (7, 8), (8, 9), (9, 9), (10, 10), (11, 10), (12, 11)])
        crack(img, SOIL, [(9, 9), (9, 8), (10, 7), (10, 6)])
    elif variant == 2:
        for y in range(16):
            for x in range(16):
                d = math.hypot(min(abs(x - 9), 16 - abs(x - 9)), min(abs(y - 6), 16 - abs(y - 6)))
                if d < 4.5 + hash2(x, y, 9) * 1.5:
                    c = img.getpixel((x, y))
                    img.putpixel((x, y), SOIL[1] if c == SOIL[2] else SOIL[2] if c in (SOIL[3], SOIL[4]) else c)
        for (x, y) in [(8, 5), (10, 7)]:
            img.putpixel((x, y), CHAR[1])
        img.putpixel((9, 6), EMBER[1])
    elif variant == 3:
        for (x, y) in [(3, 10), (4, 10), (5, 9)]:  # rusted shard
            img.putpixel((x, y), RUST[3])
        img.putpixel((3, 11), RUST[1])
        img.putpixel((4, 9), RUST[5])
        for (x, y) in [(10, 3), (11, 3), (12, 4)]:  # bit of bone
            img.putpixel((x, y), ASH[5])
        img.putpixel((11, 4), ASH[2])
    elif variant == 4:  # fallen burnt branch with a couple of twigs
        burnt_branch(img, [(1, 11), (2, 11), (3, 10), (4, 10), (5, 10), (6, 9), (7, 9), (8, 9), (9, 8), (10, 8), (11, 7), (12, 7), (13, 6)],
                     twigs=[[(6, 8), (6, 7), (5, 6)], [(10, 9), (11, 10), (12, 11)]])
    elif variant == 5:  # rotted leaves and a charred root breaking the surface
        dead_leaf(img, 2, 2)
        dead_leaf(img, 9, 9)
        burnt_branch(img, [(10, 3), (11, 3), (12, 4), (13, 4), (14, 5)])
    elif variant == 6:  # cracked hardpan with a dead tuft
        hardpan(img, 61)
        dead_grass(img, 4, 9)
    elif variant == 7:  # wasteland litter: rusted tin, scrap and dry stalks
        rusted_can(img, 8, 4)
        dead_grass(img, 2, 12)
        for (x, y) in [(12, 11), (13, 11), (13, 12)]:
            px(img, x, y, RUST[2])
        px(img, 12, 10, RUST[4])
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
    **{f"ash_block_{i + 1}": (lambda i=i: ash_block(i)) for i in range(1, 5)},
    **{f"ashen_soil_{i + 1}": (lambda i=i: ashen_soil(i)) for i in range(1, 8)},
    "scorched_bricks": lambda: bricks(), "cracked_scorched_bricks": lambda: bricks(True),
    "scorched_concrete": lambda: concrete(), "cracked_scorched_concrete": lambda: concrete(True),
    "charred_log": charred_log, "charred_log_top": charred_log_top, "charred_planks": charred_planks,
    "rebar": rebar, "shattered_glass": shattered_glass,
    "sandbags": lambda: sandbags(), "sandbags_top": lambda: sandbags(True),
}


# ------------------------------------------------------------------ wasteland
SLUDGE = ramp("6F7C26", step=0.14)
RUBBER = ramp("2E2C2A", step=0.22)


def rusted_plating():
    """A single sheet panel: lit top lip, rivets at the corners, rust blooming through grey paint."""
    img = blank()
    n = fbm(16, 16, 71, cells=(4, 2), weights=(0.7, 0.3))
    for y in range(16):
        for x in range(16):
            v = n[y][x]
            if v < 0.47:
                c = STONE[quant(v, [(0.36, 2), (1.1, 3)])]
            else:
                c = RUST[quant(v, [(0.55, 2), (0.66, 3), (1.1, 4)])]
            if y == 0 or x == 0:
                c = STONE[4] if v < 0.47 else RUST[4]
            elif y == 15 or x == 15:
                c = RUST[0]
            img.putpixel((x, y), c)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px(img, x, y, STONE[5])
        px(img, x + 1, y + 1, RUST[1])
        px(img, x, y + 2, RUST[2])  # rust weep
    return img


def scrap_pile():
    """Loose heap of bent sheet, pipe, bolts and wire."""
    img = blank(RUST[0])
    r = random.Random(81)
    for _ in range(9):
        x0, y0, w, h = r.randrange(16), r.randrange(16), r.randint(3, 7), r.randint(2, 4)
        m = r.choice((RUST, RUST, STONE, CONCRETE))
        t = r.randint(2, 4)
        for y in range(h):
            for x in range(w):
                c = m[min(6, t + 1)] if y == 0 else (m[t - 1] if y == h - 1 else m[t])
                if x == 0 and y:
                    c = m[t - 1]
                px(img, x0 + x, y0 + y, c)
    for _ in range(5):  # bolts
        x, y = r.randrange(16), r.randrange(16)
        px(img, x, y, STONE[5])
        px(img, x + 1, y + 1, STONE[1])
    for _ in range(2):  # wire
        x, y = r.randrange(16), r.randrange(16)
        for i in range(6):
            px(img, x + i, y + int(1.5 * math.sin(i)), (150, 92, 48, 255) if i % 2 else (112, 66, 34, 255))
    return img


def burnt_tire():
    """Tread side: chunky lugs, melted and cracked."""
    img = blank()
    for y in range(16):
        for x in range(16):
            lug = (x // 4 + (y // 3) % 2) % 2
            if y % 3 == 2:
                c = RUBBER[0]
            elif lug:
                c = RUBBER[4] if y % 3 == 0 else RUBBER[3]
            else:
                c = RUBBER[2] if y % 3 == 0 else RUBBER[1]
            img.putpixel((x, y), c)
    r = random.Random(91)
    for _ in range(4):
        px(img, r.randrange(16), r.randrange(16), ASH[3])
    for x in range(16):
        img.putpixel((x, 0), RUBBER[5] if x % 4 else RUBBER[4])
        img.putpixel((x, 15), RUBBER[0])
    return img


def burnt_tire_end():
    """Sidewall rings around a rusted rim and an empty hub."""
    img = blank()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            lit = (x + y) < 15
            if d < 2.2:
                c = RUBBER[0] if d < 1.5 else RUST[1]
            elif d < 4.5:
                c = RUST[4 if lit else 2] if int(d * 2) % 2 else RUST[3]
            elif d < 5.2:
                c = RUBBER[0]
            elif d < 7.6:
                c = RUBBER[3 if lit else 2] if int(d) % 2 else RUBBER[1 if not lit else 2]
            else:
                c = RUBBER[1]
            img.putpixel((x, y), c)
    return img


def toxic_sludge(frames=8):
    """Bubbling sickly green ooze; an animated vertical strip."""
    img = blank(w=16, h=16 * frames)
    for f in range(frames):
        n = fbm(16, 16, 101 + f // 2, cells=(4, 2), weights=(0.65, 0.35))
        n2 = fbm(16, 16, 201 + (f + 1) // 2, cells=(4, 2), weights=(0.65, 0.35))
        a = (f % 2) * 0.5
        for y in range(16):
            for x in range(16):
                v = n[y][x] * (1 - a) + n2[y][x] * a
                img.putpixel((x, f * 16 + y), SLUDGE[quant(v, [(0.3, 1), (0.42, 2), (0.56, 3), (0.68, 4), (1.1, 5)])])
        r = random.Random(300 + f)
        for _ in range(3):
            x, y = r.randrange(1, 15), r.randrange(1, 15)
            px(img, x, f * 16 + y, SLUDGE[6])
            px(img, x + 1, f * 16 + y, SLUDGE[5])
            px(img, x, f * 16 + y + 1, SLUDGE[2])
    return img


ALL.update({
    "rusted_plating": rusted_plating, "scrap_pile": scrap_pile,
    "burnt_tire": burnt_tire, "burnt_tire_end": burnt_tire_end,
})


# ------------------------------------------------------------------ charred joinery
def _boards(img, y0, y1, seed):
    """Vertical charred boards with lit left edges and dark seams between them."""
    r = random.Random(seed)
    for y in range(y0, y1):
        for x in range(16):
            lx = (x - 1) % 5
            if x in (0, 15):
                c = CHAR[1]
            elif lx == 4:
                c = CHAR[0]
            elif lx == 0:
                c = CHAR[4]
            else:
                c = CHAR[3] if (x * 3 + y * 7 + seed) % 9 else CHAR[2]
            img.putpixel((x, y), c)
    for _ in range(3):  # ember-glowing cracks
        x, y = r.randrange(2, 14), r.randrange(y0 + 1, y1 - 1)
        if img.getpixel((x, y)) not in (CHAR[0], CHAR[1]):
            img.putpixel((x, y), EMBER[1])


def charred_door_top():
    img = blank()
    _boards(img, 0, 16, 5)
    for x in range(16):
        img.putpixel((x, 0), CHAR[5])
        img.putpixel((x, 15), CHAR[1])
    # smashed-out window: ragged hole with a couple of shards left
    hole = {(x, y) for x in range(4, 12) for y in range(3, 10)} - {(4, 3), (11, 3), (4, 9), (11, 9), (10, 9), (5, 3)}
    hole |= {(7, 10), (8, 10), (3, 6), (12, 5)}
    for (x, y) in hole:
        img.putpixel((x, y), (0, 0, 0, 0))
    for (x, y) in ((4, 4), (5, 4), (4, 5), (11, 8), (10, 8)):
        img.putpixel((x, y), (196, 206, 212, 120))
    for x in range(3, 13):
        if img.getpixel((x, 2))[3]:
            img.putpixel((x, 2), CHAR[0])
    for x in range(3, 13):
        if img.getpixel((x, 11))[3] and (x, 10) in hole:
            img.putpixel((x, 11), CHAR[5])
    return img


def charred_door_bottom():
    img = blank()
    _boards(img, 0, 16, 11)
    for x in range(16):
        img.putpixel((x, 15), CHAR[0])
        img.putpixel((x, 6), CHAR[1])  # cross rail
        img.putpixel((x, 5), CHAR[4])
    for (x, y) in ((12, 1), (13, 1), (12, 2), (13, 2)):  # rusted handle
        img.putpixel((x, y), RUST[4] if y == 1 else RUST[2])
    for y in (3, 11):  # hinges
        for x in range(0, 3):
            img.putpixel((x, y), RUST[3] if x < 2 else RUST[1])
    for (x, y) in ((6, 15), (7, 15), (8, 15), (7, 14), (6, 14), (9, 15)):  # burnt-through bottom
        img.putpixel((x, y), (0, 0, 0, 0))
    return img


def charred_trapdoor():
    img = blank()
    _boards(img, 0, 16, 23)
    for i in range(16):
        for p in ((i, 0), (0, i)):
            img.putpixel(p, CHAR[5])
        for p in ((i, 15), (15, i)):
            img.putpixel(p, CHAR[0])
    for y in (4, 11):
        for x in range(1, 15):
            img.putpixel((x, y), CHAR[4] if y == 4 else CHAR[1])
    for (x, y) in ((6, 7), (7, 7), (7, 8), (8, 8), (9, 9), (12, 2), (2, 13), (3, 13)):  # burnt holes
        img.putpixel((x, y), (0, 0, 0, 0))
    return img


def charred_door_item():
    img = blank()
    top, bot = charred_door_top(), charred_door_bottom()
    for half, oy in ((top, 0), (bot, 8)):
        small = half.resize((8, 8), Image.NEAREST)
        img.paste(small, (4, oy), small)
    return img


ALL.update({"charred_door_top": charred_door_top, "charred_door_bottom": charred_door_bottom,
            "charred_trapdoor": charred_trapdoor})


# ------------------------------------------------------------------ roads
TARMAC = ramp("3C3C3E", step=0.16)
PAINT = ramp("BBA552", step=0.12)


def asphalt(cracked=False, seed=121):
    """Weathered tarmac: fine dark aggregate, pale chips, a few oil stains; optionally crazed."""
    img = blank()
    n = fbm(16, 16, seed, cells=(4, 2), weights=(0.6, 0.4))
    r = random.Random(seed)
    for y in range(16):
        for x in range(16):
            c = TARMAC[quant(n[y][x], [(0.35, 2), (0.6, 3), (1.1, 4)])]
            h = hash2(x, y, seed)
            if h > 0.93:
                c = TARMAC[5]
            elif h < 0.07:
                c = TARMAC[1]
            img.putpixel((x, y), c)
    if cracked:
        for path in ([(1, 3), (2, 4), (3, 4), (4, 5), (5, 7), (6, 8), (7, 8), (8, 9), (10, 10), (11, 12), (12, 13), (14, 14)],
                     [(7, 8), (7, 6), (8, 5), (9, 3), (10, 2), (10, 0)],
                     [(8, 9), (6, 11), (5, 12), (3, 14), (2, 15)],
                     [(11, 12), (13, 11), (15, 10)]):
            for (a, b) in zip(path, path[1:]):
                steps = max(abs(b[0] - a[0]), abs(b[1] - a[1]))
                for i in range(steps + 1):
                    x = round(a[0] + (b[0] - a[0]) * i / steps)
                    y = round(a[1] + (b[1] - a[1]) * i / steps)
                    px(img, x, y, TARMAC[0])
                    if hash2(x, y, 9) > 0.5:
                        px(img, x + 1, y, TARMAC[1])
        for _ in range(3):  # ash blown into the cracks
            px(img, r.randrange(16), r.randrange(16), ASH[2])
    return img


def asphalt_line():
    """Tarmac with a worn, flaking centre line running along the texture's vertical (the road's axis)."""
    img = asphalt(seed=131)
    for y in range(16):
        for x in (7, 8):
            h = hash2(x, y, 77)
            if h < 0.18:
                continue  # flaked off
            img.putpixel((x, y), PAINT[4] if x == 7 else PAINT[3] if h > 0.4 else PAINT[2])
    return img


ALL.update({"asphalt": asphalt, "cracked_asphalt": lambda: asphalt(True), "asphalt_line": asphalt_line})
