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
WOOL_COLORS = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]
WOOL_TINTS = [
    0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
    0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B,
]

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


FACING_ROTATION = {"north": 0, "east": 90, "south": 180, "west": 270}


def facing_state(name, model=None):
    blockstate(name, {"variants": {
        f"facing={facing}": {"model": model or ref(name), "y": rotation} if rotation else {"model": model or ref(name)}
        for facing, rotation in FACING_ROTATION.items()
    }})


def uv_rect(u1, v1, u2, v2, tex_w, tex_h):
    return [u1 * 16 / tex_w, v1 * 16 / tex_h, u2 * 16 / tex_w, v2 * 16 / tex_h]


def model_box(start, end, tex_offset, size, tex_size, texture="#texture", mirror=True, rotated=False):
    u, v = tex_offset
    dx, dy, dz = size
    tex_w, tex_h = tex_size

    def face(u1, v1, u2, v2, rotation=0):
        result = {"uv": uv_rect(u1, v1, u2, v2, tex_w, tex_h), "texture": texture}
        if rotation:
            result["rotation"] = rotation
        return result

    top = face(u + dz, v, u + dz + dx, v + dz, 90 if rotated else 0)
    bottom = face(u + dz + dx, v, u + dz + 2 * dx, v + dz, 90 if rotated else 0)
    front = face(u + dz, v + dz, u + dz + dx, v + dz + dy)
    back = face(u + 2 * dz + dx, v + dz, u + 2 * dz + 2 * dx, v + dz + dy)
    right = face(u + dz + dx, v + dz, u + 2 * dz + dx, v + dz + dy)
    left = face(u, v + dz, u + dz, v + dz + dy)
    if mirror:
        right, left = left, right
    if rotated:
        faces = {"up": top, "down": bottom, "east": front, "west": back, "north": right, "south": left}
    else:
        faces = {"up": top, "down": bottom, "south": front, "north": back, "east": right, "west": left}
    return {"from": list(start), "to": list(end), "faces": faces}


def plane(start, end, direction, texture):
    return {"from": list(start), "to": list(end), "faces": {direction: {"uv": [0, 0, 16, 16], "texture": texture}}}


def crucible():
    inset = 0.123 * 16
    sides = {direction: {"texture": "#side", "cullface": direction} for direction in ["north", "south", "east", "west"]}
    block_model("crucible", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ref("metalbase"),
            "top": ref("crucible1"),
            "bottom": ref("crucible2"),
            "side": ref("crucible3"),
            "inner": ref("crucible5"),
            "floor": ref("crucible6"),
        },
        "elements": [
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
                "up": {"texture": "#top", "cullface": "up"},
                "down": {"texture": "#bottom", "cullface": "down"},
                **sides,
            }},
            plane([inset, 0, 0], [inset, 16, 16], "east", "#inner"),
            plane([16 - inset, 0, 0], [16 - inset, 16, 16], "west", "#inner"),
            plane([0, 0, inset], [16, 16, inset], "south", "#inner"),
            plane([0, 0, 16 - inset], [16, 16, 16 - inset], "north", "#inner"),
            plane([0, 4, 0], [16, 4, 16], "up", "#floor"),
        ],
    })
    block_model("crucible_inventory", {
        "parent": "minecraft:block/cube",
        "textures": {
            "particle": ref("metalbase"),
            "up": ref("crucible4"),
            "down": ref("crucible2"),
            "north": ref("crucible3"),
            "south": ref("crucible3"),
            "east": ref("crucible3"),
            "west": ref("crucible3"),
        },
    })
    simple_state("crucible")
    item_definition("crucible", ref("crucible_inventory"))
    self_drop("crucible")
    mineable("pickaxe", "crucible")


