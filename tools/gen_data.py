#!/usr/bin/env python3
"""Writes Affermath's JSON assets and data (models, blockstates, loot, recipes, tags, worldgen, lang).
Run from the repo root: python3 tools/gen_data.py"""
import json, os

NS = "affermath"
RES = "src/main/resources"
A = f"{RES}/assets/{NS}"
D = f"{RES}/data/{NS}"


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def ns(x):
    return x if ":" in x else f"{NS}:{x}"


# ---------------------------------------------------------------- blocks
CUBES = ["ash_block", "ashen_soil", "rubble", "scorched_bricks", "cracked_scorched_bricks", "scorched_concrete",
         "cracked_scorched_concrete", "charred_planks", "sandbags"]
for b in CUBES:
    w(f"{A}/blockstates/{b}.json", {"variants": {"": {"model": f"{NS}:block/{b}"}}})
    w(f"{A}/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{b}"}})

# floor blocks: weighted random texture variants, each also randomly rotated so the ground never tiles
FLOOR_VARIANTS = {"ash_block": [16, 2, 2, 2, 1], "ashen_soil": [16, 2, 2, 1, 1, 1, 3, 1]}


def floor_variants(b, model_of):
    out = []
    for i, wt in enumerate(FLOOR_VARIANTS[b]):
        for rot in (0, 90, 180, 270):
            v = {"model": model_of(i)}
            if rot:
                v["y"] = rot
            v["weight"] = wt
            out.append(v)
    return out


for b, weights in FLOOR_VARIANTS.items():
    for i in range(1, len(weights)):
        w(f"{A}/models/block/{b}_{i + 1}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{b}_{i + 1}"}})
    w(f"{A}/blockstates/{b}.json", {"variants": {"": floor_variants(b, lambda i, b=b: f"{NS}:block/{b}" + (f"_{i + 1}" if i else ""))}})

# sandbags: top differs
w(f"{A}/models/block/sandbags.json", {"parent": "minecraft:block/cube_column",
                                      "textures": {"end": f"{NS}:block/sandbags_top", "side": f"{NS}:block/sandbags"}})

w(f"{A}/models/block/shattered_glass.json", {"parent": "minecraft:block/cube_all", "render_type": "minecraft:cutout",
                                             "textures": {"all": f"{NS}:block/shattered_glass"}})
w(f"{A}/blockstates/shattered_glass.json", {"variants": {"": {"model": f"{NS}:block/shattered_glass"}}})

# log
w(f"{A}/models/block/charred_log.json", {"parent": "minecraft:block/cube_column",
                                         "textures": {"end": f"{NS}:block/charred_log_top", "side": f"{NS}:block/charred_log"}})
w(f"{A}/models/block/charred_log_horizontal.json", {"parent": "minecraft:block/cube_column_horizontal",
                                                    "textures": {"end": f"{NS}:block/charred_log_top", "side": f"{NS}:block/charred_log"}})
w(f"{A}/blockstates/charred_log.json", {"variants": {
    "axis=y": {"model": f"{NS}:block/charred_log"},
    "axis=z": {"model": f"{NS}:block/charred_log_horizontal", "x": 90},
    "axis=x": {"model": f"{NS}:block/charred_log_horizontal", "x": 90, "y": 90}}})

# ash layers (like snow)
for h in range(1, 9):
    if h == 8:
        w(f"{A}/models/block/ash_layer_height16.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/ash_block"}})
    else:
        w(f"{A}/models/block/ash_layer_height{h*2}.json", {"parent": f"minecraft:block/snow_height{h*2}",
                                                          "textures": {"particle": f"{NS}:block/ash_block", "texture": f"{NS}:block/ash_block"}})
for i in range(1, len(FLOOR_VARIANTS["ash_block"])):
    for h in range(1, 8):
        w(f"{A}/models/block/ash_layer_height{h*2}_{i + 1}.json", {"parent": f"minecraft:block/snow_height{h*2}",
                                                                   "textures": {"particle": f"{NS}:block/ash_block_{i + 1}", "texture": f"{NS}:block/ash_block_{i + 1}"}})
