import json
import shutil
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src" / "generated" / "resources"
ASSETS = OUT / "assets" / "thaumcraft"
DATA = OUT / "data"
NS = "thaumcraft"

INFUSED_COLORS = [0xFFFFFF, 0xFFFF7E, 0xFF5A01, 0x0090FF, 0x00A000, 0xAA33FC, 0xB0B0BC]
FOLIAGE_DEFAULT = 0x48B518
SILVERWOOD_COLOR = 0x8899AA

block_tags = defaultdict(set)
item_tags = defaultdict(set)


SOURCE_TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / NS / "textures"


def copy_texture(source, target):
    destination = ASSETS / "textures" / target
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(SOURCE_TEXTURES / source, destination)


def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2) + "\n", encoding="utf-8")


def ref(name, kind="block"):
    return f"{NS}:{kind}/{name}"


def tint(color):
    value = color | 0xFF000000
    if value >= 0x80000000:
        value -= 0x100000000
    return {"type": "minecraft:constant", "value": value}


def block_model(name, content):
    write(ASSETS / "models" / "block" / f"{name}.json", content)


def item_model(name, content):
    write(ASSETS / "models" / "item" / f"{name}.json", content)


def item_definition(name, model, tints=None):
    definition = {"type": "minecraft:model", "model": model}
    if tints:
        definition["tints"] = tints
    write(ASSETS / "items" / f"{name}.json", {"model": definition})


def blockstate(name, content):
    write(ASSETS / "blockstates" / f"{name}.json", content)


def simple_state(name, model=None):
    blockstate(name, {"variants": {"": {"model": model or ref(name)}}})


def cube_all(name, texture):
    block_model(name, {"parent": "minecraft:block/cube_all", "textures": {"all": ref(texture)}})


def generated_item(name, texture, tints=None, texture_kind="item"):
    item_model(name, {"parent": "minecraft:item/generated", "textures": {"layer0": ref(texture, texture_kind)}})
    item_definition(name, ref(name, "item"), tints)


def block_item(name, tints=None):
    item_definition(name, ref(name), tints)


def loot(name, pools):
    write(DATA / NS / "loot_table" / "blocks" / f"{name}.json", {
        "type": "minecraft:block",
        "pools": pools,
        "random_sequence": f"{NS}:blocks/{name}",
    })


def self_drop(name):
    loot(name, [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}],
        "condition": {"type": "minecraft:survives_explosion"},
    }])


def bonus_drop(block, item, silk_self, shear_self=False, base_max=1, explosion=True):
    children = []
    if silk_self or shear_self:
        terms = []
        if shear_self:
            terms.append("minecraft:tool/can_shear")
        if silk_self:
            terms.append("minecraft:tool/can_silk_touch")
        children.append({
            "type": "minecraft:item",
            "condition": terms[0] if len(terms) == 1 else {"type": "minecraft:any_of", "terms": terms},
            "name": f"{NS}:{block}",
        })
    modifiers = []
    if base_max > 1:
        modifiers.append({"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": base_max}})
    modifiers.append({
        "type": "minecraft:apply_bonus",
        "enchantment": "minecraft:fortune",
        "formula": "minecraft:uniform_bonus_count",
        "parameters": {"bonusMultiplier": 1},
    })
    if explosion:
        modifiers.append({"type": "minecraft:explosion_decay"})
    children.append({"type": "minecraft:item", "name": item, "modifier": modifiers})
    loot(block, [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": children}]}])


def shear_only(name):
    loot(name, [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:item",
            "condition": {"type": "minecraft:any_of", "terms": ["minecraft:tool/can_shear", "minecraft:tool/can_silk_touch"]},
            "name": f"{NS}:{name}",
        }],
    }])


def fuel(name, ticks):
    write(DATA / NS / "context_int_provider" / "cooking" / f"time_{name}.json", {
        "type": "minecraft:div",
        "left": ticks,
        "right": {
            "type": "minecraft:conditional",
            "condition": "minecraft:block/fast_cooking",
            "on_false": "minecraft:cooking/normal_burn_time_reduction_factor",
            "on_true": "minecraft:cooking/fast_burn_time_reduction_factor",
        },
    })


def mineable(tool, *blocks):
    for name in blocks:
        block_tags[f"minecraft:mineable/{tool}"].add(f"{NS}:{name}")