def alembic():
    tex = (64, 32)
    copy_texture("model/alembic.png", "block/alembic_model.png")
    block_model("alembic", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ref("goldbase"), "texture": ref("alembic_model")},
        "elements": [
            model_box([7, 6, -5], [9, 19, -3], (56, 0), (2, 13, 2), tex),
            model_box([7, 4, 7], [9, 19, 9], (56, 0), (2, 15, 2), tex),
            model_box([4, 0, 4], [12, 4, 12], (0, 20), (8, 4, 8), tex),
            model_box([6, 16, -3], [10, 20, 7], (36, 24), (10, 4, 4), tex, rotated=True),
            model_box([4, 4, 4], [12, 14, 12], (0, 0), (8, 10, 8), tex),
        ],
    })
    facing_state("alembic")
    block_item("alembic")
    self_drop("alembic")
    mineable("pickaxe", "alembic")


def tables():
    copy_texture("model/table.png", "block/table_model.png")
    copy_texture("model/worktable.png", "block/worktable_model.png")
    tex = (64, 32)
    block_model("table", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ref("woodplain"), "texture": ref("table_model")},
        "elements": [
            model_box([0, 12, 0], [16, 16, 16], (0, 0), (16, 4, 16), tex),
            model_box([10, 4, 6], [14, 12, 10], (0, 20), (4, 8, 4), tex),
            model_box([2, 4, 6], [6, 12, 10], (0, 20), (4, 8, 4), tex),
            model_box([0, 0, 4], [16, 4, 12], (16, 20), (16, 4, 8), tex),
        ],
    })
    blockstate("table", {"variants": {
        "axis=z": {"model": ref("table")},
        "axis=x": {"model": ref("table"), "y": 90},
    }})
    block_item("table")
    self_drop("table")
    tex = (128, 64)
    legs = [([11, 4, 11], [15, 8, 15]), ([1, 4, 1], [5, 8, 5]), ([11, 4, 1], [15, 8, 5]), ([1, 4, 11], [5, 8, 15])]
    block_model("arcane_worktable", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ref("woodplain"), "texture": ref("worktable_model")},
        "elements": [
            model_box([0, 8, 0], [16, 16, 16], (0, 0), (16, 8, 16), tex),
            model_box([0, 0, 0], [16, 4, 16], (0, 32), (16, 4, 16), tex),
            *[model_box(start, end, (72, 0), (4, 4, 4), tex) for start, end in legs],
        ],
    })
    simple_state("arcane_worktable")
    block_item("arcane_worktable")
    self_drop("arcane_worktable")
    mineable("axe", "table", "arcane_worktable", "research_table")
    research_table()


def research_table():
    copy_texture("model/restable.png", "block/restable_model.png")
    copy_texture("misc/parchment.png", "block/parchment.png")
    tex = (128, 64)
    block_model("research_table", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ref("woodplain"),
            "texture": ref("restable_model"),
            "parchment": ref("parchment"),
            "quill": ref("tablequill"),
        },
        "elements": [
            model_box([0, 12, 0], [32, 16, 16], (0, 0), (32, 4, 16), tex),
            model_box([2, 0, 10], [6, 12, 14], (0, 24), (4, 12, 4), tex),
            model_box([2, 0, 2], [6, 12, 6], (0, 24), (4, 12, 4), tex),
            model_box([26, 0, 10], [30, 12, 14], (0, 24), (4, 12, 4), tex),
            model_box([26, 0, 2], [30, 12, 6], (0, 24), (4, 12, 4), tex),
            model_box([4, 2, 6], [28, 6, 10], (24, 24), (24, 4, 4), tex),
            model_box([2, 16, 2], [5, 18, 5], (0, 44), (3, 2, 3), tex),
            {
                "from": [13.6, 16.16, 2.4],
                "to": [23.2, 16.16, 12],
                "rotation": {"origin": [13.6, 16.16, 2.4], "axis": "y", "angle": -15},
                "faces": {"up": {"uv": [0, 0, 16, 16], "texture": "#parchment"}},
            },
            {
                "from": [5.6, 17.6, 2.72],
                "to": [5.6, 25.6, 10.72],
                "rotation": {"origin": [5.6, 17.6, 10.72], "axis": "y", "angle": 15},
                "faces": {
                    "east": {"uv": [16, 0, 0, 16], "texture": "#quill"},
                    "west": {"uv": [0, 0, 16, 16], "texture": "#quill"},
                },
            },
        ],
    })
    block_model("research_table_side", {"textures": {"particle": ref("woodplain")}})
    rotations = {"east": 0, "south": 90, "west": 180, "north": 270}
    variants = {}
    for facing, angle in rotations.items():
        main = {"model": ref("research_table")}
        if angle:
            main["y"] = angle
        variants[f"facing={facing},part=main"] = main
        variants[f"facing={facing},part=side"] = {"model": ref("research_table_side")}
    blockstate("research_table", {"variants": variants})
    loot("research_table", [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"{NS}:table"}],
        "condition": {"type": "minecraft:survives_explosion"},
    }])