w(f"{A}/blockstates/ash_layer.json", {"variants": {
    f"layers={h}": (floor_variants("ash_block", lambda i, h=h: f"{NS}:block/ash_block" + (f"_{i + 1}" if i else "")) if h == 8 else
                    floor_variants("ash_block", lambda i, h=h: f"{NS}:block/ash_layer_height{h*2}" + (f"_{i + 1}" if i else "")))
    for h in range(1, 9)}})
w(f"{A}/models/item/ash_layer.json", {"parent": f"{NS}:block/ash_layer_height2"})

# rebar (iron bars clone)
bars = {"post_ends": "iron_bars_post_ends", "post": "iron_bars_post", "cap": "iron_bars_cap", "cap_alt": "iron_bars_cap_alt",
        "side": "iron_bars_side", "side_alt": "iron_bars_side_alt"}
for k, parent in bars.items():
    w(f"{A}/models/block/rebar_{k}.json", {"parent": f"minecraft:block/{parent}", "render_type": "minecraft:cutout",
                                           "textures": {"particle": f"{NS}:block/rebar", "bars": f"{NS}:block/rebar", "edge": f"{NS}:block/rebar"}})
m = lambda k: f"{NS}:block/rebar_{k}"
w(f"{A}/blockstates/rebar.json", {"multipart": [
    {"apply": {"model": m("post_ends")}},
    {"when": {"north": "false", "west": "false", "south": "false", "east": "false"}, "apply": {"model": m("post")}},
    {"when": {"north": "true", "west": "false", "south": "false", "east": "false"}, "apply": {"model": m("cap")}},
    {"when": {"north": "false", "west": "false", "south": "false", "east": "true"}, "apply": {"model": m("cap"), "y": 90}},
    {"when": {"north": "false", "west": "false", "south": "true", "east": "false"}, "apply": {"model": m("cap_alt")}},
    {"when": {"north": "false", "west": "true", "south": "false", "east": "false"}, "apply": {"model": m("cap_alt"), "y": 90}},
    {"when": {"north": "true"}, "apply": {"model": m("side")}},
    {"when": {"east": "true"}, "apply": {"model": m("side"), "y": 90}},
    {"when": {"south": "true"}, "apply": {"model": m("side_alt")}},
    {"when": {"west": "true"}, "apply": {"model": m("side_alt"), "y": 90}}]})
w(f"{A}/models/item/rebar.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:block/rebar"}})

# portal
w(f"{A}/models/block/affermath_portal_ns.json", {"parent": "minecraft:block/nether_portal_ns", "render_type": "minecraft:translucent",
                                                 "textures": {"particle": f"{NS}:block/affermath_portal", "portal": f"{NS}:block/affermath_portal"}})
w(f"{A}/models/block/affermath_portal_ew.json", {"parent": "minecraft:block/nether_portal_ew", "render_type": "minecraft:translucent",
                                                 "textures": {"particle": f"{NS}:block/affermath_portal", "portal": f"{NS}:block/affermath_portal"}})
w(f"{A}/blockstates/affermath_portal.json", {"variants": {"axis=x": {"model": f"{NS}:block/affermath_portal_ns"},
                                                         "axis=z": {"model": f"{NS}:block/affermath_portal_ew"}}})
w(f"{A}/blockstates/field_light.json", {"variants": {"": {"model": "minecraft:block/air"}}})

for b in CUBES + ["shattered_glass", "charred_log"]:
    w(f"{A}/models/item/{b}.json", {"parent": f"{NS}:block/{b}"})

# ---------------------------------------------------------------- items
FLAT = ["ash", "spectral_ash", "warforged_plating", "warforged_upgrade_smithing_template", "ash_igniter",
        "eye_of_the_cratomancer", "dog_tag", "dog_tags", "shrapnel_charm", "field_lantern", "ration_tin", "gas_mask",
        "warforged_helmet", "warforged_chestplate", "warforged_leggings", "warforged_boots"]
for i in FLAT:
    w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{i}"}})
for t in ["sword", "pickaxe", "axe", "shovel", "hoe"]:
    w(f"{A}/models/item/warforged_{t}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/warforged_{t}"}})
for e in ["hollow_soldier", "ash_wraith", "cinder_crow", "cratomancer"]:
    w(f"{A}/models/item/{e}_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{e}_spawn_egg"}})