def world_blocks():
    for name, texture in [("cinnabar_ore", "cinnibar"), ("amber_ore", "amberore")]:
        cube_all(name, texture)
        simple_state(name)
        block_item(name)
    self_drop("cinnabar_ore")
    bonus_drop("amber_ore", f"{NS}:amber", silk_self=True)

    infused = ["air", "fire", "water", "earth", "vis", "dull"]
    for index, element in enumerate(infused, start=1):
        name = f"{element}_infused_stone"
        block_model(name, {
            "parent": "minecraft:block/block",
            "textures": {"particle": ref("infusedorestone"), "stone": ref("infusedorestone"), "ore": ref("infusedore")},
            "elements": [
                {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
                    face: {"texture": "#stone", "cullface": face} for face in ["down", "up", "north", "south", "west", "east"]
                }},
                {"from": [0, 0, 0], "to": [16, 16, 16], "light_emission": 10, "faces": {
                    face: {"texture": "#ore", "cullface": face, "tintindex": 0} for face in ["down", "up", "north", "south", "west", "east"]
                }},
            ],
        })
        simple_state(name)
        block_item(name, [tint(INFUSED_COLORS[index])])
        loot(name, [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:item",
                "name": f"{NS}:{element}_shard",
                "modifier": [
                    {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}},
                    {"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 1}},
                    {"type": "minecraft:explosion_decay"},
                ],
            }],
        }])
        mineable("pickaxe", name)

    for name, texture in [("amber_block", "amberblock"), ("amber_bricks", "amberbrick"), ("obsidian_tile", "obsidiantile")]:
        cube_all(name, texture)
        simple_state(name)
        block_item(name)
        self_drop(name)

    block_model("travel_paving_stone", {
        "parent": "minecraft:block/cube",
        "textures": {
            "particle": ref("travelstoneside"),
            "down": ref("infusionbase"),
            "up": "minecraft:block/chiseled_quartz_block_top",
            "north": ref("travelstoneside"),
            "south": ref("travelstoneside"),
            "west": ref("travelstoneside"),
            "east": ref("travelstoneside"),
        },
    })
    simple_state("travel_paving_stone")
    block_item("travel_paving_stone")
    self_drop("travel_paving_stone")

    totem_variants = {}
    for part, side in [("base", "obsidiantotembase"), ("shaded", "obsidiantotembaseshaded")]:
        model = f"obsidian_totem_{part}"
        block_model(model, {"parent": "minecraft:block/cube_column", "textures": {"end": ref("obsidiantile"), "side": ref(side)}})
        for carving in range(4):
            totem_variants[f"carving={carving},part={part}"] = {"model": ref(model)}
    sides = {"north": 2, "south": 3, "west": 4, "east": 5}
    for carving in range(4):
        model = f"obsidian_totem_carved_{carving}"
        textures = {"particle": ref("obsidiantile"), "down": ref("obsidiantile"), "up": ref("obsidiantile")}
        for face, side in sides.items():
            textures[face] = ref(f"obsidiantotem{(side + carving) % 4 + 1}")
        block_model(model, {"parent": "minecraft:block/cube", "textures": textures})
        totem_variants[f"carving={carving},part=carved"] = {"model": ref(model)}
    blockstate("obsidian_totem", {"variants": totem_variants})
    block_item("obsidian_totem")
    item_definition("obsidian_totem", ref("obsidian_totem_base"))
    self_drop("obsidian_totem")
    mineable("pickaxe", "cinnabar_ore", "amber_ore", "amber_block", "amber_bricks", "obsidian_totem", "obsidian_tile", "travel_paving_stone")

    for wood in ["greatwood", "silverwood"]:
        log = f"{wood}_log"
        block_model(log, {"parent": "minecraft:block/cube_column", "textures": {"end": ref(f"{wood}top"), "side": ref(f"{wood}side")}})
        block_model(f"{log}_horizontal", {"parent": "minecraft:block/cube_column_horizontal", "textures": {"end": ref(f"{wood}top"), "side": ref(f"{wood}side")}})
        blockstate(log, {"variants": {
            "axis=x": {"model": ref(f"{log}_horizontal"), "x": 90, "y": 90},
            "axis=y": {"model": ref(log)},
            "axis=z": {"model": ref(f"{log}_horizontal"), "x": 90},
        }})
        block_item(log)
        self_drop(log)
        mineable("axe", log)
        block_tags["minecraft:logs"].add(f"{NS}:{log}")

        leaves = f"{wood}_leaves"
        block_model(leaves, {"parent": "minecraft:block/leaves", "textures": {"all": ref(f"{wood}leaves")}})
        simple_state(leaves)
        color = FOLIAGE_DEFAULT if wood == "greatwood" else SILVERWOOD_COLOR
        block_item(leaves, [tint(color)])
        shear_only(leaves)
        mineable("hoe", leaves)
        block_tags["minecraft:leaves"].add(f"{NS}:{leaves}")

        sapling = f"{wood}_sapling"
        block_model(sapling, {"parent": "minecraft:block/cross", "textures": {"cross": ref(f"{wood}sapling")}})
        simple_state(sapling)
        generated_item(sapling, f"{wood}sapling", texture_kind="block")
        self_drop(sapling)

    for flower, texture, drop in [("shimmerleaf", "shimmerleaf", f"{NS}:quicksilver"), ("cinderpearl", "cinderpearl", "minecraft:blaze_powder")]:
        block_model(flower, {"parent": "minecraft:block/cross", "textures": {"cross": ref(texture)}})
        simple_state(flower)
        generated_item(flower, texture, texture_kind="block")
        bonus_drop(flower, drop, silk_self=False, shear_self=True, explosion=False)

    block_model("nitor", {"textures": {"particle": ref("nitor")}})
    simple_state("nitor")
    generated_item("nitor", "nitor")
    self_drop("nitor")


