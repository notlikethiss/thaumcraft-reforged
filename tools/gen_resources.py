import json
import shutil
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_structures
import mcformat

profile = mcformat.init()
OUT = mcformat.out_root
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


MANIFEST = {"predicates": {}, "tints": {}, "builtin_entity": {}, "composite": {}}
TRADES = {}
FUELS = {}
NOTES = defaultdict(list)
ITEM_MODELS = {}
BLOCK_MODELS = {}
INJECTIONS = []
FUEL_ITEMS = {"alumentum": ["alumentum"], "magical_log": ["greatwood_log", "silverwood_log"]}
NBT_KEYS = {"thaumcraft:wand_vis": "wand_vis", "thaumcraft:stored_vis": "stored_vis", "thaumcraft:golem_core": "golem_core", "thaumcraft:mirror_link": "mirror_link"}


def rename_ids(value):
    if isinstance(value, str):
        return profile.rename_vanilla(value)
    if isinstance(value, list):
        return [rename_ids(entry) for entry in value]
    if isinstance(value, dict):
        return {key: rename_ids(entry) for key, entry in value.items()}
    return value


def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    if profile.vanilla_renames:
        content = rename_ids(content)
    path.write_text(json.dumps(content, indent=2) + "\n", encoding="utf-8")


def ref(name, kind="block"):
    return f"{NS}:{kind}/{name}"


def tint(color):
    value = color | 0xFF000000
    if value >= 0x80000000:
        value -= 0x100000000
    return {"type": "minecraft:constant", "value": value}


def convert_elements(name, content):
    if profile.modern or "elements" not in content:
        return content
    return {**content, "elements": [profile.model_element(element, name, NOTES["model_elements"]) for element in content["elements"]]}


def block_model(name, content):
    BLOCK_MODELS[name] = content
    write(ASSETS / "models" / "block" / f"{name}.json", convert_elements(f"block/{name}", content))


def texture_file(reference):
    if ":" not in reference:
        reference = "minecraft:" + reference
    namespace, path = reference.split(":", 1)
    if namespace != NS:
        return None
    for root in (ASSETS / "textures", SOURCE_TEXTURES):
        candidate = root / f"{path}.png"
        if candidate.exists():
            return candidate
    return None


def texture_layer(reference, cache={}):
    if reference not in cache:
        from PIL import Image
        file = texture_file(reference)
        layer = "solid"
        if file is not None:
            alpha = Image.open(file).convert("RGBA").getchannel("A")
            histogram = alpha.histogram()
            if any(histogram[1:255]):
                layer = "translucent"
            elif histogram[0]:
                layer = "cutout"
        cache[reference] = layer
    return cache[reference]


def model_textures(name, seen=None):
    seen = seen or set()
    if name in seen or name not in BLOCK_MODELS:
        return []
    seen.add(name)
    content = BLOCK_MODELS[name]
    result = [value for value in content.get("textures", {}).values() if not value.startswith("#")]
    parent = content.get("parent", "")
    if parent.startswith(f"{NS}:block/"):
        result += model_textures(parent[len(f"{NS}:block/"):], seen)
    return result


def apply_render_types():
    order = ["solid", "cutout", "translucent"]
    for name in BLOCK_MODELS:
        layers = [texture_layer(texture) for texture in model_textures(name)]
        if not layers:
            continue
        layer = max(layers, key=order.index)
        if layer == "solid":
            continue
        path = ASSETS / "models" / "block" / f"{name}.json"
        content = json.loads(path.read_text(encoding="utf-8"))
        content["render_type"] = f"minecraft:{layer}"
        path.write_text(json.dumps(content, indent=2) + "\n", encoding="utf-8")


def item_model(name, content):
    ITEM_MODELS[name] = content
    write(ASSETS / "models" / "item" / f"{name}.json", convert_elements(f"item/{name}", content))


def tint_manifest(tints):
    result = {}
    for layer, entry in enumerate(tints):
        if entry["type"] == "minecraft:constant":
            result[str(layer)] = {"constant": f"#{entry['value'] & 0xFFFFFF:06X}"}
        else:
            result[str(layer)] = {"source": entry["type"]}
    return result


def register_tints(name, tints):
    if tints:
        MANIFEST["tints"][name] = tint_manifest(tints)
    else:
        MANIFEST["tints"].pop(name, None)


def item_definition(name, model, tints=None):
    if not profile.modern:
        if model != ref(name, "item"):
            item_model(name, {"parent": model})
        register_tints(name, tints)
        return
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


def write_loot(folder, name, table_type, pools):
    write(DATA / NS / profile.loot_folder / folder / f"{name}.json", profile.loot_table({
        "type": table_type,
        "pools": pools,
        "random_sequence": f"{NS}:{folder}/{name}",
    }))


def loot(name, pools):
    write_loot("blocks", name, "minecraft:block", pools)


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
    if not profile.modern:
        FUELS[name] = ticks
        return
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
    "hover_harness": "hoverharness",
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
    "hover_harness": ("hoverharness", None),
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
    item_tags["minecraft:chest_armor"].add(f"{NS}:hover_harness")
    for asset, (outer, inner) in EQUIPMENT.items():
        if not profile.modern:
            copy_texture(f"model/{outer}.png", f"models/armor/{asset}_layer_1.png")
            if inner:
                copy_texture(f"model/{inner}.png", f"models/armor/{asset}_layer_2.png")
            continue
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


WANDS = {"wand_apprentice": "wandapprentice", "wand_adept": "wandadept", "wand_thaumaturge": "wandthaumaturge", "wand_excavation": "wandexcavation", "wand_equal_trade": "wandtrade", "wand_frost": "wandfrost", "wand_lightning": "wandlightning", "wand_fire": "wandfire", "hellrod": "hellrod"}


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


