"""Item textures: the ascii sprites below, recoloured onto hue-shifted ramps, then lit
from the top-left with a coloured (not black) outline, like vanilla items."""
from PIL import Image

from palette import hexc, ramp

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

R_STEEL = ramp("4E5058", step=0.2)
R_EMBER = [hexc(h) for h in ("5A1E0E", "7E2C10", "A8401A", "D0601F", "EE8A2E", "FFB24A", "FFE08A")]
R_ASH = ramp("8E8C88", step=0.13)
R_SPECTRAL = ramp("8FA8B8", step=0.15)
R_SILVER = ramp("9EA2AA", step=0.15)
R_OLIVE = ramp("515A3A", step=0.18)
R_RUBBER = ramp("4A4842", step=0.2)
R_GLASS = ramp("8FB2BE", step=0.16)
R_STRING = ramp("6B5A44", step=0.18)
RAMPS = [R_STEEL, R_EMBER, R_ASH, R_SPECTRAL, R_SILVER, R_OLIVE, R_RUBBER, R_GLASS, R_STRING]

# sprites whose colours should be matched only against these ramps
PREFER = {
    "ash": [R_ASH], "spectral_ash": [R_SPECTRAL],
    "field_lantern": [R_OLIVE, R_EMBER, R_STEEL], "ration_tin": [R_OLIVE, R_STEEL, R_SILVER],
    "gas_mask": [R_RUBBER, R_GLASS, R_STEEL], "dog_tag": [R_SILVER], "dog_tags": [R_SILVER],
    "eye_of_the_cratomancer": [R_ASH, R_EMBER, R_STEEL], "shrapnel_charm": [R_STEEL, R_STRING, R_EMBER],
    "ash_igniter": [R_STEEL, R_EMBER],
}


def _snap(c, ramps):
    best = None
    for rmp in ramps:
        for i, rc in enumerate(rmp):
            d = sum((a - b) ** 2 for a, b in zip(c[:3], rc[:3]))
            if best is None or d < best[0]:
                best = (d, rmp, i)
    return best[1], best[2]


def render(name, rows, pal, ramps=None):
    rows = [row.ljust(16, ".")[:16] for row in rows]
    ramps = ramps or PREFER.get(name, RAMPS)
    grid = {}
    outline = set()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in ". " or pal.get(ch) is None:
                continue
            if ch == "k":
                outline.add((x, y))
            else:
                grid[(x, y)] = _snap(pal[ch], ramps)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def empty(p):
        return p not in grid

    for (x, y), (rmp, i) in grid.items():
        j = i
        lit = empty((x, y - 1)) or empty((x - 1, y))
        shadow = empty((x, y + 1)) or empty((x + 1, y))
        if lit and shadow:
            j = max(1, i - 1)  # thin lines (chains, strings) read darker
        elif lit and not shadow:
            j = min(6, i + 1)
        elif shadow and not lit:
            j = max(1, i - 1)
        if rmp is R_EMBER:
            j = i  # glow stays flat
        img.putpixel((x, y), rmp[j])
    for (x, y) in outline:
        near = [grid[(x + dx, y + dy)][0] for dx, dy in ((0, 1), (1, 0), (0, -1), (-1, 0), (1, 1), (-1, -1), (1, -1), (-1, 1))
                if (x + dx, y + dy) in grid]
        near = [n for n in near if n is not R_EMBER] or near
        rmp = max(set(map(id, near)), key=lambda k: sum(1 for n in near if id(n) == k)) if near else None
        rmp = next((n for n in near if id(n) == rmp), R_STEEL)
        img.putpixel((x, y), rmp[0])
    return img


def all_items():
    return {name: render(name, rows, pal) for name, (rows, pal) in ITEMS.items()}


R_RUST = ramp("7A4A2E", step=0.2)
R_SLUDGE = ramp("6F7C26", step=0.14)
ITEMS.update({
    "scrap": (["................", "................", "................", "........kk......", ".......kmlk.....",
               "...kk.kmhllk....", "..kllkmmhlmk....", "..klhlkmmmdk.kk.", "...kmmlkddk.kmlk", "....kmmdkk..kldk",
               "...kmrrmmdk..kk.", "..kmrrrmmmdk....", "..kdmrmmmddk....", "...kkddddkk.....", ".....kkkk.......",
               "................"],
              {"k": hexc("1A1A1C"), "d": hexc("4A3020"), "m": hexc("7A4A2E"), "l": hexc("9A6A46"),
               "h": hexc("B8B8B4"), "r": hexc("A8401A")}),
    "toxic_sludge": (["................", "................", "................", "................", "................",
                      "................", "......kkk.......", "....kkhlkkk.....", "...khllmmllk....", "..khlmmdmmmlk...",
                      ".khlmmmmmdmmlk..", ".kmmdmmmmmmdmk..", "..kkmmmdmmmkk...", "....kkkkkkk.....", "................",
                      "................"],
                     {"k": hexc("2A3010"), "d": hexc("5A6A1A"), "m": hexc("8A9A2E"), "l": hexc("AABA4A"), "h": hexc("D8E07A")}),
})
PREFER.update({"scrap": [R_RUST, R_STEEL, R_SILVER], "toxic_sludge": [R_SLUDGE]})
