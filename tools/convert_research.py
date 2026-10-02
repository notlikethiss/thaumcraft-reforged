import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONFIG = ROOT / "reference" / "decompiled" / "thaumcraft" / "common" / "Config.java"
OUTPUT = ROOT / "src" / "main" / "java" / "thaumcraft" / "research" / "ConfigResearch.java"

ICONS = {
    ("blockCustomOre", 5): "thaumcraft:vis_infused_stone",
    ("blockCustomPlant", 0): "thaumcraft:greatwood_sapling",
    ("blockCustomPlant", 1): "thaumcraft:silverwood_sapling",
    ("blockCustomPlant", 2): "thaumcraft:shimmerleaf",
    ("blockCustomPlant", 3): "thaumcraft:cinderpearl",
    ("blockTable", 15): "thaumcraft:arcane_worktable",
    ("blockTable", 0): "thaumcraft:table",
    ("itemResource", 0): "thaumcraft:alumentum",
    ("itemResource", 1): "thaumcraft:nitor",
    ("itemResource", 2): "thaumcraft:thaumium_ingot",
    ("itemResource", 4): "thaumcraft:magic_tallow",
    ("itemResource", 7): "thaumcraft:enchanted_fabric",
    ("itemResource", 9): "thaumcraft:knowledge_fragment",
    ("itemEssence", 0): "thaumcraft:essentia_phial",
    ("itemNugget", 0): "minecraft:iron_nugget",
    ("itemNugget", 1): "minecraft:copper_nugget",
    ("itemNugget", 2): "thaumcraft:tin_nugget",
    ("itemNugget", 3): "thaumcraft:silver_nugget",
    ("itemNugget", 4): "thaumcraft:lead_nugget",
    ("blockInfusionWorkbench", 0): "thaumcraft:arcane_stone",
    ("blockCosmeticSolid", 2): "thaumcraft:travel_paving_stone",
    ("blockWoodenDevice", 0): "thaumcraft:arcane_bellows",
    ("blockWoodenDevice", 1): "thaumcraft:arcane_ear",
    ("blockWoodenDevice", 2): "thaumcraft:arcane_pressure_plate",
    ("blockWoodenDevice", 5): "thaumcraft:arcane_bore",
    ("blockJar", 0): "thaumcraft:warded_jar",
    ("blockJar", 1): "thaumcraft:brain_jar",
    ("blockCrystal", 5): "thaumcraft:mixed_crystal_cluster",
    ("blockCrystal", 6): "thaumcraft:crystal_core",
    ("blockCrystal", 7): "thaumcraft:crystal_capacitor",
    ("itemGolemPlacer", 0): "thaumcraft:wood_golem",
    ("itemGolemPlacer", 16): "thaumcraft:clay_golem",
    ("itemGolemPlacer", 32): "thaumcraft:stone_golem",
    ("itemGolemPlacer", 48): "thaumcraft:tallow_golem",
    ("itemGolemPlacer", 64): "thaumcraft:straw_golem",
    ("itemGolemPlacer", 80): "thaumcraft:advanced_clay_golem",
    ("itemGolemPlacer", 96): "thaumcraft:advanced_stone_golem",
    ("itemGolemPlacer", 112): "thaumcraft:iron_guardian_golem",
    ("itemGolemPlacer", 128): "thaumcraft:decanting_golem",
    ("itemGolemCore", 1): "thaumcraft:golem_core_speed",
    ("itemGolemCore", 2): "thaumcraft:golem_core_intelligence",
    ("itemGolemCore", 3): "thaumcraft:golem_core_perception",
    ("itemGolemCore", 4): "thaumcraft:golem_core_strength",
    ("itemGolemDecoration", 0): "thaumcraft:golem_top_hat",
    ("itemGolemDecoration", 1): "thaumcraft:golem_spectacles",
    ("itemGolemDecoration", 2): "thaumcraft:golem_bowtie",
    ("itemGolemDecoration", 3): "thaumcraft:golem_fez",
    ("itemGolemDecoration", 4): "thaumcraft:golem_dart_launcher",
    ("itemGolemDecoration", 5): "thaumcraft:golem_visor",
    ("itemGolemDecoration", 6): "thaumcraft:golem_iron_plating",
}