def builtin_entity_model(name, base):
    kind = "item" if "/item/" in base else "block"
    source = (ITEM_MODELS if kind == "item" else BLOCK_MODELS).get(base.split("/")[-1], {})
    item_model(name, {"parent": "minecraft:builtin/entity", "textures": source.get("textures", {}), "display": profile.item_model_display()})


def special_item(name, base, model):
    if not profile.modern:
        builtin_entity_model(name, base)
        MANIFEST["builtin_entity"][name] = {"renderer": model["type"], **{key: value for key, value in model.items() if key != "type"}}
        return
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


def box_element(start, end, textures, tintindex=None, light=None):
    bounds = {
        "down": start[1] == 0, "up": end[1] == 16,
        "north": start[2] == 0, "south": end[2] == 16,
        "west": start[0] == 0, "east": end[0] == 16,
    }
    faces = {}
    for direction in DIRECTIONS:
        face = {"texture": textures[direction]}
        if bounds[direction]:
            face["cullface"] = direction
        if tintindex is not None:
            face["tintindex"] = tintindex[direction]
        faces[direction] = face
    element = {"from": start, "to": end, "faces": faces}
    if light:
        element["light_emission"] = light
    return element


def faces(top, bottom, side):
    return {"up": top, "down": bottom, "north": side, "south": side, "west": side, "east": side}


EAR_BELLS = [
    ([4, 8, 1], [12, 16, 3]), ([5, 8, 3], [11, 15, 4]),
    ([1, 8, 4], [3, 16, 12]), ([3, 8, 5], [4, 15, 11]),
    ([4, 8, 13], [12, 16, 15]), ([5, 8, 12], [11, 15, 13]),
    ([13, 8, 4], [15, 16, 12]), ([12, 8, 5], [13, 15, 11]),
]
LEVITATOR_TINTS = [0x00A000, 0xFFFF7E, 0xAA33FC]


def door_variants(name):
    variants = {}
    for facing, base in {"east": 0, "south": 90, "west": 180, "north": 270}.items():
        for half, part in [("lower", "bottom"), ("upper", "top")]:
            for hinge in ["left", "right"]:
                for is_open in [False, True]:
                    model = f"{name}_{part}_{hinge}" + ("_open" if is_open else "")
                    rotation = (base + (0 if not is_open else 90 if hinge == "left" else 270)) % 360
                    variant = {"model": ref(model)}
                    if rotation:
                        variant["y"] = rotation
                    variants[f"facing={facing},half={half},hinge={hinge},open={str(is_open).lower()}"] = variant
    return variants


def owned_devices():
    ear_textures = {
        "particle": ref("arcaneearsideon"),
        "side": ref("arcaneearsideon"),
        "bottom": ref("arcaneearbottom"),
        "bell_side": ref("arcaneearbellside"),
        "bell_top": ref("arcaneearbelltop"),
    }
    for suffix, top in [("", "arcaneeartopoff"), ("_on", "arcaneeartopon")]:
        block_model(f"arcane_ear{suffix}", {
            "parent": "minecraft:block/block",
            "textures": {**ear_textures, "top": ref(top)},
            "elements": [
                box_element([0, 0, 0], [16, 3, 16], faces("#top", "#bottom", "#side")),
                box_element([4, 3, 4], [12, 16, 12], faces("#top", "#bottom", "#side")),
                *[box_element(start, end, faces("#bell_top", "#bell_top", "#bell_side")) for start, end in EAR_BELLS],
            ],
        })
    blockstate("arcane_ear", {"variants": {
        "powered=false": {"model": ref("arcane_ear")},
        "powered=true": {"model": ref("arcane_ear_on")},
    }})
    block_item("arcane_ear")
    self_drop("arcane_ear")

    variants = {}
    for mode in range(3):
        for powered, parent in [("false", "pressure_plate_up"), ("true", "pressure_plate_down")]:
            model = f"arcane_pressure_plate_{mode}" + ("_down" if powered == "true" else "")
            block_model(model, {"parent": f"minecraft:block/{parent}", "textures": {"texture": ref(f"applate{mode + 1}")}})
            variants[f"mode={mode},powered={powered}"] = {"model": ref(model)}
    blockstate("arcane_pressure_plate", {"variants": variants})
    item_definition("arcane_pressure_plate", ref("arcane_pressure_plate_0"))
    self_drop("arcane_pressure_plate")

    for part in ["bottom", "top"]:
        for hinge in ["left", "right"]:
            for suffix in ["", "_open"]:
                block_model(f"arcane_door_{part}_{hinge}{suffix}", {
                    "parent": f"minecraft:block/door_{part}_{hinge}{suffix}",
                    "textures": {"bottom": ref("adoorbot"), "top": ref("adoortop")},
                })
    blockstate("arcane_door", {"variants": door_variants("arcane_door")})
    generated_item("arcane_door", "arcanedoor")
    loot("arcane_door", [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:item",
            "name": f"{NS}:arcane_door",
            "condition": {"type": "minecraft:match_block", "blocks": f"{NS}:arcane_door", "state": {"half": "lower"}},
        }],
        "condition": {"type": "minecraft:survives_explosion"},
    }])

    generated_item("iron_arcane_key", "keyiron")
    generated_item("gold_arcane_key", "keygold")

    levitator_textures = {"particle": ref("lifterside"), "top": ref("liftertop"), "side": ref("lifterside"), "glow": ref("animatedglow")}
    glow_tints = {"up": 0, "down": 1, "north": 2, "south": 2, "west": 2, "east": 2}
    for suffix, light in [("", 11), ("_powered", None)]:
        glow = box_element([0.16, 0.16, 0.16], [15.84, 15.84, 15.84], faces("#glow", "#glow", "#glow"), glow_tints, light)
        block_model(f"arcane_levitator{suffix}", {
            "parent": "minecraft:block/block",
            "textures": levitator_textures,
            "elements": [glow, box_element([0, 0, 0], [16, 16, 16], faces("#top", "#top", "#side"))],
        })
    blockstate("arcane_levitator", {"variants": {
        "powered=false": {"model": ref("arcane_levitator")},
        "powered=true": {"model": ref("arcane_levitator_powered")},
    }})
    block_item("arcane_levitator", [tint(color) for color in LEVITATOR_TINTS])
    self_drop("arcane_levitator")

    block_model("hungry_chest", {"textures": {"particle": ref("woodplain")}})
    facing_state("hungry_chest")
    item_model("hungry_chest", {"parent": "minecraft:block/block", "textures": {"particle": ref("woodplain")}})
    special_item("hungry_chest", ref("hungry_chest", "item"), {"type": f"{NS}:hungry_chest"})
    self_drop("hungry_chest")

    crystals()
    mirrors_and_hole()
    bore()

    mineable("axe", "arcane_ear", "arcane_pressure_plate", "arcane_levitator", "hungry_chest")
    mineable("pickaxe", "arcane_door")