ARCANE_STONE_PARTS = {
    "origin": ("infusion1", {"south": 5, "west": 5, "north": 6, "east": 6}),
    "x": ("infusion2", {"north": 5, "south": 5, "west": 6, "east": 6}),
    "z": ("infusion3", {"north": 5, "south": 5, "west": 6, "east": 6}),
    "xz": ("infusion4", {"north": 5, "east": 5, "south": 6, "west": 6}),
}


def arcane_stone():
    cube_all("arcane_stone", "infusionbase")
    variants = {"part=none": {"model": ref("arcane_stone")}}
    glow = 0.02 * 16
    for part, (top, sides) in ARCANE_STONE_PARTS.items():
        faces = {"up": {"texture": "#top", "cullface": "up"}, "down": {"texture": "#top", "cullface": "down"}}
        for direction, index in sides.items():
            faces[direction] = {"texture": f"#side{index}", "cullface": direction}
        name = f"arcane_stone_{part}"
        block_model(name, {
            "parent": "minecraft:block/block",
            "textures": {
                "particle": ref("infusionbase"),
                "top": ref(top),
                "side5": ref("infusion5"),
                "side6": ref("infusion6"),
                "glow": ref("animatedglow"),
            },
            "elements": [
                {
                    "from": [glow, glow, glow],
                    "to": [16 - glow, 16 - glow, 16 - glow],
                    "light_emission": 11,
                    "shade": False,
                    "faces": {
                        direction: {"texture": "#glow", "tintindex": 0}
                        for direction in ["up", "down", "north", "south", "east", "west"]
                    },
                },
                {"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces},
            ],
        })
        variants[f"part={part}"] = {"model": ref(name)}
    blockstate("arcane_stone", {"variants": variants})
    block_item("arcane_stone")
    self_drop("arcane_stone")
    mineable("pickaxe", "arcane_stone")


WANDS = {"wand_apprentice": "wandapprentice", "wand_adept": "wandadept", "wand_thaumaturge": "wandthaumaturge"}


def full_cube(texture, tintindex=None):
    face = {"texture": texture}
    if tintindex is not None:
        face["tintindex"] = tintindex
    return {
        "from": [0, 0, 0],
        "to": [16, 16, 16],
        "faces": {direction: {**face, "cullface": direction} for direction in DIRECTIONS},
    }


DIRECTIONS = ["down", "up", "north", "south", "west", "east"]
PERPENDICULAR = {
    "down": ["north", "south", "west", "east"],
    "up": ["north", "south", "west", "east"],
    "north": ["down", "up", "west", "east"],
    "south": ["down", "up", "west", "east"],
    "west": ["down", "up", "north", "south"],
    "east": ["down", "up", "north", "south"],
}
RUNE_SLABS = {
    "down": ([0, 0, 0], [16, 3, 16]),
    "up": ([0, 13, 0], [16, 16, 16]),
    "north": ([0, 0, 0], [16, 16, 3]),
    "south": ([0, 0, 13], [16, 16, 16]),
    "west": ([0, 0, 0], [3, 16, 16]),
    "east": ([13, 0, 0], [16, 16, 16]),
}


def rune_strip(direction):
    start, end = RUNE_SLABS[direction]
    return {
        "from": start,
        "to": end,
        "faces": {face: {"texture": "#rune", "cullface": face} for face in PERPENDICULAR[direction]},
    }


def candle_drips(seed):
    import random
    rng = random.Random(seed)
    drips = []
    for index in range(1 + rng.randrange(5)):
        side = rng.random() < 0.5
        loc = 2 + rng.randrange(2)
        height = 1 + rng.randrange(3)
        if index % 2 == 0:
            start = [5 + loc, 0, 5 if side else 10]
            end = [6 + loc, height, 6 if side else 11]
        else:
            start = [5 if side else 10, 0, 5 + loc]
            end = [6 if side else 11, height, 6 + loc]
        drips.append({
            "from": start,
            "to": end,
            "faces": {direction: {"texture": "#candle", "tintindex": 0} for direction in DIRECTIONS},
        })
    return drips


def candle_elements(drips):
    column = {
        "from": [6, 0, 6],
        "to": [10, 8, 10],
        "faces": {direction: {"texture": "#candle", "tintindex": 0} for direction in DIRECTIONS},
    }
    wick = {
        "from": [7.6, 8, 7.6],
        "to": [8.4, 10, 8.4],
        "faces": {direction: {"texture": "#stub"} for direction in DIRECTIONS if direction != "down"},
    }
    return [column, *drips, wick]


def wards_and_decor():
    cube_all("arcane_wood", "arcanewoodblock")
    simple_state("arcane_wood")
    block_item("arcane_wood")
    self_drop("arcane_wood")
    mineable("axe", "arcane_wood")
    block_tags["minecraft:beacon_base_blocks"].add(f"{NS}:arcane_wood")

    block_model("warded_stone", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ref("wardedstone"), "all": ref("wardedstone")},
        "elements": [full_cube("#all", 0)],
    })
    for color, value in zip(WOOL_COLORS, WOOL_TINTS):
        name = f"{color}_warded_stone"
        simple_state(name, ref("warded_stone"))
        item_definition(name, ref("warded_stone"), [tint(value)])
        self_drop(name)
        mineable("pickaxe", name)

    glass_textures = {"particle": ref("wardedglass"), "all": ref("wardedglass"), "rune": ref("wardedglassrune")}
    block_model("warded_glass", {"parent": "minecraft:block/block", "textures": glass_textures, "elements": [full_cube("#all")]})
    for direction in DIRECTIONS:
        block_model(f"warded_glass_rune_{direction}", {"textures": glass_textures, "elements": [rune_strip(direction)]})
    blockstate("warded_glass", {"multipart": [
        {"apply": {"model": ref("warded_glass")}},
        *[
            {"when": {direction: "false"}, "apply": {"model": ref(f"warded_glass_rune_{direction}")}}
            for direction in DIRECTIONS
        ],
    ]})
    item_model("warded_glass", {
        "parent": "minecraft:block/block",
        "textures": glass_textures,
        "elements": [full_cube("#all"), *[rune_strip(direction) for direction in DIRECTIONS]],
    })
    item_definition("warded_glass", ref("warded_glass", "item"))
    mineable("pickaxe", "warded_glass")

    candle_textures = {"particle": ref("candle"), "candle": ref("candle"), "stub": ref("candlestub")}
    for variant in range(4):
        block_model(f"tallow_candle_{variant}", {
            "parent": "minecraft:block/block",
            "textures": candle_textures,
            "elements": candle_elements(candle_drips(variant * 7919 + 13)),
        })
    item_model("tallow_candle", {"parent": "minecraft:block/block", "textures": candle_textures, "elements": candle_elements([])})
    for color, value in zip(WOOL_COLORS, WOOL_TINTS):
        name = f"{color}_tallow_candle"
        blockstate(name, {"variants": {"": [{"model": ref(f"tallow_candle_{variant}")} for variant in range(4)]}})
        item_definition(name, ref("tallow_candle", "item"), [tint(value)])
        self_drop(name)

    block_model("marker", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ref("marker"), "base": ref("marker"), "inset": ref("markerinset")},
        "elements": [full_cube("#base"), full_cube("#inset", 0)],
    })
    for color, value in zip(WOOL_COLORS, WOOL_TINTS):
        name = f"{color}_marker"
        simple_state(name, ref("marker"))
        item_definition(name, ref("marker"), [tint(value)])
        self_drop(name)
        mineable("axe", name)


