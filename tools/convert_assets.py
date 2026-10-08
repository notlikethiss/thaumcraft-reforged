import json
import re
import shutil
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ORIGINAL = ROOT / "Thaumcraft3.0.5e"
TEXTURES_IN = ORIGINAL / "mods" / "thaumcraft" / "textures"
RESOURCES_IN = ORIGINAL / "thaumcraft" / "resources"
SOUNDS_IN = ORIGINAL / "thaumcraft" / "sound"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "thaumcraft"
LANG_EXTRA = ROOT / "tools" / "lang"

TEXTURE_DIRS = {
    "blocks": "block",
    "items": "item",
    "gui": "gui",
    "misc": "misc",
    "models": "model",
}

SKIPPED_TEXTURES = {
    "items/iconplate.png",
    "items/iconplateexposed.png",
    "items/scanner.png",
    "models/camera.png",
    "models/scanner.png",
    "misc/camera_back.png",
}

SKIPPED_SOUND_GROUPS = {"cameraclack", "cameradone", "camerasuck", "cameraticks"}

COLORS = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]

LANG_FILES = {"en_US": "en_us", "ru_RU": "ru_ru", "de_DE": "de_de"}

RESEARCH_XML = {
    "en_us": ORIGINAL / "thaumcraft" / "resources" / "research.xml",
    "ru_ru": ROOT / "research.xml",
}


def block(name):
    return f"block.thaumcraft.{name}"


def item(name):
    return f"item.thaumcraft.{name}"