CRYSTAL_CLUSTERS = ["air", "fire", "water", "earth", "vis", "mixed"]


def crystals():
    block_model("crystal", {"textures": {"particle": ref("crystal")}})
    item_model("crystal", {"parent": "minecraft:block/block", "textures": {"particle": ref("crystal")}})
    for index, name in enumerate(CRYSTAL_CLUSTERS):
        block = f"{name}_crystal_cluster"
        blockstate(block, {"variants": {f"facing={direction}": {"model": ref("crystal")} for direction in DIRECTIONS}})
        special_item(block, ref("crystal", "item"), {"type": f"{NS}:crystal", "kind": "cluster", "crystal": index})
        self_drop(block)
    simple_state("crystal_core", ref("crystal"))
    special_item("crystal_core", ref("crystal", "item"), {"type": f"{NS}:crystal", "kind": "core"})
    loot("crystal_core", [])

    frame = ref("crystalframe")
    inset = 1.84
    inner = [
        {"from": [0, 0, inset], "to": [16, 16, inset], "faces": {"south": {"texture": "#frame"}}},
        {"from": [0, 0, 16 - inset], "to": [16, 16, 16 - inset], "faces": {"north": {"texture": "#frame"}}},
        {"from": [inset, 0, 0], "to": [inset, 16, 16], "faces": {"east": {"texture": "#frame"}}},
        {"from": [16 - inset, 0, 0], "to": [16 - inset, 16, 16], "faces": {"west": {"texture": "#frame"}}},
        {"from": [0, inset, 0], "to": [16, inset, 16], "faces": {"up": {"texture": "#frame"}}},
        {"from": [0, 16 - inset, 0], "to": [16, 16 - inset, 16], "faces": {"down": {"texture": "#frame"}}},
    ]
    block_model("crystal_capacitor", {
        "parent": "minecraft:block/block",
        "textures": {"particle": frame, "frame": frame},
        "elements": [full_cube("#frame"), *inner],
    })
    simple_state("crystal_capacitor")
    if profile.modern:
        write(ASSETS / "items" / "crystal_capacitor.json", {"model": {
            "type": "minecraft:composite",
            "models": [
                {"type": "minecraft:model", "model": ref("crystal_capacitor")},
                {"type": "minecraft:special", "base": ref("crystal_capacitor"), "model": {"type": f"{NS}:crystal", "kind": "capacitor"}},
            ],
        }})
    else:
        builtin_entity_model("crystal_capacitor", ref("crystal_capacitor"))
        MANIFEST["composite"]["crystal_capacitor"] = [
            {"kind": "block_model", "model": ref("crystal_capacitor")},
            {"kind": "builtin_entity", "renderer": f"{NS}:crystal", "params": {"kind": "capacitor"}},
        ]
    loot("crystal_capacitor", [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:item",
            "name": f"{NS}:crystal_capacitor",
            "modifier": {"type": "minecraft:copy_components", "source": "block_entity", "include": [f"{NS}:stored_vis"]},
        }],
    }])


MIRROR_ROTATIONS = {
    "north": {}, "south": {"y": 180}, "west": {"y": 270}, "east": {"y": 90}, "up": {"x": 270}, "down": {"x": 90},
}