ALL_ITEMS = CUBES + ["shattered_glass", "charred_log", "ash_layer", "rebar"] + FLAT + [f"warforged_{t}" for t in ["sword", "pickaxe", "axe", "shovel", "hoe"]] \
    + [f"{e}_spawn_egg" for e in ["hollow_soldier", "ash_wraith", "cinder_crow", "cratomancer"]]
for i in ALL_ITEMS:
    w(f"{A}/items/{i}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{i}"}})
for mat in ["warforged", "gas_mask"]:
    layers = {"humanoid": [{"texture": f"{NS}:{mat}"}], "humanoid_baby": [{"texture": f"{NS}:{mat}"}]}
    if mat == "warforged":
        layers["humanoid_leggings"] = [{"texture": f"{NS}:{mat}"}]
    w(f"{A}/equipment/{mat}.json", {"layers": layers})
w(f"{D}/tags/item/warforged_repair.json", {"values": [ns("warforged_plating")]})

# particles
w(f"{A}/particles/ash_flake.json", {"textures": [f"{NS}:ash_flake_{i}" for i in range(3)]})
w(f"{A}/particles/ash_streak.json", {"textures": [f"{NS}:ash_streak_{i}" for i in range(2)]})

# ---------------------------------------------------------------- loot: blocks
def self_drop(b):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": ns(b)}],
                                                  "conditions": [{"condition": "minecraft:survives_explosion"}]}]}

for b in ["ash_block", "ashen_soil", "rubble", "scorched_bricks", "cracked_scorched_bricks", "scorched_concrete",
          "cracked_scorched_concrete", "charred_log", "charred_planks", "rebar", "sandbags"]:
    w(f"{D}/loot_table/blocks/{b}.json", self_drop(b))