def build_key_map():
    keys = {
        "enchantment.potency": "enchantment.thaumcraft.potency",
        "enchantment.frugal": "enchantment.thaumcraft.frugal",
        "enchantment.charging": "enchantment.thaumcraft.charging",
        "enchantment.wandfortune": "enchantment.thaumcraft.treasure",
        "enchantment.haste": "enchantment.thaumcraft.haste",
        "enchantment.repair": "enchantment.thaumcraft.repair",
        "tile.blockHole.name": block("hole"),
        "tile.blockSecure.name": block("warded_stone"),
        "tile.blockInfusionWorkbench.name": block("arcane_stone"),
        "tile.blockWooden.name": block("arcane_wood"),
        "tile.blockChestHungry.name": block("hungry_chest"),
        "tile.blockArcaneDoor.name": block("arcane_door"),
        "tile.blockLifter.name": block("arcane_levitator"),
        "tile.blockMirror.name": block("magic_mirror"),
        "tile.blockTable.0.name": block("table"),
        "tile.blockTable.15.name": block("arcane_worktable"),
        "tile.blockJar.0.name": block("warded_jar"),
        "tile.blockJar.1.name": block("brain_jar"),
        "tile.blockCrucible.0.name": block("crucible"),
        "tile.blockCrucible.1.name": block("alembic"),
        "tile.blockCrucible.5.name": block("advanced_alembic"),
        "tile.blockMagicalLog.greatwood.name": block("greatwood_log"),
        "tile.blockMagicalLog.silverwood.name": block("silverwood_log"),
        "tile.blockMagicalLeaves.greatwood.name": block("greatwood_leaves"),
        "tile.blockMagicalLeaves.silverwood.name": block("silverwood_leaves"),
        "item.ItemHoverHarness.name": item("hover_harness"),
        "item.WandCastingApprentice.name": item("wand_apprentice"),
        "item.WandCastingAdept.name": item("wand_adept"),
        "item.WandCastingMage.name": item("wand_thaumaturge"),
        "item.WandFire.name": item("wand_fire"),
        "item.WandLightning.name": item("wand_lightning"),
        "item.WandFrost.name": item("wand_frost"),
        "item.WandTrade.name": item("wand_equal_trade"),
        "item.WandExcavation.name": item("wand_excavation"),
        "item.Hellrod.name": item("hellrod"),
        "item.BootsTraveller.name": item("boots_traveller"),
        "item.ItemNuggetChicken.name": item("chicken_nugget"),
        "item.ItemNuggetBeef.name": item("beef_nugget"),
        "item.ItemNuggetPork.name": item("pork_nugget"),
        "item.BlockJarFilledItem.name": item("filled_jar"),
        "item.TripleMeatTreat.name": item("triple_meat_treat"),
        "item.ItemShovelElemental.name": item("elemental_shovel"),
        "item.ItemPickaxeElemental.name": item("elemental_pickaxe"),
        "item.ItemAxeElemental.name": item("elemental_axe"),
        "item.ItemHoeElemental.name": item("elemental_hoe"),
        "item.ItemSwordElemental.name": item("elemental_sword"),
        "item.ItemChestplateRobe.name": item("robe_chestplate"),
        "item.ItemLeggingsRobe.name": item("robe_leggings"),
        "item.ItemBootsRobe.name": item("robe_boots"),
        "item.ArcaneDoorKey.0.name": item("iron_arcane_key"),
        "item.ArcaneDoorKey.1.name": item("gold_arcane_key"),
        "item.HandMirror.name": item("hand_mirror"),
        "item.ItemInkwell.name": item("scribing_tools"),
        "item.ItemPortableHole.name": item("portable_hole"),
        "item.ItemThaumometer.name": item("thaumometer"),
        "item.ItemThaumonomicon.name": item("thaumonomicon"),
        "item.ItemGoggles.name": item("goggles_of_revealing"),
        "item.ItemHelmetThaumium.name": item("thaumium_helmet"),
        "item.ItemChestplateThaumium.name": item("thaumium_chestplate"),
        "item.ItemLeggingsThaumium.name": item("thaumium_leggings"),
        "item.ItemBootsThaumium.name": item("thaumium_boots"),
        "item.ItemShovelThaumium.name": item("thaumium_shovel"),
        "item.ItemAxeThaumium.name": item("thaumium_axe"),
        "item.ItemSwordThaumium.name": item("thaumium_sword"),
        "item.ItemPickThaumium.name": item("thaumium_pickaxe"),
        "item.ItemHoeThaumium.name": item("thaumium_hoe"),
        "item.researchnotes.name": item("research_notes"),
        "item.discovery.name": item("discovery"),
        "item.ItemWispEssence.name": item("wisp_essence"),
        "item.ItemGolemCore.name": item("golem_core"),
        "item.ItemGolemDecoration.name": item("golem_decoration"),
    }

    sequences = {
        "tile.blockCustomOre": ("block", [
            "cinnabar_ore", "air_infused_stone", "fire_infused_stone", "water_infused_stone",
            "earth_infused_stone", "vis_infused_stone", "dull_infused_stone", "amber_ore",
        ]),
        "tile.blockCosmeticOpaque": ("block", ["amber_block", "amber_bricks", "warded_glass"]),
        "tile.blockCosmeticSolid": ("block", ["obsidian_totem", "obsidian_tile", "travel_paving_stone"]),
        "tile.blockCustomPlant": ("block", ["greatwood_sapling", "silverwood_sapling", "shimmerleaf", "cinderpearl"]),
        "tile.blockCandle": ("block", [f"{color}_tallow_candle" for color in COLORS]),
        "tile.blockMarker": ("block", [f"{color}_marker" for color in COLORS]),
        "tile.blockWoodenDevice": ("block", {
            0: "arcane_bellows", 1: "arcane_ear", 2: "arcane_pressure_plate", 4: "arcane_bore_base", 5: "arcane_bore",
        }),
        "tile.blockCrystal": ("block", [
            "air_crystal_cluster", "fire_crystal_cluster", "water_crystal_cluster", "earth_crystal_cluster",
            "vis_crystal_cluster", "mixed_crystal_cluster", "crystal_core", "crystal_capacitor",
        ]),
        "item.ItemResource": ("item", [
            "alumentum", None, "thaumium_ingot", "quicksilver", "magic_tallow", "zombie_brain",
            "amber", "enchanted_fabric", "flux_filter", "knowledge_fragment", "mirrored_glass",
        ]),
        "item.ItemShard": ("item", ["air_shard", "fire_shard", "water_shard", "earth_shard", "vis_shard", "dull_shard"]),
        "item.ItemNugget": ("item", {
            2: "tin_nugget", 3: "silver_nugget", 4: "lead_nugget", 5: "quicksilver_drop",
            16: "native_iron_cluster", 17: "native_copper_cluster", 18: "native_tin_cluster",
            19: "native_silver_cluster", 20: "native_lead_cluster", 31: "native_gold_cluster",
        }),
        "item.ItemGolemCore": ("tc", {
            0: "golem_core.basic", 1: "golem_core.speed", 2: "golem_core.intelligence",
            3: "golem_core.perception", 4: "golem_core.strength",
        }),
        "item.ItemGolemPlacer": ("item", [
            "wood_golem", "clay_golem", "stone_golem", "tallow_golem", "straw_golem",
            "advanced_clay_golem", "advanced_stone_golem", "iron_guardian_golem", "decanting_golem",
        ]),
        "item.ItemGolemDecoration": ("tc", {
            0: "golem_decoration.top_hat", 1: "golem_decoration.spectacles", 2: "golem_decoration.bowtie",
            3: "golem_decoration.fez", 4: "golem_decoration.dart_launcher", 5: "golem_decoration.visor",
            6: "golem_decoration.iron_plating",
        }),
    }

    for prefix, (kind, names) in sequences.items():
        pairs = names.items() if isinstance(names, dict) else enumerate(names)
        for index, name in pairs:
            if name is None:
                continue
            if kind == "tc":
                new_key = f"tc.thaumcraft.{name}"
            else:
                new_key = f"{kind}.thaumcraft.{name}"
            keys[f"{prefix}.{index}.name"] = new_key

    keys["item.ItemResource.1.name"] = block("nitor")
    return keys


