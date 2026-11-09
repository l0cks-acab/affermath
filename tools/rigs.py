"""Custom mob rigs: one spec drives both the Java model geometry (RigLayers.java) and the
painted skins, so UVs can never drift apart.

Coordinates follow vanilla entity models: pixels, y points down, feet at y=24, -z is the
front. A box's paint function gets (face, x, y, w, h) for every pixel of every face and
returns None (default shading), an int (index into the box's ramp), a colour tuple,
("glow", colour) for emissive pixels, or "clear"."""
import math
from PIL import Image

from palette import ramp, hexc, hash2

# ------------------------------------------------------------------ ramps
R_SKIN = ramp("4C4740", step=0.18)        # desiccated, ash-caked skin
R_BONE = ramp("B3AE9F", step=0.14)
R_COAT = ramp("4F4A3B", step=0.18)        # drab greatcoat
R_TROUSER = ramp("454234", step=0.18)
R_HELMET = ramp("50553F", step=0.2)
R_RUBBER = ramp("3A3833", step=0.22)
R_LEATHER = ramp("3E3126", step=0.2)
R_CANVAS = ramp("6E6650", step=0.16)
R_BRASS = ramp("7C6A3E", step=0.18)
R_SHROUD = ramp("7A7874", step=0.15)
R_HOOD = ramp("504E4B", step=0.2)
R_FEATHER = ramp("26262E", step=0.26)
R_BEAK = ramp("3B3733", step=0.22)
R_IRON = ramp("3E3F45", step=0.22)
R_RUST = ramp("6E3B22", step=0.2)
R_BOSSCOAT = ramp("2F2C29", step=0.22)
R_OLIVE = ramp("4F5638", step=0.2)
EMBER = [hexc(h) for h in ("5A1E0E", "7E2C10", "A8401A", "D0601F", "EE8A2E", "FFB24A", "FFE08A")]
SPECTRE = [hexc(h) for h in ("2C3A44", "46606E", "6A8C9C", "94BCCB", "BFE2EE", "DDF4FA", "F6FEFF")]

# 1.21.2+ hangs the hat layer off the head; 1.21.1 keeps it at the root
HAT_PARENT = None

FACES = ("top", "bottom", "right", "front", "left", "back")


class Box:
    def __init__(self, x, y, z, w, h, d, mat, base=3, paint=None, inflate=0.0, grain=0.12, seed=0):
        self.x, self.y, self.z, self.w, self.h, self.d = x, y, z, w, h, d
        self.mat, self.base, self.paint, self.inflate, self.grain, self.seed = mat, base, paint, inflate, grain, seed
        self.u = self.v = None

    @property
    def net(self):
        return 2 * (self.w + self.d), self.d + self.h


class Part:
    def __init__(self, name, parent, pivot, rot=(0, 0, 0), boxes=()):
        self.name, self.parent, self.pivot, self.rot, self.boxes = name, parent, pivot, rot, list(boxes)