SIMPLE = {
    "itemWandCastingApprentice": "thaumcraft:wand_apprentice",
    "blockCrucible": "thaumcraft:crucible",
    "itemThaumonomicon": "thaumcraft:thaumonomicon",
    "itemInkwell": "thaumcraft:scribing_tools",
    "Item.enchantedBook": "minecraft:enchanted_book",
    "Item.goldNugget": "minecraft:gold_nugget",
    "Item.gunpowder": "minecraft:gunpowder",
    "itemThaumometer": "thaumcraft:thaumometer",
    "itemChestRobe": "thaumcraft:robe_chestplate",
    "itemGoggles": "thaumcraft:goggles_of_revealing",
    "itemWandFire": "thaumcraft:wand_fire",
    "itemWandFrost": "thaumcraft:wand_frost",
    "itemWandLightning": "thaumcraft:wand_lightning",
    "itemWandExcavation": "thaumcraft:wand_excavation",
    "itemWandTrade": "thaumcraft:wand_equal_trade",
    "itemBootsTraveller": "thaumcraft:boots_traveller",
    "itemAxeElemental": "thaumcraft:elemental_axe",
    "itemSwordElemental": "thaumcraft:elemental_sword",
    "itemShovelElemental": "thaumcraft:elemental_shovel",
    "itemPickElemental": "thaumcraft:elemental_pickaxe",
    "itemHoeElemental": "thaumcraft:elemental_hoe",
    "blockChestHungry": "thaumcraft:hungry_chest",
    "itemPortableHole": "thaumcraft:portable_hole",
    "blockMirror": "thaumcraft:magic_mirror",
    "itemHandMirror": "thaumcraft:hand_mirror",
    "blockLifter": "thaumcraft:arcane_levitator",
    "itemArcaneDoor": "thaumcraft:arcane_door",
    "itemHellrod": "thaumcraft:hellrod",
    "itemHoverHarness": "thaumcraft:hover_harness",
}


def convert_icon(expression):
    expression = expression.strip()
    if re.fullmatch(r"-?\d+", expression):
        return expression
    match = re.fullmatch(r"new ItemStack\((\w+(?:\.\w+)?)(?:,\s*\d+,\s*(\d+))?\)", expression)
    if match:
        name, meta = match.group(1), match.group(2)
        if meta is not None and (name, int(meta)) in ICONS:
            return f'"{ICONS[(name, int(meta))]}"'
        if name in SIMPLE:
            return f'"{SIMPLE[name]}"'
        raise KeyError(expression)
    if expression in SIMPLE:
        return f'"{SIMPLE[expression]}"'
    raise KeyError(expression)


def main():
    text = CONFIG.read_text(encoding="utf-8")
    start = text.index("public static void initResearch()")
    body_start = text.index("{", start) + 1
    end = text.index("public static void initLoot()")
    body = text[body_start:end]
    body = body[: body.rindex("}")]
    body = body.replace("initResearchXML();", "")
    body = re.sub(r"if \(found\w+\) \{", "{", body)

    def replace_item(match):
        args = match.group(1)
        depth = 0
        parts = []
        current = ""
        for char in args:
            if char == "(":
                depth += 1
            elif char == ")":
                depth -= 1
            if char == "," and depth == 0:
                parts.append(current)
                current = ""
            else:
                current += char
        parts.append(current)
        icon = convert_icon(parts[4])
        return f"new ResearchItem({parts[0].strip()}, {parts[1].strip()}, {parts[2].strip()}, {parts[3].strip()}, {icon})"

    pattern = re.compile(r"new ResearchItem\(((?:[^()]|\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))*)\)")
    body = pattern.sub(replace_item, body)
    body = body.replace("new ObjectTags()", "tags()").replace("EnumTag.", "Aspect.")
    names = sorted(set(re.findall(r"\b(research\w+) =", body)))
    declarations = "\n".join(f"        ResearchItem {name};" for name in names)
    lines = [line for line in body.splitlines()]
    java = f"""package thaumcraft.research;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class ConfigResearch {{
    private ConfigResearch() {{
    }}

    private static AspectList tags() {{
        return new AspectList();
    }}

    public static void registerAll() {{
{declarations}
{chr(10).join(lines)}
    }}
}}
"""
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(java, encoding="utf-8")
    print(f"research converted, {len(names)} variables")


if __name__ == "__main__":
    main()