def mirrors_and_hole():
    for suffix, pane in [("", "mirrorpane"), ("_linked", "mirrorpanetrans")]:
        block_model(f"magic_mirror{suffix}", {
            "parent": "minecraft:block/block",
            "textures": {"particle": ref("mirrorframe"), "frame": ref("mirrorframe"), "pane": ref(pane)},
            "elements": [
                {"from": [0, 0, 15], "to": [16, 16, 16], "faces": {direction: {"texture": "#frame"} for direction in DIRECTIONS}},
                {"from": [0, 0, 15.68], "to": [16, 16, 15.68], "faces": {"north": {"texture": "#pane"}}},
            ],
        })
    variants = {}
    for facing, rotation in MIRROR_ROTATIONS.items():
        for linked, suffix in [("false", ""), ("true", "_linked")]:
            variants[f"facing={facing},linked={linked}"] = {"model": ref(f"magic_mirror{suffix}"), **rotation}
    blockstate("magic_mirror", {"variants": variants})
    for texture in ["mirrorpane", "mirrorpaneopen"]:
        copy_texture(f"block/{texture}.png", f"item/{texture}.png")
    for suffix, pane in [("", "mirrorpane"), ("_linked", "mirrorpaneopen")]:
        item_model(f"magic_mirror{suffix}", {"parent": "minecraft:item/generated", "textures": {"layer0": ref("mirrorframe", "item"), "layer1": ref(pane, "item")}})
    if profile.modern:
        write(ASSETS / "items" / "magic_mirror.json", {"model": {
            "type": "minecraft:condition",
            "property": "minecraft:has_component",
            "component": f"{NS}:mirror_link",
            "on_true": {"type": "minecraft:model", "model": ref("magic_mirror_linked", "item")},
            "on_false": {"type": "minecraft:model", "model": ref("magic_mirror", "item")},
        }})
    else:
        item_model("magic_mirror", {**ITEM_MODELS["magic_mirror"], "overrides": [
            {"predicate": {f"{NS}:linked": 1}, "model": ref("magic_mirror_linked", "item")},
        ]})
        MANIFEST["predicates"]["magic_mirror"] = {
            f"{NS}:linked": {
                "kind": "has_component",
                "component": f"{NS}:mirror_link",
                "nbt_key": NBT_KEYS[f"{NS}:mirror_link"],
                "meaning": "1 when the stack carries the mirror link, otherwise 0",
                "values": {"1": ref("magic_mirror_linked", "item")},
                "default": ref("magic_mirror", "item"),
            },
        }
    loot("magic_mirror", [])
    generated_item("hand_mirror", "mirrorhand")
    generated_item("portable_hole", "portablehole")
    block_model("hole", {"textures": {"particle": "minecraft:block/black_concrete"}})
    simple_state("hole")
    block_tags[f"{NS}:portable_hole_blacklist"].update({
        f"{NS}:arcane_stone", f"{NS}:arcane_wood", "#minecraft:beds", "minecraft:oak_door", "minecraft:iron_door",
    })


def bore():
    block_model("arcane_bore", {"textures": {"particle": ref("woodplain")}})
    item_model("arcane_bore", {"parent": "minecraft:block/block", "textures": {"particle": ref("woodplain")}})
    blockstate("arcane_bore_base", {"variants": {f"facing={facing}": {"model": ref("arcane_bore")} for facing in FACING_ROTATION}})
    blockstate("arcane_bore", {"variants": {f"base_below={value}": {"model": ref("arcane_bore")} for value in ["true", "false"]}})
    special_item("arcane_bore_base", ref("arcane_bore", "item"), {"type": f"{NS}:bore", "base": True})
    special_item("arcane_bore", ref("arcane_bore", "item"), {"type": f"{NS}:bore"})
    self_drop("arcane_bore_base")
    self_drop("arcane_bore")
    mineable("axe", "arcane_bore_base", "arcane_bore")


def devices():
    wards_and_decor()
    owned_devices()
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
    write_loot("chests", name, "minecraft:chest", pools)


EGG_SHAPE = [
    "................",
    "......####......",
    ".....######.....",
    "....########....",
    "....########....",
    "...##########...",
    "...##########...",
    "..############..",
    "..############..",
    "..############..",
    "..############..",
    "..############..",
    "...##########...",
    "...##########...",
    "....########....",
    "......####......",
]
EGG_SPOTS = [(6, 3), (9, 5), (5, 7), (10, 9), (7, 11), (4, 10), (11, 6), (8, 8), (6, 13)]
EGG_HIGHLIGHTS = [(5, 4), (6, 4), (5, 5)]


def spawn_egg(name, base, spots):
    from PIL import Image

    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    inside = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and EGG_SHAPE[y][x] == "#"
    for y in range(16):
        for x in range(16):
            if not inside(x, y):
                continue
            color = spots if (x, y) in EGG_SPOTS else base
            factor = 1.0
            if not inside(x + 1, y) or not inside(x, y + 1):
                factor = 0.6
            elif not inside(x - 1, y) or not inside(x, y - 1):
                factor = 0.85
            if (x, y) in EGG_HIGHLIGHTS:
                factor = 1.3
            channels = [min(255, int(((color >> shift) & 0xFF) * factor)) for shift in (16, 8, 0)]
            image.putpixel((x, y), (*channels, 255))
    destination = ASSETS / "textures" / "item" / f"{name}.png"
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination)
    generated_item(name, name)


def entity_loot(name, pools):
    write_loot("entities", name, "minecraft:entity", pools)


def chance_pool(item, rolls, chance, count=1):
    entry = {"type": "minecraft:item", "name": item}
    if count > 1:
        entry["modifier"] = {"type": "minecraft:set_count", "count": count}
    return {"rolls": rolls, "entries": [entry], "condition": {"type": "minecraft:random_chance", "chance": chance}}


def brain_pool():
    return {
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"{NS}:zombie_brain"}],
        "condition": {
            "type": "minecraft:random_chance_with_enchanted_bonus",
            "enchantment": "minecraft:looting",
            "unenchanted_chance": 0.5,
            "enchanted_chance": {"type": "minecraft:linear", "base": 0.6, "per_level_above_first": 0.1},
        },
    }


def rare_pool(items):
    return {
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": item} for item in items],
        "condition": {
            "type": "minecraft:all_of",
            "terms": [
                {"type": "minecraft:killed_by_player"},
                {
                    "type": "minecraft:random_chance_with_enchanted_bonus",
                    "enchantment": "minecraft:looting",
                    "unenchanted_chance": 0.025,
                    "enchanted_chance": {"type": "minecraft:linear", "base": 0.035, "per_level_above_first": 0.01},
                },
            ],
        },
    }


def add_spawns(name, biomes, entity, weight):
    write(DATA / NS / profile.biome_modifier_namespace / "biome_modifier" / f"spawn_{name}.json", profile.spawn_modifier(biomes, f"{NS}:{entity}", weight))