w(f"{D}/loot_table/blocks/shattered_glass.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": ns("shattered_glass")}],
    "conditions": [{"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}]})
w(f"{D}/loot_table/blocks/ash_layer.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
    {"type": "minecraft:item", "name": ns("ash"), "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]})

# ---------------------------------------------------------------- loot: entities
def item(name, lo=1, hi=1, weight=1, looting=None, extra=None):
    e = {"type": "minecraft:item", "name": ns(name), "weight": weight}
    fns = []
    if (lo, hi) != (1, 1):
        fns.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if looting:
        fns.append({"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
                    "count": {"type": "minecraft:uniform", "min": 0, "max": looting}})
    if extra:
        fns += extra
    if fns:
        e["functions"] = fns
    return e

w(f"{D}/loot_table/entities/hollow_soldier.json", {"type": "minecraft:entity", "pools": [
    {"rolls": 1, "entries": [item("minecraft:rotten_flesh", 0, 2, looting=1)]},
    {"rolls": 1, "entries": [item("minecraft:iron_nugget", 1, 3)], "conditions": [{"condition": "minecraft:random_chance", "chance": 0.4}]},
    {"rolls": 1, "entries": [item("dog_tag")], "conditions": [{"condition": "minecraft:killed_by_player"},
        {"condition": "minecraft:random_chance_with_enchanted_bonus", "enchantment": "minecraft:looting",
         "unenchanted_chance": 0.12, "enchanted_chance": {"type": "minecraft:linear", "base": 0.16, "per_level_above_first": 0.04}}]},
    {"rolls": 1, "entries": [item("minecraft:gunpowder", 1, 2)], "conditions": [{"condition": "minecraft:random_chance", "chance": 0.15}]}]})
w(f"{D}/loot_table/entities/ash_wraith.json", {"type": "minecraft:entity", "pools": [
    {"rolls": 1, "entries": [item("spectral_ash", 1, 2, looting=1)], "conditions": [{"condition": "minecraft:killed_by_player"}]},
    {"rolls": 1, "entries": [item("ash", 1, 3)]}]})
w(f"{D}/loot_table/entities/cinder_crow.json", {"type": "minecraft:entity", "pools": [
    {"rolls": 1, "entries": [item("minecraft:feather", 0, 2, looting=1)]},
    {"rolls": 1, "entries": [item("ash")], "conditions": [{"condition": "minecraft:random_chance", "chance": 0.5}]}]})
WF = ["warforged_helmet", "warforged_chestplate", "warforged_leggings", "warforged_boots", "warforged_sword",
      "warforged_pickaxe", "warforged_axe", "warforged_shovel", "warforged_hoe"]
w(f"{D}/loot_table/entities/cratomancer.json", {"type": "minecraft:entity", "pools": [
    {"rolls": 1, "entries": [item("warforged_upgrade_smithing_template", 1, 2)]},
    {"rolls": 1, "entries": [item("warforged_plating", 4, 8, looting=2)]},
    {"rolls": 1, "entries": [item(x, weight=(3 if "hoe" not in x else 1)) for x in WF]},
    {"rolls": 1, "entries": [item("spectral_ash", 3, 6)]},
    {"rolls": 1, "entries": [item("dog_tags")], "conditions": [{"condition": "minecraft:random_chance", "chance": 0.5}]}]})

# ---------------------------------------------------------------- loot: chests
def pool(rolls, entries):
    return {"rolls": rolls, "entries": entries}

ruin = [pool({"type": "minecraft:uniform", "min": 3, "max": 6}, [
    item("minecraft:bread", 1, 3, 10), item("minecraft:cooked_beef", 1, 2, 5), item("minecraft:baked_potato", 1, 3, 6),
    item("minecraft:dried_kelp", 2, 6, 6), item("minecraft:iron_nugget", 2, 8, 10), item("minecraft:gunpowder", 1, 4, 8),
    item("minecraft:paper", 1, 4, 6), item("minecraft:string", 1, 3, 4), item("minecraft:leather", 1, 2, 4),
    item("minecraft:coal", 2, 6, 8), item("minecraft:charcoal", 1, 4, 5), item("minecraft:torch", 2, 6, 5),
    item("minecraft:iron_ingot", 1, 3, 4), item("minecraft:copper_ingot", 2, 5, 4), item("minecraft:arrow", 2, 8, 4),
    item("ash", 2, 6, 6), item("dog_tag", 1, 1, 6), item("minecraft:compass", 1, 1, 1), item("minecraft:map", 1, 1, 2),
    item("minecraft:name_tag", 1, 1, 1), item("minecraft:golden_apple", 1, 1, 1)]),
    pool(1, [{"type": "minecraft:empty", "weight": 70}, item("gas_mask", weight=8), item("ash_igniter", weight=4),
             item("field_lantern", weight=5), item("ration_tin", weight=5), item("shrapnel_charm", weight=4),
             item("dog_tags", weight=5)])]
w(f"{D}/loot_table/chests/ruin.json", {"type": "minecraft:chest", "pools": ruin})

bunker = [pool({"type": "minecraft:uniform", "min": 4, "max": 8}, [
    item("minecraft:cooked_beef", 2, 4, 8), item("minecraft:bread", 2, 5, 8), item("minecraft:golden_carrot", 1, 3, 3),
    item("minecraft:iron_ingot", 2, 6, 8), item("minecraft:gold_ingot", 1, 4, 5), item("minecraft:redstone", 3, 9, 5),
    item("minecraft:gunpowder", 2, 6, 8), item("minecraft:tnt", 1, 2, 2), item("minecraft:diamond", 1, 2, 2),
    item("minecraft:iron_chestplate", 1, 1, 2), item("minecraft:iron_helmet", 1, 1, 2), item("minecraft:crossbow", 1, 1, 2),
    item("minecraft:arrow", 4, 12, 5), item("spectral_ash", 1, 2, 3), item("dog_tag", 1, 2, 6),
    item("minecraft:experience_bottle", 1, 4, 3), item("minecraft:netherite_scrap", 1, 1, 1)]),
    pool(1, [{"type": "minecraft:empty", "weight": 40}, item("gas_mask", weight=8), item("field_lantern", weight=6),
             item("ration_tin", weight=6), item("shrapnel_charm", weight=6), item("dog_tags", weight=6)])]
w(f"{D}/loot_table/chests/bunker.json", {"type": "minecraft:chest", "pools": bunker})

vault = [pool({"type": "minecraft:uniform", "min": 4, "max": 7}, [
    item("minecraft:diamond", 1, 3, 6), item("minecraft:emerald", 2, 6, 6), item("minecraft:gold_ingot", 3, 8, 8),
    item("minecraft:netherite_scrap", 1, 2, 3), item("minecraft:netherite_ingot", 1, 1, 1),
    item("minecraft:enchanted_golden_apple", 1, 1, 1), item("minecraft:golden_apple", 1, 2, 4),
    item("minecraft:experience_bottle", 3, 8, 6), item("spectral_ash", 2, 5, 6), item("warforged_plating", 1, 2, 2)]),
    pool(1, [item("warforged_upgrade_smithing_template", weight=1), {"type": "minecraft:empty", "weight": 1}]),
    pool(1, [item("field_lantern"), item("ration_tin"), item("shrapnel_charm"), item("dog_tags")])]
w(f"{D}/loot_table/chests/bunker_vault.json", {"type": "minecraft:chest", "pools": vault})

# ---------------------------------------------------------------- recipes
def shaped(name, pattern, key, result, count=1):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                  "key": {k: (v if v.startswith("#") else ns(v)) for k, v in key.items()},
                                  "result": {"id": ns(result), "count": count}})

def shapeless(name, ings, result, count=1):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                  "ingredients": [(v if v.startswith("#") else ns(v)) for v in ings],
                                  "result": {"id": ns(result), "count": count}})