SIMPLE_ITEMS = {
    "alumentum": "alumentum",
    "thaumium_ingot": "thaumiumingot",
    "quicksilver": "quicksilver",
    "magic_tallow": "tallow",
    "zombie_brain": "brain",
    "amber": "amber",
    "enchanted_fabric": "cloth",
    "flux_filter": "filter",
    "knowledge_fragment": "knowledgefragment",
    "mirrored_glass": "mirrorglass",
    "tin_nugget": "nuggettin",
    "silver_nugget": "nuggetsilver",
    "lead_nugget": "nuggetlead",
    "quicksilver_drop": "nuggetquicksilver",
    "native_iron_cluster": "clusteriron",
    "native_copper_cluster": "clustercopper",
    "native_tin_cluster": "clustertin",
    "native_silver_cluster": "clustersilver",
    "native_lead_cluster": "clusterlead",
    "native_gold_cluster": "clustergold",
    "chicken_nugget": "nuggetchicken",
    "beef_nugget": "nuggetbeef",
    "pork_nugget": "nuggetpork",
    "triple_meat_treat": "tripletreat",
    "goggles_of_revealing": "gogglesrevealing",
    "thaumium_helmet": "thaumiumhelm",
    "thaumium_chestplate": "thaumiumchest",
    "thaumium_leggings": "thaumiumlegs",
    "thaumium_boots": "thaumiumboots",
    "robe_chestplate": "clothchest",
    "robe_leggings": "clothlegs",
    "robe_boots": "clothboots",
    "boots_traveller": "bootstraveler",
}

HANDHELD_ITEMS = {
    "thaumium_sword": "thaumiumsword",
    "thaumium_pickaxe": "thaumiumpick",
    "thaumium_axe": "thaumiumaxe",
    "thaumium_shovel": "thaumiumshovel",
    "thaumium_hoe": "thaumiumhoe",
    "elemental_sword": "elementalsword",
    "elemental_pickaxe": "elementalpick",
    "elemental_axe": "elementalaxe",
    "elemental_shovel": "elementalshovel",
    "elemental_hoe": "elementalhoe",
}

EQUIPMENT = {
    "thaumium": ("thaumium_1", "thaumium_2"),
    "robes": ("robes_1", "robes_2"),
    "goggles": ("goggles", None),
    "boots_traveller": ("bootstraveler", None),
}

TOOL_TAGS = {
    "sword": "minecraft:swords",
    "pickaxe": "minecraft:pickaxes",
    "axe": "minecraft:axes",
    "shovel": "minecraft:shovels",
    "hoe": "minecraft:hoes",
}

ARMOR_TAGS = {
    "helmet": "minecraft:head_armor",
    "chestplate": "minecraft:chest_armor",
    "leggings": "minecraft:leg_armor",
    "boots": "minecraft:foot_armor",
}