GOLEM_ICONS = {
    "wood_golem": "golemwood", "clay_golem": "golemclay", "stone_golem": "golemstone", "tallow_golem": "golemtallow",
    "straw_golem": "golemstraw", "advanced_clay_golem": "golemclayadv", "advanced_stone_golem": "golemstoneadv",
    "iron_guardian_golem": "golemiron", "decanting_golem": "golemdecant",
}
GOLEM_CORE_ICONS = {"basic": "golemcorebasic", "speed": "golemcorespeed", "intelligence": "golemcoresmart", "perception": "golemcorevision", "strength": "golemcorestrong"}
GOLEM_DECORATION_ICONS = {
    "top_hat": "golemdecotophat", "spectacles": "golemdecoglasses", "bowtie": "golemdecobowtie", "fez": "golemdecofez",
    "dart_launcher": "golemdecodart", "visor": "golemdecovisor", "iron_plating": "golemdecoarmor",
}
GOLEM_TYPE_OVERLAYS = {1: "golemtypefast", 2: "golemtypesmart", 3: "golemtypevision", 4: "golemtypestrong"}


def golems():
    for core, icon in GOLEM_CORE_ICONS.items():
        generated_item(f"golem_core_{core}", icon)
    for decoration, icon in GOLEM_DECORATION_ICONS.items():
        generated_item(f"golem_{decoration}", icon)
    for golem, icon in GOLEM_ICONS.items():
        item_model(golem, {"parent": "minecraft:item/generated", "textures": {"layer0": ref(icon, "item")}})
        cases = []
        for core, overlay in GOLEM_TYPE_OVERLAYS.items():
            name = f"{golem}_{overlay}"
            item_model(name, {"parent": "minecraft:item/generated", "textures": {"layer0": ref(icon, "item"), "layer1": ref(overlay, "item")}})
            cases.append({"when": core, "model": {"type": "minecraft:model", "model": ref(name, "item")}})
        if profile.modern:
            write(ASSETS / "items" / f"{golem}.json", {"model": {
                "type": "minecraft:select",
                "property": "minecraft:component",
                "component": f"{NS}:golem_core",
                "cases": cases,
                "fallback": {"type": "minecraft:model", "model": ref(golem, "item")},
            }})
        else:
            item_model(golem, {**ITEM_MODELS[golem], "overrides": [
                {"predicate": {f"{NS}:golem_core": case["when"]}, "model": case["model"]["model"]} for case in cases
            ]})
            MANIFEST["predicates"][golem] = {
                f"{NS}:golem_core": {
                    "kind": "component_int",
                    "component": f"{NS}:golem_core",
                    "nbt_key": NBT_KEYS[f"{NS}:golem_core"],
                    "meaning": "golem core type stored in the stack (0 or absent = base model), overrides are threshold based so each value from 1 to 4 selects its own model",
                    "values": {str(case["when"]): case["model"]["model"] for case in cases},
                    "default": ref(golem, "item"),
                },
            }
    block_tags[f"{NS}:golem_harvestable"]


def entities():
    flesh = "minecraft:rotten_flesh"
    entity_loot("brainy_zombie", [
        chance_pool(flesh, 3, 0.5),
        brain_pool(),
        rare_pool(["minecraft:iron_ingot", "minecraft:carrot", "minecraft:potato"]),
    ])
    entity_loot("giant_brainy_zombie", [
        chance_pool(flesh, 12, 0.5, 2),
        brain_pool(),
        rare_pool([f"{NS}:thaumium_ingot", "minecraft:carrot", "minecraft:potato", f"{NS}:amber"]),
    ])
    entity_loot("fire_bat", [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:item",
            "name": "minecraft:gunpowder",
            "modifier": [
                {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}},
                {
                    "type": "minecraft:enchanted_count_increase",
                    "enchantment": "minecraft:looting",
                    "count": {"type": "minecraft:uniform", "min": 0, "max": 1},
                },
            ],
        }],
    }])
    add_spawns("brainy_zombie", "#minecraft:is_overworld", "brainy_zombie", 6)
    add_spawns("fire_bat", "#minecraft:is_nether", "fire_bat", 10)
    spawn_egg("brainy_zombie_spawn_egg", 44975, 16729224)
    spawn_egg("fire_bat_spawn_egg", 16733525, 15602158)
    spawn_egg("wisp_spawn_egg", 5592405, 1131656)
    generated_item("wisp_essence", "wispessence", [{"type": f"{NS}:essence"}])


def worldgen():
    write(DATA / NS / "worldgen" / profile.feature_folder / "world_generation.json", profile.configured_feature({"type": f"{NS}:world_generation"}))
    write(DATA / NS / "worldgen" / "placed_feature" / "world_generation.json", {"feature": f"{NS}:world_generation", "placement": []})
    for dimension in ["overworld", "nether"]:
        write(DATA / NS / profile.biome_modifier_namespace / "biome_modifier" / f"world_generation_{dimension}.json", profile.feature_modifier(
            f"#minecraft:is_{dimension}", f"{NS}:world_generation", "top_layer_modification",
        ))
    chest_loot("mound", ["minecraft:chests/simple_dungeon"], 1 / 20)
    chest_loot("hilltop_stones", ["minecraft:chests/simple_dungeon", "minecraft:chests/simple_dungeon"], 1 / 10)
    chest_loot("greatwood_spider_nest", ["minecraft:chests/simple_dungeon"], 1 / 15)
    wizard_tower_loot()
    chest_injections()
    gen_structures.generate(DATA)