class Rig:
    def __init__(self, name, tw, th, parts):
        self.name, self.tw, self.th, self.parts = name, tw, th, parts
        self.pack()

    def pack(self):
        """Skyline bottom-left packing of every box's UV net."""
        boxes = [b for p in self.parts for b in p.boxes]
        order = sorted(boxes, key=lambda b: (-b.net[1] * b.net[0], -b.net[0]))
        sky = [0] * self.tw
        for b in order:
            w, h = b.net
            best = None
            for x in range(0, self.tw - w + 1):
                y = max(sky[x:x + w])
                if best is None or y < best[1]:
                    best = (x, y)
            if best is None or best[1] + h > self.th:
                raise SystemExit(f"{self.name}: UVs overflow {self.tw}x{self.th}")
            x, y = best
            b.u, b.v = x, y
            for i in range(x, x + w):
                sky[i] = y + h

    # -------------------------------------------------------------- java
    def java(self):
        out = [f"    public static LayerDefinition {self.name}() {{",
               "        MeshDefinition mesh = new MeshDefinition();",
               "        PartDefinition root = mesh.getRoot();"]
        for p in self.parts:
            cubes = "CubeListBuilder.create()"
            for b in p.boxes:
                cubes += f".texOffs({b.u}, {b.v}).addBox({f(b.x)}, {f(b.y)}, {f(b.z)}, {f(b.w)}, {f(b.h)}, {f(b.d)}"
                cubes += f", new CubeDeformation({f(b.inflate)}))" if b.inflate else ")"
            px, py, pz = p.pivot
            rx, ry, rz = p.rot
            pose = (f"PartPose.offsetAndRotation({f(px)}, {f(py)}, {f(pz)}, {f(rx)}, {f(ry)}, {f(rz)})"
                    if any(p.rot) else f"PartPose.offset({f(px)}, {f(py)}, {f(pz)})")
            parent = "root" if p.parent is None else var(p.parent)
            out.append(f"        PartDefinition {var(p.name)} = {parent}.addOrReplaceChild(\"{p.name}\", {cubes}, {pose});")
        out.append(f"        return LayerDefinition.create(mesh, {self.tw}, {self.th});")
        out.append("    }")
        return "\n".join(out)

    # -------------------------------------------------------------- paint
    def paint(self):
        img = Image.new("RGBA", (self.tw, self.th), (0, 0, 0, 0))
        glow = Image.new("RGBA", (self.tw, self.th), (0, 0, 0, 0))
        for p in self.parts:
            for b in p.boxes:
                paint_box(img, glow, b)
        return img, glow


def f(v):
    return f"{float(v)}f"


def var(name):
    return "p_" + name


def face_rects(b):
    u, v, w, h, d = b.u, b.v, b.w, b.h, b.d
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