def items():
    for name, texture in SIMPLE_ITEMS.items():
        generated_item(name, texture)
    for index, element in enumerate(["air", "fire", "water", "earth", "vis", "dull"], start=1):
        generated_item(f"{element}_shard", "shard", [tint(INFUSED_COLORS[index])])
    for name, texture in HANDHELD_ITEMS.items():
        item_model(name, {"parent": "minecraft:item/handheld", "textures": {"layer0": ref(texture, "item")}})
        item_definition(name, ref(name, "item"))
        item_tags[TOOL_TAGS[name.split("_")[1]]].add(f"{NS}:{name}")
    for name in SIMPLE_ITEMS:
        suffix = name.split("_")[-1]
        if suffix in ARMOR_TAGS:
            item_tags[ARMOR_TAGS[suffix]].add(f"{NS}:{name}")
    item_tags["minecraft:head_armor"].add(f"{NS}:goggles_of_revealing")
    for asset, (outer, inner) in EQUIPMENT.items():
        layers = {"humanoid": [{"texture": f"{NS}:{asset}"}]}
        copy_texture(f"model/{outer}.png", f"entity/equipment/humanoid/{asset}.png")
        if inner:
            layers["humanoid_leggings"] = [{"texture": f"{NS}:{asset}"}]
            copy_texture(f"model/{inner}.png", f"entity/equipment/humanoid_leggings/{asset}.png")
        write(ASSETS / "equipment" / f"{asset}.json", {"layers": layers})
    fuel("alumentum", 6400)
    fuel("magical_log", 400)

    item_tags[f"{NS}:thaumium_tool_materials"].add(f"{NS}:thaumium_ingot")
    item_tags[f"{NS}:repairs_thaumium_armor"].add(f"{NS}:thaumium_ingot")
    item_tags[f"{NS}:repairs_special_armor"].add(f"{NS}:enchanted_fabric")
    item_tags["c:ingots/thaumium"].add(f"{NS}:thaumium_ingot")
    item_tags["c:ingots"].add(f"{NS}:thaumium_ingot")
    item_tags["c:gems/amber"].add(f"{NS}:amber")
    item_tags["c:gems"].add(f"{NS}:amber")
    for metal in ["tin", "silver", "lead"]:
        item_tags[f"c:nuggets/{metal}"].add(f"{NS}:{metal}_nugget")
        item_tags["c:nuggets"].add(f"{NS}:{metal}_nugget")
        item_tags[f"c:raw_materials/{metal}"].add(f"{NS}:native_{metal}_cluster")
    item_tags["c:nuggets/quicksilver"].add(f"{NS}:quicksilver_drop")
    item_tags["c:ores/cinnabar"].add(f"{NS}:cinnabar_ore")
    item_tags["c:ores/amber"].add(f"{NS}:amber_ore")
    block_tags["c:ores/cinnabar"].add(f"{NS}:cinnabar_ore")
    block_tags["c:ores/amber"].add(f"{NS}:amber_ore")
    block_tags["c:ores"].update({f"{NS}:cinnabar_ore", f"{NS}:amber_ore"})
    item_tags["c:ores"].update({f"{NS}:cinnabar_ore", f"{NS}:amber_ore"})


ELEMENTAL_TOOLS = [f"{NS}:elemental_{tool}" for tool in ["axe", "sword", "shovel", "pickaxe", "hoe"]]


def chest_loot(name, base_tables, elemental_chance):
    pools = [{"rolls": 1, "entries": [{"type": "minecraft:loot_table", "value": table}]} for table in base_tables]
    pools.append({
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": tool} for tool in ELEMENTAL_TOOLS],
        "condition": {"type": "minecraft:random_chance", "chance": elemental_chance},
    })
    write(DATA / NS / "loot_table" / "chests" / f"{name}.json", {"type": "minecraft:chest", "pools": pools, "random_sequence": f"{NS}:chests/{name}"})


def worldgen():
    write(DATA / NS / "worldgen" / "feature" / "world_generation.json", {"type": f"{NS}:world_generation"})
    write(DATA / NS / "worldgen" / "placed_feature" / "world_generation.json", {"feature": f"{NS}:world_generation", "placement": []})
    for dimension in ["overworld", "nether"]:
        write(DATA / NS / "neoforge" / "biome_modifier" / f"world_generation_{dimension}.json", {
            "type": "neoforge:add_features",
            "biomes": f"#minecraft:is_{dimension}",
            "features": f"{NS}:world_generation",
            "step": "top_layer_modification",
        })
    chest_loot("mound", ["minecraft:chests/simple_dungeon"], 1 / 20)
    chest_loot("hilltop_stones", ["minecraft:chests/simple_dungeon", "minecraft:chests/simple_dungeon"], 1 / 10)
    chest_loot("greatwood_spider_nest", ["minecraft:chests/simple_dungeon"], 1 / 15)


def write_tags():
    for tag, values in block_tags.items():
        namespace, path = tag.split(":")
        write(DATA / namespace / "tags" / "block" / f"{path}.json", {"replace": False, "values": sorted(values)})
    for tag, values in item_tags.items():
        namespace, path = tag.split(":")
        write(DATA / namespace / "tags" / "item" / f"{path}.json", {"replace": False, "values": sorted(values)})


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    world_blocks()
    items()
    worldgen()
    write_tags()
    count = sum(1 for _ in OUT.rglob("*.json"))
    print(f"generated {count} files")


if __name__ == "__main__":
    main()