def smelt(name, ing, result, xp=0.1):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:smelting", "category": "misc", "ingredient": ns(ing),
                                  "result": {"id": ns(result)}, "experience": xp, "cookingtime": 200})

smelt("ash_from_charcoal", "minecraft:charcoal", "ash")
shapeless("ash_from_block", ["ash_block"], "ash", 4)
shaped("ash_block", ["AA", "AA"], {"A": "ash"}, "ash_block")
shaped("ash_layer", ["AAA"], {"A": "ash_block"}, "ash_layer", 6)
shaped("scorched_bricks", ["BBB", "BAB", "BBB"], {"B": "minecraft:bricks", "A": "ash"}, "scorched_bricks", 8)
smelt("cracked_scorched_bricks", "scorched_bricks", "cracked_scorched_bricks")
shaped("scorched_concrete", ["SSS", "SAS", "SSS"], {"S": "minecraft:smooth_stone", "A": "ash"}, "scorched_concrete", 8)
smelt("cracked_scorched_concrete", "scorched_concrete", "cracked_scorched_concrete")
shapeless("charred_planks", ["charred_log"], "charred_planks", 4)
shaped("rebar", ["I I", "I I", "I I"], {"I": "minecraft:iron_nugget"}, "rebar", 4)
shaped("sandbags", ["SLS", "LSL", "SLS"], {"S": "minecraft:sand", "L": "minecraft:string"}, "sandbags", 4)
shapeless("ash_igniter", ["minecraft:flint", "ash", "minecraft:gunpowder", "minecraft:iron_nugget"], "ash_igniter")
shaped("eye_of_the_cratomancer", ["SSS", "SES", "SSS"], {"S": "spectral_ash", "E": "minecraft:ender_eye"}, "eye_of_the_cratomancer")
shaped("gas_mask", [" L ", "GHG", " C "], {"L": "minecraft:leather", "G": "minecraft:glass_pane", "H": "minecraft:leather_helmet", "C": "minecraft:charcoal"}, "gas_mask")
shapeless("dog_tags", ["minecraft:iron_chain", "dog_tag"], "dog_tags")
shaped("warforged_upgrade_smithing_template", ["SPS", "STS", "SSS"],
       {"S": "spectral_ash", "P": "warforged_plating", "T": "warforged_upgrade_smithing_template"}, "warforged_upgrade_smithing_template", 2)
base = {"helmet": "netherite_helmet", "chestplate": "netherite_chestplate", "leggings": "netherite_leggings", "boots": "netherite_boots",
        "sword": "netherite_sword", "pickaxe": "netherite_pickaxe", "axe": "netherite_axe", "shovel": "netherite_shovel", "hoe": "netherite_hoe"}
for k, v in base.items():
    w(f"{D}/recipe/warforged_{k}_smithing.json", {"type": "minecraft:smithing_transform",
        "template": ns("warforged_upgrade_smithing_template"), "base": f"minecraft:{v}",
        "addition": ns("warforged_plating"), "result": {"id": ns(f"warforged_{k}"), "count": 1}})

# ---------------------------------------------------------------- tags
w(f"{D}/tags/block/portal_frame.json", {"values": [ns("scorched_bricks"), ns("cracked_scorched_bricks")]})
w(f"{RES}/data/minecraft/tags/block/mineable/pickaxe.json", {"values": [ns(x) for x in [
    "scorched_bricks", "cracked_scorched_bricks", "scorched_concrete", "cracked_scorched_concrete", "rebar"]]})