TOWER_CHEST_CONTENTS = [
    ("minecraft:glowstone_dust", 1, 3, 3),
    ("minecraft:glass_bottle", 1, 5, 10),
    ("minecraft:gold_nugget", 1, 3, 5),
    ("minecraft:fire_charge", 1, 1, 5),
    ("minecraft:skeleton_skull", 1, 1, 3),
    (f"{NS}:knowledge_fragment", 1, 3, 20),
    (f"{NS}:alumentum", 1, 1, 5),
    (f"{NS}:nitor", 1, 1, 5),
    (f"{NS}:thaumium_ingot", 1, 2, 5),
]


CHEST_INJECTIONS = [
    ("simple_dungeon", "minecraft:chests/simple_dungeon", {"type": "minecraft:uniform", "min": 1, "max": 3}, 144, 5, 4),
    ("jungle_temple", "minecraft:chests/jungle_temple", {"type": "minecraft:uniform", "min": 2, "max": 6}, 89, 5, 4),
    ("desert_pyramid", "minecraft:chests/desert_pyramid", {"type": "minecraft:uniform", "min": 2, "max": 4}, 247, 5, 4),
    ("abandoned_mineshaft", "minecraft:chests/abandoned_mineshaft", {"type": "minecraft:uniform", "min": 2, "max": 4}, 98, 4, 3),
    ("stronghold_corridor", "minecraft:chests/stronghold_corridor", {"type": "minecraft:uniform", "min": 2, "max": 3}, 101, 4, 3),
    ("stronghold_crossing", "minecraft:chests/stronghold_crossing", {"type": "minecraft:uniform", "min": 1, "max": 4}, 62, 4, 3),
    ("stronghold_library", "minecraft:chests/stronghold_library", {"type": "minecraft:uniform", "min": 2, "max": 10}, 52, 4, 3),
]
CHEST_RARE_LOOT = ["thaumium_sword", "thaumium_pickaxe", "thaumium_axe", "thaumium_hoe"]