DROPPED_KEYS = re.compile(
    r"^(item\.(ThaumicCamera|PhotoPlate|ItemFlyingCarpet|ItemHelmetRunic|ItemChestplateRunic|ItemLeggingsRunic|ItemBootsRunic|ItemArcaneDoor)|item\.ItemNugget\.(0|1)\.name)"
)


def read_lang(path):
    raw = path.read_bytes().decode("latin-1")
    raw = re.sub(r"\\u([0-9a-fA-F]{4})", lambda match: chr(int(match.group(1), 16)), raw)
    entries = {}
    for line in raw.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        entries[key.strip()] = value.rstrip()
    return entries


def convert_lang():
    key_map = build_key_map()
    out_dir = ASSETS / "lang"
    out_dir.mkdir(parents=True, exist_ok=True)
    english = None
    for old_name, new_name in LANG_FILES.items():
        entries = read_lang(RESOURCES_IN / f"{old_name}.lang")
        result = {}
        unmapped = []
        for key, value in entries.items():
            if DROPPED_KEYS.match(key):
                continue
            if key in key_map:
                result[key_map[key]] = value
            elif key.startswith("tc."):
                result["tc.thaumcraft." + key[3:]] = value
            else:
                unmapped.append(key)
        extra_path = LANG_EXTRA / f"{new_name}.json"
        if extra_path.exists():
            result.update(json.loads(extra_path.read_text(encoding="utf-8")))
        if new_name == "en_us":
            english = result
        if unmapped:
            print(f"{new_name}: unmapped keys {unmapped}")
        ordered = dict(sorted(result.items()))
        (out_dir / f"{new_name}.json").write_text(
            json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
        print(f"lang {new_name}: {len(ordered)} keys")
    return english


def write_animation(png_path, txt_path):
    lines = [line.strip() for line in txt_path.read_text().splitlines() if line.strip()]
    animation = {}
    if lines:
        frames = []
        for line in lines:
            if "*" in line:
                index, time = line.split("*")
                frames.append({"index": int(index), "time": int(time)})
            else:
                frames.append(int(line))
        times = {frame["time"] for frame in frames if isinstance(frame, dict)}
        if len(times) == 1 and all(isinstance(frame, dict) for frame in frames):
            animation["frametime"] = times.pop()
            animation["frames"] = [frame["index"] for frame in frames]
        else:
            animation["frames"] = frames
    meta = {"animation": animation}
    Path(str(png_path) + ".mcmeta").write_text(json.dumps(meta, indent=2) + "\n")


def convert_textures():
    count = 0
    for old_dir, new_dir in TEXTURE_DIRS.items():
        source = TEXTURES_IN / old_dir
        target = ASSETS / "textures" / new_dir
        if target.exists():
            shutil.rmtree(target)
        target.mkdir(parents=True)
        for path in source.rglob("*"):
            if path.is_dir():
                continue
            relative = path.relative_to(TEXTURES_IN).as_posix()
            if relative in SKIPPED_TEXTURES or path.suffix not in (".png", ".obj"):
                continue
            if path.suffix == ".obj" and "Copy" in path.name:
                continue
            name = path.relative_to(source).as_posix().lower().replace(" ", "_")
            destination = target / name
            if path.suffix == ".obj":
                destination = ASSETS / "models" / "obj" / name
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, destination)
            animation = path.with_suffix(".txt")
            if animation.exists():
                write_animation(destination, animation)
            count += 1
    blank = ASSETS / "textures" / "block" / "blank.png"
    if blank.exists():
        from PIL import Image
        Image.new("RGBA", (16, 16), (0, 0, 0, 0)).save(blank)
    print(f"textures: {count}")