w(f"{RES}/data/minecraft/tags/block/mineable/shovel.json", {"values": [ns(x) for x in ["ash_layer", "ash_block", "ashen_soil", "rubble", "sandbags"]]})
w(f"{RES}/data/minecraft/tags/block/mineable/axe.json", {"values": [ns(x) for x in ["charred_log", "charred_planks"]]})
w(f"{RES}/data/minecraft/tags/block/dirt.json", {"values": [ns("ashen_soil")]})
w(f"{RES}/data/minecraft/tags/item/head_armor.json", {"values": [ns("gas_mask"), ns("warforged_helmet")]})
w(f"{RES}/data/minecraft/tags/item/chest_armor.json", {"values": [ns("warforged_chestplate")]})
w(f"{RES}/data/minecraft/tags/item/leg_armor.json", {"values": [ns("warforged_leggings")]})
w(f"{RES}/data/minecraft/tags/item/foot_armor.json", {"values": [ns("warforged_boots")]})
w(f"{RES}/data/minecraft/tags/item/swords.json", {"values": [ns("warforged_sword")]})
w(f"{RES}/data/minecraft/tags/item/pickaxes.json", {"values": [ns("warforged_pickaxe")]})
w(f"{RES}/data/minecraft/tags/item/axes.json", {"values": [ns("warforged_axe")]})
w(f"{RES}/data/minecraft/tags/item/shovels.json", {"values": [ns("warforged_shovel")]})
w(f"{RES}/data/minecraft/tags/item/hoes.json", {"values": [ns("warforged_hoe")]})
w(f"{RES}/data/minecraft/tags/item/trimmable_armor.json", {"values": [ns(x) for x in ["warforged_helmet", "warforged_chestplate", "warforged_leggings", "warforged_boots"]]})
w(f"{RES}/data/curios/tags/item/necklace.json", {"values": [ns("dog_tags")]})
w(f"{RES}/data/curios/tags/item/charm.json", {"values": [ns("shrapnel_charm")]})
w(f"{RES}/data/curios/tags/item/belt.json", {"values": [ns("field_lantern"), ns("ration_tin")]})
w(f"{D}/curios/entities/affermath.json", {"entities": ["player"], "slots": ["necklace", "charm", "belt"]})
w(f"{D}/tags/worldgen/structure/eye_of_the_cratomancer_located.json", {"values": [ns("bunker")]})
BIOMES = ["ash_wastes", "crater_fields", "dead_forest"]
w(f"{D}/tags/worldgen/biome/has_ruin.json", {"values": [ns(b) for b in BIOMES]})
w(f"{D}/tags/worldgen/biome/has_bunker.json", {"values": [ns(b) for b in BIOMES]})

# ---------------------------------------------------------------- worldgen
w(f"{D}/dimension_type/affermath.json", {
    "has_skylight": True, "has_ceiling": False, "has_ender_dragon_fight": False, "has_fixed_time": True,
    "coordinate_scale": 1.0, "ambient_light": 0.15, "logical_height": 256, "min_y": 0, "height": 256,
    "infiniburn": "#minecraft:infiniburn_overworld", "skybox": "none",
    "monster_spawn_light_level": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7},
    "monster_spawn_block_light_limit": 0,
    "attributes": {
        "minecraft:audio/ambient_sounds": {"loop": "minecraft:ambient.basalt_deltas.loop",
            "mood": {"sound": "minecraft:ambient.basalt_deltas.mood", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}},
        "minecraft:gameplay/bed_rule": {"can_set_spawn": "never", "can_sleep": "never", "explodes": False},
        "minecraft:gameplay/respawn_anchor_works": False,
        "minecraft:gameplay/monsters_burn": False,
        "minecraft:visual/fog_color": "#8e8d8b",
        "minecraft:visual/sky_color": "#9a9997",
        "minecraft:visual/fog_start_distance": 1.0,
        "minecraft:visual/fog_end_distance": 34.0,
        "minecraft:visual/sky_fog_end_distance": 34.0,
        "minecraft:visual/cloud_color": "#00ffffff",
        "minecraft:visual/sunrise_sunset_color": "#00000000",
        "minecraft:visual/star_brightness": 0.0,
        "minecraft:visual/ambient_light_color": "#1a1a1a",
        "minecraft:visual/water_fog_color": "#3a3b3c"}})