def count_modifier(minimum, maximum):
    return {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": minimum, "max": maximum}}


def chest_injection(name, target, pools):
    write_loot("inject", name, "minecraft:chest", pools)
    write(DATA / NS / "loot_modifiers" / f"{name}.json", profile.loot_modifier(name, target, f"{NS}:inject/{name}"))
    INJECTIONS.append(f"{NS}:{name}")


def chest_injections():
    for name, target, rolls, empty_weight, common_weight, uncommon_weight in CHEST_INJECTIONS:
        entries = [
            {"type": "minecraft:item", "name": f"{NS}:thaumium_ingot", "weight": common_weight, "modifier": count_modifier(1, 3)},
            {"type": "minecraft:item", "name": f"{NS}:amber", "weight": common_weight, "modifier": count_modifier(1, 3)},
            {"type": "minecraft:item", "name": f"{NS}:knowledge_fragment", "weight": uncommon_weight, "modifier": count_modifier(1, 2)},
        ]
        if name == "stronghold_library":
            entries.append({"type": "minecraft:item", "name": f"{NS}:knowledge_fragment", "weight": 20, "modifier": count_modifier(3, 6)})
        entries += [{"type": "minecraft:item", "name": f"{NS}:{tool}", "weight": 1} for tool in CHEST_RARE_LOOT]
        entries.append({"type": "minecraft:empty", "weight": empty_weight})
        chest_injection(name, target, [{"rolls": rolls, "entries": entries}])
    chest_injection("village_weaponsmith", "minecraft:chests/village/village_weaponsmith", [{
        "rolls": {"type": "minecraft:uniform", "min": 3, "max": 8},
        "entries": [
            {"type": "minecraft:item", "name": f"{NS}:thaumium_ingot", "weight": 10, "modifier": count_modifier(1, 3)},
            {"type": "minecraft:empty", "weight": 107},
        ],
    }])


def wizard_tower_loot():
    entries = []
    for item, minimum, maximum, weight in TOWER_CHEST_CONTENTS:
        entry = {"type": "minecraft:item", "name": item, "weight": weight}
        if maximum > minimum:
            entry["modifier"] = {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": minimum, "max": maximum}}
        entries.append(entry)
    write_loot("chests", "wizard_tower", "minecraft:chest", [{"rolls": {"type": "minecraft:uniform", "min": 4, "max": 9}, "entries": entries}])




def mod_ids(values):
    found = set()
    for value in values:
        if isinstance(value, str) and value.startswith(f"{NS}:"):
            found.add(value)
    return found


def recipe(name, content, referenced):
    conditions = profile.recipe_conditions(sorted(mod_ids(referenced)))
    if conditions:
        content = {**conditions, **content}
    write(DATA / NS / profile.recipe_folder / f"{name}.json", content)


def shaped(name, result, count, pattern, key, category="misc", components=None):
    content = {"type": "minecraft:crafting_shaped"}
    profile.recipe_category(content, category)
    content["key"] = {symbol: profile.recipe_ingredient(ingredient) for symbol, ingredient in key.items()}
    content["pattern"] = pattern
    content["result"] = profile.recipe_result(result, count, components)
    recipe(name, content, [result, *key.values()])


def shapeless(name, result, count, ingredients, category="misc"):
    content = {"type": "minecraft:crafting_shapeless"}
    profile.recipe_category(content, category)
    content["ingredients"] = [profile.recipe_ingredient(ingredient) for ingredient in ingredients]
    content["result"] = profile.recipe_result(result, count, None)
    recipe(name, content, [result, *ingredients])


def smelting(name, ingredient, result, count, experience):
    if count > 1 and not profile.modern:
        NOTES["recipes"].append({
            "recipe": f"{NS}:{name}",
            "note": f"smelting result count {count} for {result} is not representable in the {profile.name} cooking recipe format",
            "emitted_count": count if profile.name == "1.21.1" else 1,
        })
    content = {"type": "minecraft:smelting", "cookingtime": 200, "experience": experience, "ingredient": profile.recipe_ingredient(ingredient), "result": profile.smelting_result(result, count)}
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


def villagers():
    tc = lambda name: f"{NS}:{name}"

    def uniform(minimum, maximum):
        return {"type": "minecraft:uniform", "min": minimum, "max": maximum}

    def trade(level, name, wants, gives, max_uses, xp, modifier=None):
        content = {"wants": wants, "gives": gives, "max_uses": max_uses, "xp": xp, "reputation_discount": 0.05}
        if modifier:
            content["given_item_modifier"] = modifier
        if not profile.modern:
            TRADES.setdefault(level, []).append({"name": name, **content})
            return f"{NS}:wizard/{level}/{name}"
        write(DATA / NS / "villager_trade" / "wizard" / str(level) / f"{name}.json", content)
        return f"{NS}:wizard/{level}/{name}"

    emerald = {"id": "minecraft:emerald"}
    shard_modifier = [{"type": "minecraft:set_item", "item": tc(f"{shard}_shard"), "condition": {"type": "minecraft:random_chance", "chance": chance}} for shard, chance in [("fire", 1 / 2), ("water", 1 / 3), ("earth", 1 / 4), ("vis", 1 / 5)]]
    shard_modifier.append({"type": "minecraft:set_count", "count": uniform(2, 3)})
    levels = {
        1: [
            trade(1, "emerald_knowledge_fragment", emerald, {"id": tc("knowledge_fragment")}, 8, 2),
            trade(1, "quicksilver_emerald", {"id": tc("quicksilver"), "count": uniform(4, 6)}, {"id": "minecraft:emerald"}, 12, 2),
        ],
        2: [
            trade(2, "emerald_alumentum", emerald, {"id": tc("alumentum")}, 8, 5),
            trade(2, "amber_emerald", {"id": tc("amber"), "count": uniform(4, 6)}, {"id": "minecraft:emerald"}, 12, 5),
        ],
        3: [
            trade(3, "emerald_nitor", emerald, {"id": tc("nitor")}, 8, 10),
            trade(3, "chicken_nugget_emerald", {"id": tc("chicken_nugget"), "count": uniform(24, 31)}, {"id": "minecraft:emerald"}, 12, 10),
        ],
        4: [
            trade(4, "bookshelf_knowledge_fragment", {"id": "minecraft:bookshelf", "count": uniform(2, 3)}, {"id": tc("knowledge_fragment")}, 8, 15),
            trade(4, "emerald_shard", emerald, {"id": tc("air_shard")}, 8, 15, shard_modifier),
        ],
        5: [
            trade(5, "wand_adept_knowledge_fragment", {"id": tc("wand_adept")}, {"id": tc("knowledge_fragment")}, 4, 30, {"type": "minecraft:set_count", "count": uniform(2, 3)}),
        ],
    }
    for level, trades in levels.items():
        if not profile.modern:
            continue
        write(DATA / NS / "tags" / "villager_trade" / "wizard" / f"level_{level}.json", {"values": trades})
        write(DATA / NS / "trade_set" / "wizard" / f"level_{level}.json", {
            "amount": min(2, len(trades)),
            "random_sequence": f"{NS}:trade_set/wizard/level_{level}",
            "trades": f"#{NS}:wizard/level_{level}",
        })
    write(DATA / "minecraft" / "tags" / "point_of_interest_type" / "acquirable_job_site.json", {"replace": False, "values": [{"id": tc("arcane_worktable"), "required": False}]})


WAND_ITEMS = ["wand_excavation", "wand_equal_trade", "wand_frost", "wand_lightning", "wand_fire", "hellrod"]
VIS_REPAIRABLE = [
    "thaumium_sword", "thaumium_pickaxe", "thaumium_axe", "thaumium_shovel", "thaumium_hoe",
    "elemental_sword", "elemental_pickaxe", "elemental_axe", "elemental_shovel", "elemental_hoe",
    "thaumium_helmet", "thaumium_chestplate", "thaumium_leggings", "thaumium_boots",
    "robe_chestplate", "robe_leggings", "robe_boots", "goggles_of_revealing", "boots_traveller", "hover_harness",
]
ENCHANTMENTS = [
    ("potency", 4, 3, 10, 11, "mainhand", f"#{NS}:enchantable/wand", None),
    ("frugal", 5, 3, 5, 11, "mainhand", f"#{NS}:enchantable/wand_frugal", f"{NS}:charging"),
    ("charging", 2, 1, 20, 0, "mainhand", f"#{NS}:enchantable/wand", f"{NS}:frugal"),
    ("treasure", 3, 3, 15, 9, "mainhand", f"#{NS}:enchantable/wand_trade_excavation", None),
    ("haste", 3, 3, 15, 9, "armor", f"#{NS}:enchantable/haste", None),
    ("repair", 2, 2, 20, 10, "any", f"#{NS}:vis_repairable", "minecraft:unbreaking"),
]


def enchantments():
    for name, weight, max_level, base, step, slot, supported, exclusive in ENCHANTMENTS:
        if not profile.enchantment_json:
            continue
        definition = {
            "anvil_cost": 2,
            "description": {"translate": f"enchantment.{NS}.{name}"},
            "effects": {},
            "max_cost": {"base": 61, "per_level_above_first": 10},
            "max_level": max_level,
            "min_cost": {"base": base, "per_level_above_first": step},
            "slots": [slot],
            "supported_items": supported,
            "weight": weight,
        }
        if name == "haste":
            definition["primary_items"] = "#minecraft:enchantable/armor"
        if exclusive:
            definition["exclusive_set"] = exclusive
        write(DATA / NS / "enchantment" / f"{name}.json", definition)
    for tag in ["in_enchanting_table", "on_random_loot", "tradeable", "non_treasure"]:
        if not profile.enchantment_json:
            break
        write(DATA / "minecraft" / "tags" / "enchantment" / f"{tag}.json", {"replace": False, "values": [f"{NS}:{name}" for name, *_ in ENCHANTMENTS]})
    item_tags[f"{NS}:enchantable/wand"].update(f"{NS}:{name}" for name in WAND_ITEMS)
    item_tags[f"{NS}:enchantable/wand_frugal"].update(f"{NS}:{name}" for name in WAND_ITEMS if name != "hellrod")
    item_tags[f"{NS}:enchantable/wand_trade_excavation"].update({f"{NS}:wand_excavation", f"{NS}:wand_equal_trade"})
    item_tags[f"{NS}:enchantable/haste"].update({f"{NS}:boots_traveller", f"{NS}:hover_harness"})
    item_tags[f"{NS}:vis_repairable"].update(f"{NS}:{name}" for name in VIS_REPAIRABLE)
    item_tags[f"{NS}:repairs_goggles"].add("minecraft:gold_ingot")
    if not profile.modern:
        enchantment_manifest()


def resolve_tag(tag):
    if not tag.startswith("#"):
        return [tag]
    return sorted(item_tags.get(tag[1:], []))


def enchantment_manifest():
    rarities = [(10, "COMMON"), (5, "UNCOMMON"), (2, "RARE"), (1, "VERY_RARE")]
    entries = []
    for name, weight, max_level, base, step, slot, supported, exclusive in ENCHANTMENTS:
        entry = {
            "id": f"{NS}:{name}",
            "weight": weight,
            "suggested_rarity_1_19_2": next(rarity for threshold, rarity in rarities if weight >= threshold),
            "max_level": max_level,
            "min_cost": {"base": base, "per_level_above_first": step},
            "max_cost": {"base": 61, "per_level_above_first": 10},
            "anvil_cost": 2,
            "slot": slot,
            "supported_items_tag": supported,
            "supported_items": resolve_tag(supported),
            "exclusive_with": exclusive,
            "treasure": False,
            "discoverable_tradeable": True,
        }
        if name == "haste":
            entry["primary_items_tag"] = "#minecraft:enchantable/armor"
        entries.append(entry)
    NOTES["enchantments"] = entries


def tag_entries(values):
    return [{"id": value, "required": False} if value.startswith(f"{NS}:") else value for value in sorted(values)]


def write_tags():
    for tag, values in block_tags.items():
        if tag in profile.dropped_tags:
            continue
        namespace, path = profile.tag_name(tag)
        write(DATA / namespace / profile.block_tag_folder / f"{path}.json", {"replace": False, "values": tag_entries(values)})
    for tag, values in item_tags.items():
        if tag in profile.dropped_tags:
            continue
        namespace, path = profile.tag_name(tag)
        write(DATA / namespace / profile.item_tag_folder / f"{path}.json", {"replace": False, "values": tag_entries(values)})


def write_manifest(name, content):
    path = mcformat.manifest_dir / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2) + "\n", encoding="utf-8")