def paint_box(img, glow, b):
    m = b.mat
    for face, (fx, fy, fw, fh) in face_rects(b).items():
        for y in range(fh):
            for x in range(fw):
                i = b.base
                if face == "top":
                    i += 1
                elif face == "bottom":
                    i -= 2
                else:
                    if fh >= 4 and y >= fh - max(1, fh // 4):
                        i -= 1
                    elif fh >= 4 and y == 0:
                        i += 1
                    if face == "back":
                        i -= 0
                g = hash2(x + fx * 3, y + fy * 7, b.seed + 17)
                g2 = hash2((x + fx) // 2, (y + fy) // 2, b.seed + 5)
                if g2 < b.grain:
                    i -= 1
                elif g2 > 1 - b.grain * 0.6:
                    i += 1
                elif g < b.grain * 0.4:
                    i -= 1
                c = m[max(0, min(len(m) - 1, i))]
                if b.paint:
                    r = b.paint(face, x, y, fw, fh)
                    if r == "clear":
                        continue
                    if isinstance(r, int):
                        c = m[max(0, min(len(m) - 1, r))]
                    elif isinstance(r, tuple) and r and r[0] == "glow":
                        c = r[1]
                        glow.putpixel((fx + x, fy + y), c)
                    elif isinstance(r, tuple):
                        c = r
                img.putpixel((fx + x, fy + y), c)


# ------------------------------------------------------------------ helpers for paint fns
def ragged(seed, depth=2):
    """Clear a ragged bottom edge on side faces."""
    def fn(face, x, y, w, h):
        if face == "bottom":
            return "clear"
        if face in ("top",):
            return None
        cut = int(hash2(x, 0, seed) * (depth + 1))
        if y >= h - cut:
            return "clear"
        return None
    return fn


def chain(*fns):
    def fn(face, x, y, w, h):
        for g in fns:
            r = g(face, x, y, w, h)
            if r is not None:
                return r
        return None
    return fn


# ================================================================== HOLLOW SOLDIER
def _soldier_head(face, x, y, w, h):
    if face == "front":
        # gas mask: rubber face plate, two round lenses glowing ember from inside
        if 1 <= y <= 7 and 1 <= x <= 6:
            if y in (2, 3) and x in (1, 2, 5, 6):
                if (x, y) in ((1, 2), (5, 2)):
                    return ("glow", EMBER[6])
                return ("glow", EMBER[4] if y == 2 else EMBER[3])
            if y == 1:
                return R_RUBBER[4]
            if y >= 6:
                return R_RUBBER[1]
            return R_RUBBER[3] if (x + y) % 3 else R_RUBBER[2]
        if y == 0 or x in (0, 7):
            return 2
    if face in ("right", "left") and y in (2, 3):
        return R_LEATHER[2] if y == 2 else R_LEATHER[1]  # mask strap
    if face == "back" and y in (2, 3):
        return R_LEATHER[2]
    return None


def _helmet_dome(face, x, y, w, h):
    if face == "bottom":
        return "clear"
    if face == "top" and (x, y) in ((2, 3), (6, 5), (3, 6)):
        return 1  # dents
    if face == "top" and (x + y) in (4, 12):
        return 4
    return None


def _helmet_brim(face, x, y, w, h):
    if face == "bottom":
        return 1
    if face == "top":
        if 1 <= x <= w - 2 and 1 <= y <= h - 2:
            return "clear" if 1 < x < w - 2 and 1 < y < h - 2 else None
        return 4
    return 2


def _canister(face, x, y, w, h):
    if face == "front":
        if (x, y) == (1, 1):
            return R_IRON[0]
        return R_OLIVE[2] if (x + y) % 2 else R_OLIVE[3]
    if face != "top" and face != "bottom" and y == 1:
        return R_BRASS[3]  # brass band
    return None


def _coat_body(face, x, y, w, h):
    if y in (8, 9):
        if face == "front" and x in (3, 4) and y == 8:
            return R_BRASS[4] if x == 3 else R_BRASS[2]
        return R_LEATHER[3] if y == 8 else R_LEATHER[1]
    if face == "front":
        if x in (2, 5) and y in (1, 4, 7):
            return R_BRASS[3]
        if x in (3, 4) and y < 8:
            return 2 if x == 4 else None  # overlap fold
        # torn hole showing ribs
        if 5 <= x <= 6 and 3 <= y <= 5:
            return R_SKIN[0] if y == 4 else R_BONE[2]
    if face in ("right", "left") and y == 0:
        return 4
    return None


def _coat_flap(seed):
    rag = ragged(seed, 3)

    def fn(face, x, y, w, h):
        r = rag(face, x, y, w, h)
        if r:
            return r
        if x in (0, w - 1):
            return 2
        return None
    return fn


def _arm(face, x, y, w, h):
    if y >= 9:  # bony hand
        if face == "bottom":
            return R_SKIN[1]
        return R_SKIN[3] if (x + y) % 2 else R_SKIN[2]
    if y == 8:
        return 1  # frayed cuff
    if y == 0 and face != "top":
        return 4
    return None


def _arm_skin(face, x, y, w, h):
    if y >= 9 and face != "top":
        return "skin"
    return None


def _leg(face, x, y, w, h):
    if y >= 10:
        if face == "bottom":
            return R_LEATHER[0]
        return R_LEATHER[3] if y == 10 else R_LEATHER[2]
    if 5 <= y <= 9 and face != "top":  # puttees: diagonal wraps
        return R_CANVAS[2] if (x + y) % 3 == 0 else R_CANVAS[3]
    return None


def _pouch(face, x, y, w, h):
    if face == "front" and y == 0:
        return 4
    return None


def _pack(face, x, y, w, h):
    if face == "back":
        if y in (1, 2) and x in (1, 5):
            return R_LEATHER[2]
        if y == 0:
            return 4
    return None


def _bedroll(face, x, y, w, h):
    if face in ("front", "back", "top") and x % 4 == 1:
        return R_LEATHER[2]
    return None


def soldier():
    def leatherize(fn):
        def g(face, x, y, w, h):
            r = fn(face, x, y, w, h)
            if r == "skin":
                return R_SKIN[3] if (x + y) % 2 else R_SKIN[2]
            return r
        return g

    return Rig("soldier", 128, 64, [
        Part("head", None, (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, R_SKIN, paint=_soldier_head, seed=1)]),
        Part("hat", HAT_PARENT, (0, 0, 0), boxes=[Box(-4.5, -9, -4.5, 9, 3, 9, R_HELMET, paint=_helmet_dome, seed=2),
                                            Box(-5.5, -6.5, -5.5, 11, 1, 11, R_HELMET, paint=_helmet_brim, seed=3)]),
        Part("snout", "head", (0, -3, -4), boxes=[Box(-1.5, -1.5, -2, 3, 3, 2, R_RUBBER, seed=4)]),
        Part("canister", "snout", (0, 0.5, -2), (0.35, 0, 0), boxes=[Box(-1.5, -1.5, -3, 3, 3, 3, R_OLIVE, paint=_canister, seed=5)]),
        Part("body", None, (0, 0, 0), boxes=[Box(-4, 0, -2, 8, 12, 4, R_COAT, paint=_coat_body, seed=6)]),
        Part("backpack", "body", (0, 1, 2), boxes=[Box(-3.5, 0, 0, 7, 7, 3, R_CANVAS, paint=_pack, seed=7),
                                                   Box(-4.5, 7, 0.5, 9, 2, 2, R_CANVAS, base=2, paint=_bedroll, seed=8)]),
        Part("pouches", "body", (0, 0, 0), boxes=[Box(-3.5, 7.5, -3, 3, 2, 1, R_LEATHER, paint=_pouch, seed=9),
                                                  Box(0.5, 7.5, -3, 3, 2, 1, R_LEATHER, paint=_pouch, seed=10)]),
        Part("coat_front", "body", (0, 11, -2.05), boxes=[Box(-4, 0, 0, 8, 6, 0, R_COAT, base=2, paint=_coat_flap(11), seed=11)]),
        Part("coat_back", "body", (0, 11, 2.05), boxes=[Box(-4, 0, 0, 8, 7, 0, R_COAT, base=2, paint=_coat_flap(12), seed=12)]),
        Part("right_arm", None, (-5, 2, 0), boxes=[Box(-3, -2, -2, 4, 12, 4, R_COAT, paint=leatherize(chain(_arm_skin, _arm)), seed=13)]),
        Part("left_arm", None, (5, 2, 0), boxes=[Box(-1, -2, -2, 4, 12, 4, R_COAT, paint=leatherize(chain(_arm_skin, _arm)), seed=14)]),
        Part("right_leg", None, (-1.9, 12, 0), boxes=[Box(-2, 0, -2, 4, 12, 4, R_TROUSER, paint=_leg, seed=15)]),
        Part("left_leg", None, (1.9, 12, 0), boxes=[Box(-2, 0, -2, 4, 12, 4, R_TROUSER, paint=_leg, seed=16)]),
    ])


# ================================================================== ASH WRAITH
def _skull(face, x, y, w, h):
    if face == "front":
        # deep sockets with a single cold pinprick of light in each, a broken nose, bared teeth
        if y in (2, 3) and x in (1, 2, 4, 5):
            if (x, y) in ((2, 3), (4, 3)):
                return ("glow", SPECTRE[4])
            return R_SHROUD[0]
        if y == 1:
            return R_BONE[1] if x in (1, 2, 4, 5) else 2  # heavy brow shadow
        if y == 4:
            return R_SHROUD[0] if x in (2, 3) else 2
        if y == 5:
            return R_BONE[4] if x % 2 else R_SHROUD[0]
        return 2
    if face in ("left", "right"):
        return 2 if y > 0 else None
    return None


def _hood(face, x, y, w, h):
    if face == "bottom":
        return "clear"
    if face == "front":
        if y <= 1 or x in (0, w - 1):
            return 1 if y == 1 or x in (0, w - 1) else 2
        if x in (1, w - 2) and y < h - 2:
            return R_SHROUD[0]
        return "clear"
    if face in ("left", "right") and y >= h - 1 and x % 2:
        return "clear"
    if face in ("left", "right") and x == 0:
        return 2
    return None


def _mantle(face, x, y, w, h):
    if face == "bottom":
        return 1
    if face != "top" and y == h - 1 and hash2(x, 3, 9) < 0.45:
        return "clear"
    return None


def _robe(face, x, y, w, h):
    if face == "front" and 2 <= x <= 5 and 2 <= y <= 6:
        # torn chest: ribs behind
        if x in (2, 5) and y in (2, 6):
            return None
        return R_BONE[3] if y % 2 == 0 else R_SHROUD[0]
    if face in ("right", "left", "front", "back") and (x * 3 + y) % 7 == 0:
        return 2  # stitched seams
    return None


def _sleeve(seed):
    rag = ragged(seed, 2)

    def fn(face, x, y, w, h):
        if face == "top":
            return 4
        return rag(face, x, y, w, h)
    return fn


def _forearm(face, x, y, w, h):
    if face != "top" and face != "bottom" and y % 3 == 2:
        return 2  # knuckly joints
    return None


def _claws(face, x, y, w, h):
    if face == "top":
        return "clear"
    if face in ("front", "back") and x == 1 and y < h - 1:
        return R_SHROUD[0]  # gap between fingers
    if y == h - 1:
        return 1 if x % 2 == 0 else "clear"
    return None


def wraith():
    def tail(name, parent, piv, x, w, h, d, seed, rag=0):
        return Part(name, parent, piv, boxes=[Box(x, 0, -d / 2, w, h, d, R_SHROUD, base=3 - (seed % 2),
                                                  paint=chain(ragged(seed, rag)) if rag else _robe if seed == 99 else None, seed=seed)])

    return Rig("wraith", 64, 64, [
        Part("body", None, (0, -4, 0), (0.12, 0, 0), boxes=[Box(-4, 0, -2.5, 8, 10, 5, R_SHROUD, paint=_robe, seed=21)]),
        Part("head", "body", (0, 0, -0.5), boxes=[Box(-3, -6.5, -3, 6, 6, 6, R_BONE, paint=_skull, seed=22, grain=0.08)]),
        Part("hood", "head", (0, 0, 0), boxes=[Box(-4, -8, -4, 8, 8, 8, R_HOOD, paint=_hood, inflate=0.4, seed=23)]),
        Part("hood_tip", "hood", (0, -7.5, 3.5), (-0.7, 0, 0), boxes=[Box(-1.5, -1, 0, 3, 2, 4, R_HOOD, seed=24)]),
        Part("mantle", "body", (0, 0, 0), boxes=[Box(-6, -0.5, -3.5, 12, 3, 7, R_HOOD, base=3, paint=_mantle, seed=25)]),
        Part("right_arm", "body", (-5.5, 1, 0), boxes=[Box(-1.5, -1, -1.5, 3, 9, 3, R_SHROUD, paint=_sleeve(26), seed=26)]),
        Part("right_forearm", "right_arm", (0, 7.5, 0), boxes=[Box(-1, 0, -1, 2, 8, 2, R_BONE, paint=_forearm, seed=27)]),
        Part("right_claws", "right_forearm", (0, 8, 0), boxes=[Box(-1, 0, -1, 2, 4, 2, R_BONE, base=2, paint=_claws, seed=28)]),
        Part("left_arm", "body", (5.5, 1, 0), boxes=[Box(-1.5, -1, -1.5, 3, 9, 3, R_SHROUD, paint=_sleeve(29), seed=29)]),
        Part("left_forearm", "left_arm", (0, 7.5, 0), boxes=[Box(-1, 0, -1, 2, 8, 2, R_BONE, paint=_forearm, seed=30)]),
        Part("left_claws", "left_forearm", (0, 8, 0), boxes=[Box(-1, 0, -1, 2, 4, 2, R_BONE, base=2, paint=_claws, seed=31)]),
        tail("tail1", "body", (0, 10, 0), -3.5, 7, 6, 4, 32),
        tail("tail2", "tail1", (0, 6, 0), -2.5, 5, 6, 3, 33),
        tail("tail3", "tail2", (0, 6, 0), -1.5, 3, 7, 2, 34, rag=4),
    ])


# ================================================================== CINDER CROW
def _crow_body(face, x, y, w, h):
    if face == "bottom" and hash2(x, y, 41) < 0.25:
        return ("glow", EMBER[3])  # smouldering underside
    if face in ("left", "right") and y >= h - 2 and x % 2:
        return 1  # layered feather tips
    if face == "top" and x in (0, w - 1):
        return 4  # blue sheen
    return None


def _crow_head(face, x, y, w, h):
    if face in ("left", "right") and y == 1 and x == (1 if face == "right" else 2):
        return ("glow", EMBER[5])
    if face == "front" and y == 1 and x in (0, 3):
        return ("glow", EMBER[5])
    return None


def _wing(seed, tip=False):
    def fn(face, x, y, w, h):
        if face in ("top", "bottom"):
            # feather rows along the span; ragged trailing edge
            if y == h - 1 and hash2(x, 1, seed) < (0.6 if tip else 0.3):
                return "clear"
            if face == "bottom" and hash2(x, y, seed) < 0.12:
                return ("glow", EMBER[2])
            if x % 2 == 0:
                return 2 if face == "top" else 1
            return 4 if face == "top" and y < 2 else None
        return None
    return fn


def _tail(face, x, y, w, h):
    if face in ("top", "bottom"):
        if y == h - 1 and x % 2:
            return "clear"
        if x % 2 == 0:
            return 2
    return None


def _crest(face, x, y, w, h):
    if y == 0 and x % 2:
        return "clear"
    return None


def crow():
    return Rig("crow", 64, 32, [
        Part("body", None, (0, 17, 0), (-0.35, 0, 0), boxes=[Box(-2.5, 0, -3.5, 5, 5, 7, R_FEATHER, paint=_crow_body, seed=40)]),
        Part("head", None, (0, 16.5, -3), boxes=[Box(-2, -4, -2.5, 4, 4, 4, R_FEATHER, paint=_crow_head, seed=42)]),
        Part("beak", "head", (0, -2, -2.5), boxes=[Box(-1, -1, -3, 2, 1, 3, R_BEAK, base=4, seed=43),
                                                   Box(-0.5, 0, -2, 1, 1, 2, R_BEAK, base=2, seed=44)]),
        Part("crest", "head", (0, -4, 0), (-0.5, 0, 0), boxes=[Box(0, -3, -1, 0, 3, 4, R_FEATHER, paint=_crest, seed=45)]),
        Part("left_wing", None, (2.5, 17, -2.5), boxes=[Box(0, 0, 0, 6, 1, 6, R_FEATHER, paint=_wing(46), seed=46)]),
        Part("left_wing_tip", "left_wing", (6, 0, 0), boxes=[Box(0, 0, 0, 6, 1, 7, R_FEATHER, base=2, paint=_wing(47, True), seed=47)]),
        Part("right_wing", None, (-2.5, 17, -2.5), boxes=[Box(-6, 0, 0, 6, 1, 6, R_FEATHER, paint=_wing(48), seed=48)]),
        Part("right_wing_tip", "right_wing", (-6, 0, 0), boxes=[Box(-6, 0, 0, 6, 1, 7, R_FEATHER, base=2, paint=_wing(49, True), seed=49)]),
        Part("tail", None, (0, 19.5, 3.5), (0.55, 0, 0), boxes=[Box(-2.5, 0, 0, 5, 1, 6, R_FEATHER, paint=_tail, seed=50)]),
        Part("left_leg", None, (1, 21, 0.5), boxes=[Box(-0.5, 0, -0.5, 1, 3, 1, R_BEAK, base=2, seed=51),
                                                    Box(-1, 3, -2, 2, 0, 3, R_BEAK, base=2, seed=52)]),
        Part("right_leg", None, (-1, 21, 0.5), boxes=[Box(-0.5, 0, -0.5, 1, 3, 1, R_BEAK, base=2, seed=53),
                                                      Box(-1, 3, -2, 2, 0, 3, R_BEAK, base=2, seed=54)]),
    ])


# ================================================================== THE CRATOMANCER
def _rivets(face, x, y, w, h, every=4):
    if face in ("front", "back", "left", "right") and (x % every == 1) and y in (1, h - 2):
        return 5
    return None


def _chest(face, x, y, w, h):
    if face == "front":
        if x in (0, w - 1) or y == 0:
            return 4
        if x == w // 2 - 3 or x == w // 2 + 2:
            return 1  # plate seams
        if y == h - 1:
            return 1
        if (x, y) in ((2, 8), (3, 9), (9, 7), (10, 8)):
            return R_RUST[3]  # rust weeps
    if face == "back" and 2 <= y <= 7 and x in (3, 8):
        return R_RUST[2]
    return _rivets(face, x, y, w, h)


def _belt(face, x, y, w, h):
    if face == "front" and 3 <= x <= 5 and y == 1:
        return R_BRASS[4]
    if y == 1 and x % 3 == 0 and face != "top":
        return R_BRASS[2]
    return None


def _core(face, x, y, w, h):
    if face == "front":
        if x in (1, 3) or y == 2:
            return R_IRON[1]  # grille bars
        return ("glow", EMBER[6] if (x, y) == (2, 1) else EMBER[5] if abs(x - 2) + abs(y - 2) <= 2 else EMBER[4])
    return None


def _helm(face, x, y, w, h):
    if face == "front":
        if y == 3 and 1 <= x <= 6:
            return ("glow", EMBER[5] if x in (2, 5) else EMBER[3])  # visor slit
        if y in (2, 4) and 1 <= x <= 6:
            return R_IRON[0]
        if y == 0 or x in (0, 7):
            return 4
    if face == "top" and x in (3, 4):
        return 1
    return _rivets(face, x, y, w, h, 3)


def _stack(face, x, y, w, h):
    if face == "top":
        return ("glow", EMBER[2] if x == 1 or y == 1 else R_IRON[0])
    if face != "bottom" and (y + x) % 5 == 0:
        return R_RUST[3]
    if face != "bottom" and y < 3:
        return R_IRON[1] if (x + y) % 2 else R_IRON[0]  # soot
    return None


def _stack_cap(face, x, y, w, h):
    if face == "top":
        return "clear" if 1 <= x <= 2 and 1 <= y <= 2 else 2
    return 2


def _mortar(face, x, y, w, h):
    if face == "top":
        return R_IRON[0] if 1 <= x <= 2 and 1 <= y <= 2 else 4
    if face != "bottom" and y in (1, 2):
        return R_RUST[3] if y == 1 else R_RUST[2]
    if face != "bottom" and y == h - 3:
        return R_BRASS[3]
    return None


def _gauntlet(face, x, y, w, h):
    if face == "bottom":
        return 1
    if face == "front" and y >= h - 3 and x % 2 == 1:
        return 5  # knuckle studs
    if face != "top" and y == 0:
        return R_RUST[3]
    if face != "top" and y == 3:
        return 1
    return _rivets(face, x, y, w, h, 3)


def _pauldron(face, x, y, w, h):
    if face == "top":
        if x in (2, 5) and y in (2, 5):
            return 6  # studs
        return None
    if face != "bottom" and y == h - 1:
        return R_RUST[3]
    return None


def _boss_leg(face, x, y, w, h):
    if y >= 7:
        if face == "bottom":
            return R_IRON[0]
        if y == 7:
            return R_IRON[5]
        return R_IRON[3] if y < h - 1 else R_IRON[1]
    if face != "top" and y == 2 and x % 2:
        return R_LEATHER[2]
    return None


def _boss_arm(face, x, y, w, h):
    if face != "top" and face != "bottom" and y in (2, 6):
        return R_LEATHER[2]
    return None


def _crest_paint(face, x, y, w, h):
    if face in ("left", "right") and y == 0 and x % 2:
        return "clear"
    return None


def cratomancer():
    return Rig("cratomancer", 128, 64, [
        Part("body", None, (0, 1, 0), (0.25, 0, 0), boxes=[
            Box(-6, -2, -3.5, 12, 11, 7, R_IRON, paint=_chest, seed=60),
            Box(-4.5, 9, -3, 9, 3, 6, R_BOSSCOAT, paint=_belt, seed=61)]),
        Part("core", "body", (0, 0, 0), boxes=[Box(-2.5, 1.5, -4.5, 5, 5, 1, R_IRON, paint=_core, seed=62)]),
        Part("head", "body", (0, -2, -2), boxes=[Box(-4, -8, -4.5, 8, 8, 8, R_IRON, paint=_helm, seed=63)]),
        Part("snout", "head", (0, -2.5, -4.5), boxes=[Box(-2, -1.5, -2, 4, 4, 2, R_RUBBER, seed=64),
                                                      Box(-1.5, -1, -3.5, 3, 3, 2, R_OLIVE, seed=65)]),
        Part("canisters", "head", (0, 0, 0), boxes=[Box(-6, -5, -3, 2, 3, 3, R_OLIVE, seed=66),
                                                    Box(4, -5, -3, 2, 3, 3, R_OLIVE, seed=67)]),
        Part("crest", "head", (0, -8, 0), boxes=[Box(-0.5, -2, -4.5, 1, 2, 9, R_RUST, paint=_crest_paint, seed=68)]),
        Part("stack_left", "body", (3, -1, 3), (-0.15, 0, 0.08), boxes=[
            Box(-1.5, -10, -1.5, 3, 10, 3, R_IRON, paint=_stack, seed=69),
            Box(-2, -11, -2, 4, 1, 4, R_IRON, paint=_stack_cap, seed=70)]),
        Part("stack_right", "body", (-3, -1, 3), (-0.15, 0, -0.08), boxes=[
            Box(-1.5, -8, -1.5, 3, 8, 3, R_IRON, paint=_stack, seed=71),
            Box(-2, -9, -2, 4, 1, 4, R_IRON, paint=_stack_cap, seed=72)]),
        Part("mortar", "body", (-5.5, -2, 0.5), (-0.35, 0, -0.3), boxes=[Box(-2, -10, -2, 4, 10, 4, R_IRON, paint=_mortar, seed=73)]),
        Part("right_arm", None, (-7.5, 2, -1), boxes=[Box(-3, -2, -3, 5, 9, 6, R_BOSSCOAT, paint=_boss_arm, seed=74)]),
        Part("right_gauntlet", "right_arm", (-0.5, 7, 0), boxes=[Box(-3.5, 0, -3.5, 7, 9, 7, R_IRON, paint=_gauntlet, seed=75)]),
        Part("left_arm", None, (7.5, 2, -1), boxes=[Box(-2, -2, -3, 5, 9, 6, R_BOSSCOAT, paint=_boss_arm, seed=76)]),
        Part("left_gauntlet", "left_arm", (0.5, 7, 0), boxes=[Box(-3.5, 0, -3.5, 7, 9, 7, R_IRON, paint=_gauntlet, seed=77)]),
        Part("left_pauldron", "left_arm", (0.5, -2, 0), (0, 0, 0.2), boxes=[Box(-3.5, -3, -4, 8, 5, 8, R_IRON, paint=_pauldron, seed=78)]),
        Part("right_leg", None, (-3.5, 12, 0.5), boxes=[Box(-3, 0, -3, 6, 12, 6, R_BOSSCOAT, paint=_boss_leg, seed=79)]),
        Part("left_leg", None, (3.5, 12, 0.5), boxes=[Box(-3, 0, -3, 6, 12, 6, R_BOSSCOAT, paint=_boss_leg, seed=80)]),
    ])


RIGS = [soldier, wraith, crow, cratomancer]
TEXTURE = {"soldier": "hollow_soldier", "wraith": "ash_wraith", "crow": "cinder_crow", "cratomancer": "cratomancer"}


def java_source(package):
    rigs = [r() for r in RIGS]
    body = "\n\n".join(r.java() for r in rigs)
    return f"""package {package};

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated by tools/rigs.py. Edit the spec there and rerun tools/textures.py. */
public final class RigLayers {{
{body}

    private RigLayers() {{}}
}}
"""


if __name__ == "__main__":
    print(java_source("com.lach.affermath.client"))