def noise(n, xz, y=0.0):
    return {"type": "minecraft:noise", "noise": n, "xz_scale": xz, "y_scale": y}

def add(a, b):
    return {"type": "minecraft:add", "argument1": a, "argument2": b}

def mul(a, b):
    return {"type": "minecraft:mul", "argument1": a, "argument2": b}

grad = {"type": "minecraft:y_clamped_gradient", "from_y": 40, "to_y": 128, "from_value": 1.0, "to_value": -1.0}
hills = add(mul(0.38, noise("minecraft:continentalness", 0.6)), mul(0.12, noise("minecraft:erosion", 2.0)))
initial = add(grad, hills)
final = {"type": "minecraft:interpolated", "argument": add(initial, mul(0.06, noise("minecraft:surface", 3.0, 3.0)))}
zero = 0.0
w(f"{D}/worldgen/noise_settings/affermath.json", {
    "sea_level": 60, "disable_mob_generation": False, "aquifers_enabled": False, "ore_veins_enabled": False,
    "legacy_random_source": False, "default_block": {"Name": "minecraft:stone"},
    "default_fluid": {"Name": "minecraft:water", "Properties": {"level": "0"}},
    "noise": {"min_y": 0, "height": 256, "size_horizontal": 1, "size_vertical": 2},
    "noise_router": {"barrier": zero, "fluid_level_floodedness": zero, "fluid_level_spread": zero, "lava": zero,
                     "temperature": noise("minecraft:temperature", 1.2), "vegetation": noise("minecraft:vegetation", 1.2),
                     "continents": zero, "erosion": zero, "depth": zero, "ridges": zero,
                     "preliminary_surface_level": {"type": "minecraft:find_top_surface", "cell_height": 8, "lower_bound": 0, "upper_bound": 200.0, "density": initial}, "final_density": final,
                     "vein_toggle": zero, "vein_ridged": zero, "vein_gap": zero},
    "spawn_target": [],
    "surface_rule": {"type": "minecraft:sequence", "sequence": [
        {"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor",
                                                    "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:bedrock"}}},
        {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor", "add_surface_depth": False, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:sequence", "sequence": [
             {"type": "minecraft:condition", "if_true": {"type": "minecraft:water", "offset": -1, "surface_depth_multiplier": 0, "add_stone_depth": False},
              "then_run": {"type": "minecraft:sequence", "sequence": [
                  {"type": "minecraft:condition", "if_true": {"type": "minecraft:biome", "biome_is": [ns("crater_fields")]},
                   "then_run": {"type": "minecraft:condition", "if_true": {"type": "minecraft:noise_threshold", "noise": "minecraft:surface", "min_threshold": 0.15, "max_threshold": 9},
                                "then_run": {"type": "minecraft:block", "result_state": {"Name": ns("rubble")}}}},
                  {"type": "minecraft:block", "result_state": {"Name": ns("ashen_soil")}}]}},
             {"type": "minecraft:block", "result_state": {"Name": ns("rubble")}}]}},
        {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor", "add_surface_depth": True, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": ns("ashen_soil")}}},
        {"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": f"{NS}:deep", "true_at_and_below": {"absolute": 24}, "false_at_and_above": {"absolute": 40}},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:deepslate", "Properties": {"axis": "y"}}}}]}})

w(f"{RES}/data/{NS}/dimension/affermath.json", {"type": f"{NS}:affermath", "generator": {
    "type": "minecraft:noise", "settings": f"{NS}:affermath",
    "biome_source": {"type": "minecraft:multi_noise", "biomes": [
        {"biome": ns("ash_wastes"), "parameters": {"temperature": [-1.0, 0.1], "humidity": [-1.0, 1.0], "continentalness": 0, "erosion": 0, "weirdness": 0, "depth": 0, "offset": 0}},
        {"biome": ns("crater_fields"), "parameters": {"temperature": [0.1, 1.0], "humidity": [-1.0, 0.25], "continentalness": 0, "erosion": 0, "weirdness": 0, "depth": 0, "offset": 0}},
        {"biome": ns("dead_forest"), "parameters": {"temperature": [0.1, 1.0], "humidity": [0.25, 1.0], "continentalness": 0, "erosion": 0, "weirdness": 0, "depth": 0, "offset": 0}}]}}})