def special_item(name, base, model):
    write(ASSETS / "items" / f"{name}.json", {"model": {"type": "minecraft:special", "base": base, "model": model}})


def jars():
    block_model("jar", {"textures": {"particle": "minecraft:block/glass"}})
    item_model("jar", {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/glass"}})
    for name, brain in [("warded_jar", False), ("brain_jar", True)]:
        simple_state(name, ref("jar"))
        special_item(name, ref("jar", "item"), {"type": f"{NS}:jar", "brain": brain})
    special_item("filled_jar", ref("jar", "item"), {"type": f"{NS}:jar"})
    loot("warded_jar", [])
    self_drop("brain_jar")


def bellows():
    block_model("arcane_bellows", {"textures": {"particle": ref("woodplain")}})
    simple_state("arcane_bellows")
    item_model("arcane_bellows", {"parent": "minecraft:block/block", "textures": {"particle": ref("woodplain")}})
    special_item("arcane_bellows", ref("arcane_bellows", "item"), {"type": f"{NS}:bellows"})
    self_drop("arcane_bellows")
    mineable("axe", "arcane_bellows")


FURNACE_FACINGS = {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}
FURNACE_GRATE_ROTATION = {"east": 0, "south": 90, "west": 180, "north": 270}
SIDE_NAMES = ["down", "up", "north", "south", "west", "east"]


def furnace_world(facing):
    fx, fz = FURNACE_FACINGS[facing]
    world = {}
    for y in range(3):
        for z in range(3):
            for x in range(3):
                if (x, y, z) == (1, 2, 1):
                    continue
                meta = z * 3 + x + 1
                if (x, y, z) == (1, 1, 1):
                    meta = 0
                if (x, y, z) == (1 + fx, 1, 1 + fz):
                    meta = 10
                world[(x, y, z)] = meta
    return world


def furnace_texture(world, x, y, z, side):
    def block(dx, dy, dz):
        return world.get((x + dx, y + dy, z + dz))

    def grate(dx, dy, dz):
        return block(dx, dy, dz) == 10

    meta = world[(x, y, z)]
    meta_above = block(0, 1, 0)
    meta_below = block(0, -1, 0)
    has_above = meta_above is not None
    has_below = meta_below is not None
    if meta_above in (10, 0):
        meta_above = meta
    if meta_below in (10, 0):
        meta_below = meta
    if meta == meta_above and meta == meta_below and has_above and has_below:
        level = 9
    elif meta != meta_above or not has_above or (meta == meta_below and has_below):
        level = 0
    else:
        level = 18

    touching = (
        (side > 3 and (grate(0, 0, 1) or grate(0, 0, -1)))
        or (1 < side < 4 and (grate(1, 0, 0) or grate(-1, 0, 0)))
        or (side > 1 and (grate(0, 1, 0) or grate(0, -1, 0)))
        or (side > 3 and (grate(0, 1, 1) or grate(0, 1, -1)))
        or (1 < side < 4 and (grate(1, 1, 0) or grate(-1, 1, 0)))
        or (side > 3 and (grate(0, -1, 1) or grate(0, -1, -1)))
        or (1 < side < 4 and (grate(1, -1, 0) or grate(-1, -1, 0)))
        or (side == 0 and grate(0, -1, 0))
        or (side == 1 and grate(0, 1, 0))
    )
    add = 3 if touching else 0
    if side in (0, 1):
        if add != 3:
            return 7 if meta == 5 else (meta - 1) % 3 + (meta - 1) // 3 * 9
        return 6
    rows = {2: {1: 2, 2: 1, 3: 0}, 3: {7: 0, 8: 1, 9: 2}, 4: {1: 0, 4: 1, 7: 2}, 5: {3: 2, 6: 1, 9: 0}}
    column = rows[side].get(meta)
    if column is None:
        return 7
    return column + level + add


def infernal_furnace():
    block_model("infernal_furnace_lava", {"parent": "minecraft:block/cube_all", "textures": {"all": "minecraft:block/lava_still"}})
    block_model("infernal_furnace_grate", {
        "parent": "minecraft:block/block",
        "textures": {"particle": ref("furnace13"), "bars": ref("furnace13"), "inner": ref("furnace15"), "fire": "minecraft:block/fire_0"},
        "elements": [
            {"from": [6, 0, 0], "to": [6, 16, 16], "shade": False, "faces": {"east": {"uv": [0, 0, 16, 16], "texture": "#bars"}}},
            {"from": [3.2, 0, 0], "to": [3.2, 16, 16], "shade": False, "faces": {"east": {"uv": [0, 0, 16, 16], "texture": "#inner"}}},
            {"from": [1.6, 0, 0], "to": [1.6, 24, 16], "shade": False, "faces": {"east": {"uv": [0, 0, 16, 16], "texture": "#fire"}}},
        ],
    })
    block_model("infernal_furnace", {"parent": "minecraft:block/cube_all", "textures": {"all": "minecraft:block/obsidian"}})
    variants = {}
    for facing in FURNACE_FACINGS:
        world = furnace_world(facing)
        for y in range(3):
            for z in range(3):
                for x in range(3):
                    key = f"facing={facing},x={x},y={y},z={z}"
                    meta = world.get((x, y, z))
                    if meta is None:
                        variants[key] = {"model": ref("infernal_furnace")}
                    elif meta == 0:
                        variants[key] = {"model": ref("infernal_furnace_lava")}
                    elif meta == 10:
                        rotation = FURNACE_GRATE_ROTATION[facing]
                        variants[key] = {"model": ref("infernal_furnace_grate"), **({"y": rotation} if rotation else {})}
                    else:
                        name = f"infernal_furnace_{facing}_{x}{y}{z}"
                        faces = {}
                        for side, direction in enumerate(SIDE_NAMES):
                            texture = furnace_texture(world, x, y, z, side)
                            faces[direction] = {"texture": f"#t{texture}", "cullface": direction}
                        textures = {f"t{face['texture'][2:]}": ref(f"furnace{face['texture'][2:]}") for face in faces.values()}
                        textures["particle"] = "minecraft:block/obsidian"
                        block_model(name, {
                            "parent": "minecraft:block/block",
                            "textures": textures,
                            "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}],
                        })
                        variants[key] = {"model": ref(name)}
    blockstate("infernal_furnace", {"variants": variants})
    mineable("pickaxe", "infernal_furnace")
    block_tags[f"{NS}:portable_hole_blacklist"].add(f"{NS}:infernal_furnace")


def devices():
    wards_and_decor()
    jars()
    bellows()
    infernal_furnace()
    crucible()
    alembic()
    tables()
    arcane_stone()
    for name, texture in WANDS.items():
        item_model(name, {"parent": "minecraft:item/handheld", "textures": {"layer0": ref(texture, "item")}})
        item_definition(name, ref(name, "item"))
    generated_item("thaumonomicon", "thaumonomicon")
    generated_item("thaumonomicon_cheat", "thaumonomiconcheat")
    generated_item("scribing_tools", "inkwell")
    item_model("thaumometer", {"parent": "minecraft:item/generated", "textures": {"layer0": ref("thaumometerring", "item"), "layer1": ref("thaumometercore", "item")}})
    item_definition("thaumometer", ref("thaumometer", "item"))
    for name, texture in [("research_notes", "researchnotes"), ("discovery", "discovery")]:
        item_model(name, {"parent": "minecraft:item/generated", "textures": {"layer0": ref(texture, "item"), "layer1": ref(texture + "overlay", "item")}})
        item_definition(name, ref(name, "item"), [tint(0xFFFFFF), {"type": f"{NS}:research_note"}])
    generated_item("essentia_phial", "phial")
    item_model("essence", {"parent": "minecraft:item/generated", "textures": {"layer0": ref("phial", "item"), "layer1": ref("essence", "item")}})
    item_definition("essence", ref("essence", "item"), [tint(0xFFFFFF), {"type": f"{NS}:essence"}])
    block_tags[f"{NS}:crucible_heaters"].update({"minecraft:lava", "#minecraft:fire", f"{NS}:nitor"})
    block_tags[f"{NS}:crucible_bellows"].add(f"{NS}:arcane_bellows")


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




def mod_ids(values):
    found = set()
    for value in values:
        if isinstance(value, str) and value.startswith(f"{NS}:"):
            found.add(value)
    return found


def recipe(name, content, referenced):
    conditions = [{"type": "neoforge:registered", "value": item} for item in sorted(mod_ids(referenced))]
    if conditions:
        content = {"neoforge:conditions": conditions, **content}
    write(DATA / NS / "recipe" / f"{name}.json", content)


def shaped(name, result, count, pattern, key, category="misc", components=None):
    content = {"type": "minecraft:crafting_shaped", "category": category, "key": key, "pattern": pattern, "result": {"id": result}}
    if components:
        content["result"]["components"] = components
    if count > 1:
        content["result"]["count"] = count
    recipe(name, content, [result, *key.values()])


def shapeless(name, result, count, ingredients, category="misc"):
    content = {"type": "minecraft:crafting_shapeless", "category": category, "ingredients": ingredients, "result": {"id": result}}
    if count > 1:
        content["result"]["count"] = count
    recipe(name, content, [result, *ingredients])


def smelting(name, ingredient, result, count, experience):
    content = {"type": "minecraft:smelting", "cookingtime": 200, "experience": experience, "ingredient": ingredient, "result": {"id": result}}
    if count > 1:
        content["result"]["count"] = count
    recipe(name, content, [ingredient, result])


def recipes():
    tc = lambda name: f"{NS}:{name}"
    for color in WOOL_COLORS:
        shaped(f"{color}_marker", tc(f"{color}_marker"), 2, [" W ", "WSW", " W "], {"W": "#minecraft:planks", "S": f"minecraft:{color}_wool"})
        item_tags[f"{NS}:tallow_candles"].add(tc(f"{color}_tallow_candle"))
    shaped("amber_block", tc("amber_block"), 1, ["##", "##"], {"#": tc("amber")}, "building")
    shaped("amber_bricks", tc("amber_bricks"), 4, ["##", "##"], {"#": tc("amber_block")}, "building")
    shaped("obsidian_tile", tc("obsidian_tile"), 4, ["##", "##"], {"#": "minecraft:obsidian"}, "building")
    shapeless("amber_from_block", tc("amber"), 4, [tc("amber_block")])
    shapeless("amber_from_bricks", tc("amber"), 4, [tc("amber_bricks")])
    shaped("research_notes_from_fragments", tc("research_notes"), 1, ["KKK", "KKK", "KKK"], {"K": tc("knowledge_fragment")})
    shaped("essentia_phial", tc("essentia_phial"), 8, [" C ", "G G", " G "], {"G": "minecraft:glass", "C": "minecraft:clay_ball"})
    shaped("table", tc("table"), 1, ["SSS", "W W"], {"S": "#minecraft:wooden_slabs", "W": "#minecraft:planks"})
    shaped("wand_apprentice", tc("wand_apprentice"), 1, ["  C", " S ", "G  "], {"C": f"#{NS}:shards", "G": "minecraft:gold_nugget", "S": "minecraft:stick"}, "equipment", {f"{NS}:wand_vis": 0})
    shaped("quicksilver", tc("quicksilver"), 1, ["###", "###", "###"], {"#": tc("quicksilver_drop")})
    armor = {
        "helm": ("thaumium_helmet", ["III", "I I"]),
        "chest": ("thaumium_chestplate", ["I I", "III", "III"]),
        "legs": ("thaumium_leggings", ["III", "I I", "I I"]),
        "feet": ("thaumium_boots", ["I I", "I I"]),
    }
    for name, (item, pattern) in armor.items():
        shaped(f"thaumium_{name}", tc(item), 1, pattern, {"I": tc("thaumium_ingot")}, "equipment")
    tools = {
        "shovel": ["I", "S", "S"],
        "pickaxe": ["III", " S ", " S "],
        "axe": ["II", "SI", "S "],
        "hoe": ["II", "S ", "S "],
        "sword": ["I", "I", "S"],
    }
    for name, pattern in tools.items():
        shaped(f"thaumium_{name}", tc(f"thaumium_{name}"), 1, pattern, {"I": tc("thaumium_ingot"), "S": "minecraft:stick"}, "equipment")
    shapeless("scribing_tools_from_phial", tc("scribing_tools"), 1, [tc("essentia_phial"), "minecraft:feather", "minecraft:ink_sac"])
    shapeless("scribing_tools", tc("scribing_tools"), 1, ["minecraft:glass_bottle", "minecraft:feather", "minecraft:ink_sac"])
    shapeless("scribing_tools_refill", tc("scribing_tools"), 1, [tc("scribing_tools"), "minecraft:ink_sac"])
    shapeless("triple_meat_treat", tc("triple_meat_treat"), 1, [tc("chicken_nugget"), tc("beef_nugget"), tc("pork_nugget"), "minecraft:sugar"])
    smelting("quicksilver_from_cinnabar", tc("cinnabar_ore"), tc("quicksilver"), 1, 1.0)
    smelting("amber_from_ore", tc("amber_ore"), tc("amber"), 1, 1.0)
    smelting("charcoal_from_greatwood", tc("greatwood_log"), "minecraft:charcoal", 1, 0.5)
    smelting("charcoal_from_silverwood", tc("silverwood_log"), "minecraft:charcoal", 1, 0.5)
    smelting("iron_from_cluster", tc("native_iron_cluster"), "minecraft:iron_ingot", 2, 1.0)
    smelting("gold_from_cluster", tc("native_gold_cluster"), "minecraft:gold_ingot", 2, 1.0)
    smelting("copper_from_cluster", tc("native_copper_cluster"), "minecraft:copper_ingot", 2, 1.0)
    for shard in ["air", "fire", "water", "earth", "vis", "dull"]:
        item_tags[f"{NS}:shards"].add(tc(f"{shard}_shard"))


def tag_entries(values):
    return [{"id": value, "required": False} if value.startswith(f"{NS}:") else value for value in sorted(values)]


def write_tags():
    for tag, values in block_tags.items():
        namespace, path = tag.split(":")
        write(DATA / namespace / "tags" / "block" / f"{path}.json", {"replace": False, "values": tag_entries(values)})
    for tag, values in item_tags.items():
        namespace, path = tag.split(":")
        write(DATA / namespace / "tags" / "item" / f"{path}.json", {"replace": False, "values": tag_entries(values)})


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    world_blocks()
    devices()
    items()
    worldgen()
    recipes()
    write_tags()
    count = sum(1 for _ in OUT.rglob("*.json"))
    print(f"generated {count} files")


if __name__ == "__main__":
    main()