def convert_villager_textures():
    from PIL import Image
    source = TEXTURES_IN / "models" / "wizard.png"
    meta = json.dumps({"villager": {"hat": "full"}}, indent=4) + "\n"
    human = ASSETS / "textures" / "entity" / "villager" / "profession"
    zombie = ASSETS / "textures" / "entity" / "zombie_villager" / "profession"
    human.mkdir(parents=True, exist_ok=True)
    zombie.mkdir(parents=True, exist_ok=True)
    image = Image.open(source).convert("RGBA")
    image.paste((0, 0, 0, 0), (32, 0, 64, 18))
    image.paste((0, 0, 0, 0), (30, 47, 64, 64))
    hem = image.crop((0, 61, 28, 62))
    image.paste(hem, (0, 62))
    image.paste(hem, (0, 63))
    image.save(human / "wizard.png")
    (human / "wizard.png.mcmeta").write_text(meta)
    zombie_image = image.copy()
    zombie_image.paste((0, 0, 0, 0), (0, 0, 32, 18))
    zombie_image.save(zombie / "wizard.png")
    (zombie / "wizard.png.mcmeta").write_text(meta)


def convert_sounds():
    target = ASSETS / "sounds"
    if target.exists():
        shutil.rmtree(target)
    target.mkdir(parents=True)
    groups = defaultdict(list)
    for path in sorted(SOUNDS_IN.glob("*.ogg")):
        group = re.sub(r"\d+$", "", path.stem)
        if group in SKIPPED_SOUND_GROUPS:
            continue
        shutil.copyfile(path, target / path.name)
        groups[group].append(f"thaumcraft:{path.stem}")
    sounds = {
        group: {"subtitle": f"subtitles.thaumcraft.{group}", "sounds": files}
        for group, files in sorted(groups.items())
    }
    (ASSETS / "sounds.json").write_text(json.dumps(sounds, indent=2) + "\n")
    print(f"sounds: {sum(len(files) for files in groups.values())} files, {len(groups)} events")
    return sorted(groups)


def convert_research():
    target = ASSETS / "research"
    target.mkdir(parents=True, exist_ok=True)
    for lang, source in RESEARCH_XML.items():
        shutil.copyfile(source, target / f"{lang}.xml")
    print(f"research xml: {list(RESEARCH_XML)}")


if __name__ == "__main__":
    convert_textures()
    convert_villager_textures()
    convert_sounds()
    convert_research()
    convert_lang()