def feature(name, ftype, placement):
    w(f"{D}/worldgen/configured_feature/{name}.json", {"type": ns(ftype), "config": {}})
    w(f"{D}/worldgen/placed_feature/{name}.json", {"feature": ns(name), "placement": placement})

surf = [{"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]
feature("crater", "crater", [{"type": "minecraft:rarity_filter", "chance": 10}] + surf)
feature("crater_dense", "crater", [{"type": "minecraft:rarity_filter", "chance": 2}] + surf)
feature("charred_tree", "charred_tree", [{"type": "minecraft:rarity_filter", "chance": 3}] + surf)
feature("charred_tree_dense", "charred_tree", [{"type": "minecraft:count", "count": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 5}}] + surf)
feature("rubble_pile", "rubble_pile", [{"type": "minecraft:count", "count": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 2}}] + surf)
feature("ash_blanket", "ash_blanket", [{"type": "minecraft:biome"}])

ORES = ["minecraft:ore_coal_upper", "minecraft:ore_coal_lower", "minecraft:ore_iron_middle", "minecraft:ore_iron_small",
        "minecraft:ore_copper", "minecraft:ore_gold", "minecraft:ore_redstone", "minecraft:ore_lapis", "minecraft:ore_diamond"]

def biome(name, local, vegetal, fog=0x8E8D8B, sky=0x9A9997, monsters=None):
    feats = [[] for _ in range(11)]
    feats[2] = local
    feats[4] = [ns("rubble_pile")]
    feats[6] = ORES
    feats[9] = vegetal
    feats[10] = [ns("ash_blanket")]
    w(f"{D}/worldgen/biome/{name}.json", {
        "has_precipitation": False, "temperature": 0.8, "downfall": 0.0,
        "attributes": {"minecraft:visual/fog_color": "#%06x" % fog, "minecraft:visual/sky_color": "#%06x" % sky,
                       "minecraft:visual/water_fog_color": "#3a3b3c"},
        "effects": {"water_color": "#5b5d5e", "grass_color": "#77766f", "foliage_color": "#5e5d57", "dry_foliage_color": "#5e5d57"},
        "spawners": {"monster": monsters or [
            {"type": ns("hollow_soldier"), "weight": 60, "minCount": 2, "maxCount": 4},
            {"type": ns("ash_wraith"), "weight": 25, "minCount": 1, "maxCount": 1},
            {"type": "minecraft:skeleton", "weight": 10, "minCount": 1, "maxCount": 2},
            {"type": "minecraft:spider", "weight": 8, "minCount": 1, "maxCount": 1}],
            "ambient": [{"type": ns("cinder_crow"), "weight": 10, "minCount": 1, "maxCount": 3}],
            "creature": [], "water_creature": [], "water_ambient": [], "underground_water_creature": [], "axolotls": [], "misc": []},
        "spawn_costs": {}, "carvers": [], "features": feats})


biome("ash_wastes", [ns("crater")], [ns("charred_tree")])
biome("crater_fields", [ns("crater_dense")], [ns("charred_tree")], fog=0x878582)
biome("dead_forest", [ns("crater")], [ns("charred_tree_dense")], fog=0x86847F)

w(f"{D}/worldgen/structure/ruin.json", {"type": ns("ruin"), "biomes": f"#{NS}:has_ruin", "step": "surface_structures",
                                         "spawn_overrides": {}, "terrain_adaptation": "beard_thin"})
w(f"{D}/worldgen/structure/bunker.json", {"type": ns("bunker"), "biomes": f"#{NS}:has_bunker", "step": "underground_structures",
                                           "spawn_overrides": {}, "terrain_adaptation": "none"})
w(f"{D}/worldgen/structure_set/ruins.json", {"structures": [{"structure": ns("ruin"), "weight": 1}],
                                             "placement": {"type": "minecraft:random_spread", "spacing": 7, "separation": 3, "salt": 61817224}})
w(f"{D}/worldgen/structure_set/bunkers.json", {"structures": [{"structure": ns("bunker"), "weight": 1}],
                                               "placement": {"type": "minecraft:random_spread", "spacing": 36, "separation": 14, "salt": 90443517}})
print("ok")