def write_extras():
    if INJECTIONS and not profile.modern:
        write(DATA / profile.loot_modifier_namespace / "loot_modifiers" / "global_loot_modifiers.json", {"replace": False, "entries": INJECTIONS})
    if FUELS and not profile.modern:
        if profile.data_maps:
            values = {f"{NS}:{item}": {"burn_time": ticks} for name, ticks in FUELS.items() for item in FUEL_ITEMS[name]}
            write(DATA / "neoforge" / "data_maps" / "item" / "furnace_fuels.json", {"values": values})
        write_manifest("fuels.json", {f"{NS}:{item}": ticks for name, ticks in FUELS.items() for item in FUEL_ITEMS[name]})
    if profile.pack_mcmeta:
        write(OUT / "pack.mcmeta", profile.pack_mcmeta)


def write_manifests():
    if profile.modern:
        return
    manifest = {"minecraft_version": profile.name, "nbt_keys": NBT_KEYS, **MANIFEST}
    manifest["notes"] = {"model_elements": NOTES["model_elements"]}
    write_manifest(f"item_models_{profile.name}.json", manifest)
    write_manifest("trades.json", {
        "profession": f"{NS}:wizard",
        "point_of_interest": f"{NS}:arcane_worktable",
        "levels": {
            str(level): {"offers_picked_per_level": min(2, len(offers)), "offers": offers} for level, offers in TRADES.items()
        },
    })
    write_manifest("enchantments.json", {
        "enchantments": NOTES["enchantments"],
        "notes": {
            "1.21.1": "json is emitted in data/thaumcraft/enchantment with empty effects, all behaviour lives in Java events",
            "1.19.2": "no json, implement as Enchantment subclasses, item tags thaumcraft:enchantable/* are still generated",
        },
    })
    if NOTES["recipes"]:
        write_manifest(f"recipe_notes_{profile.name}.json", NOTES["recipes"])


def main():
    if OUT.exists():
        if mcformat.out_overridden and OUT == ROOT:
            raise SystemExit("refusing to clear the repository root")
        shutil.rmtree(OUT)
    world_blocks()
    devices()
    items()
    worldgen()
    entities()
    golems()
    villagers()
    enchantments()
    recipes()
    write_tags()
    if profile.explicit_render_types:
        apply_render_types()
    write_extras()
    write_manifests()
    count = sum(1 for _ in OUT.rglob("*.json"))
    print(f"generated {count} files")


if __name__ == "__main__":
    main()
