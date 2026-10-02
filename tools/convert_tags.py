import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONFIG = ROOT / "reference" / "decompiled" / "thaumcraft" / "common" / "Config.java"
OUTPUT = ROOT / "src" / "main" / "java" / "thaumcraft" / "aspect" / "ConfigAspects.java"

SIMPLE = {
    "Block.cobblestone": "minecraft:cobblestone",
    "Block.stone": "minecraft:stone",
    "Block.whiteStone": "minecraft:end_stone",
    "Block.sand": "#minecraft:sand",
    "Block.dirt": "minecraft:dirt",
    "Block.grass": "minecraft:grass_block",
    "Block.gravel": "minecraft:gravel",
    "Block.mycelium": "minecraft:mycelium",
    "Item.clay": "minecraft:clay_ball",
    "Block.blockClay": "minecraft:clay",
    "Item.goldNugget": "minecraft:gold_nugget",
    "Item.brick": "minecraft:brick",
    "Block.slowSand": "minecraft:soul_sand",
    "Block.netherrack": "minecraft:netherrack",
    "Block.netherBrick": "minecraft:nether_bricks",
    "Block.glass": "minecraft:glass",
    "Item.snowball": "minecraft:snowball",
    "Block.leaves": "#minecraft:leaves",
    "Block.tallGrass": ["minecraft:short_grass", "minecraft:fern"],
    "Block.waterlily": "minecraft:lily_pad",
    "Block.deadBush": "minecraft:dead_bush",
    "Block.vine": "minecraft:vine",
    "Item.seeds": "minecraft:wheat_seeds",
    "Item.melonSeeds": "minecraft:melon_seeds",
    "Item.pumpkinSeeds": "minecraft:pumpkin_seeds",
    "Item.melon": "minecraft:melon_slice",
    "Item.netherStalkSeeds": "minecraft:nether_wart",
    "Item.cookie": "minecraft:cookie",
    "Item.potion": "minecraft:potion",
    "Block.torchWood": "minecraft:torch",
    "Block.wood": "#minecraft:logs",
    "Block.planks": "#minecraft:planks",
    "Block.cobblestoneMossy": "minecraft:mossy_cobblestone",
    "Block.ice": "minecraft:ice",
    "Block.plantRed": "minecraft:poppy",
    "Block.plantYellow": "minecraft:dandelion",
    "Block.cactus": "minecraft:cactus",
    "Block.sapling": "#minecraft:saplings",
    "Block.mushroomBrown": "minecraft:brown_mushroom",
    "Block.mushroomRed": "minecraft:red_mushroom",
    "Block.mushroomCapBrown": "minecraft:brown_mushroom_block",
    "Block.mushroomCapRed": "minecraft:red_mushroom_block",
    "Item.reed": "minecraft:sugar_cane",
    "Item.wheat": "minecraft:wheat",
    "Item.appleRed": "minecraft:apple",
    "Block.oreCoal": "minecraft:coal_ore",
    "Block.oreRedstone": "minecraft:redstone_ore",
    "Item.redstone": "minecraft:redstone",
    "Item.lightStoneDust": "minecraft:glowstone_dust",
    "Block.glowStone": "minecraft:glowstone",
    "Block.web": "minecraft:cobweb",
    "Item.flint": "minecraft:flint",
    "Item.silk": "minecraft:string",
    "Item.carrot": "minecraft:carrot",
    "Item.potato": "minecraft:potato",
    "Item.bakedPotato": "minecraft:baked_potato",
    "Item.poisonousPotato": "minecraft:poisonous_potato",
    "Item.slimeBall": "minecraft:slime_ball",
    "Item.leather": "minecraft:leather",
    "Item.rottenFlesh": "minecraft:rotten_flesh",
    "Item.feather": "minecraft:feather",
    "Item.bone": "minecraft:bone",
    "Item.egg": "minecraft:egg",
    "Block.anvil": None,
    "Block.beacon": "minecraft:beacon",
    "Block.chest": "minecraft:chest",
    "Block.cloth": "minecraft:white_wool",
    "Block.enchantmentTable": "minecraft:enchanting_table",
    "Block.fenceGate": "minecraft:oak_fence_gate",
    "Block.furnaceIdle": "minecraft:furnace",
    "Block.jukebox": "minecraft:jukebox",
    "Block.lever": "minecraft:lever",
    "Block.melon": "minecraft:melon",
    "Block.music": "minecraft:note_block",
    "Block.oreIron": "minecraft:iron_ore",
    "Block.oreLapis": "minecraft:lapis_ore",
    "Block.oreGold": "minecraft:gold_ore",
    "Block.oreDiamond": "minecraft:diamond_ore",
    "Block.oreEmerald": "minecraft:emerald_ore",
    "Block.oreNetherQuartz": "minecraft:nether_quartz_ore",
    "Block.obsidian": "minecraft:obsidian",
    "Block.pistonBase": "minecraft:piston",
    "Block.pistonStickyBase": "minecraft:sticky_piston",
    "Block.pressurePlatePlanks": "minecraft:oak_pressure_plate",
    "Block.pressurePlateStone": "minecraft:stone_pressure_plate",
    "Block.pumpkin": "minecraft:pumpkin",
    "Block.railDetector": "minecraft:detector_rail",
    "Block.railPowered": "minecraft:powered_rail",
    "Block.stoneButton": "minecraft:stone_button",
    "Block.torchRedstoneActive": "minecraft:redstone_torch",
    "Block.trapdoor": "minecraft:oak_trapdoor",
    "Block.woodenButton": "minecraft:oak_button",
    "Block.workbench": "minecraft:crafting_table",
    "Block.bookShelf": "minecraft:bookshelf",
    "Item.cauldron": "minecraft:cauldron",
    "Item.arrow": "minecraft:arrow",
    "Item.beefCooked": "minecraft:cooked_beef",
    "Item.beefRaw": "minecraft:beef",
    "Item.blazeRod": "minecraft:blaze_rod",
    "Item.boat": "minecraft:oak_boat",
    "Item.book": "minecraft:book",
    "Item.bootsChain": "minecraft:chainmail_boots",
    "Item.helmetChain": "minecraft:chainmail_helmet",
    "Item.legsChain": "minecraft:chainmail_leggings",
    "Item.plateChain": "minecraft:chainmail_chestplate",
    "Item.bowlEmpty": "minecraft:bowl",
    "Item.bowlSoup": "minecraft:mushroom_stew",
    "Item.brewingStand": "minecraft:brewing_stand",
    "Item.bucketEmpty": "minecraft:bucket",
    "Item.bucketLava": "minecraft:lava_bucket",
    "Item.bucketMilk": "minecraft:milk_bucket",
    "Item.bucketWater": "minecraft:water_bucket",
    "Item.carrotOnAStick": "minecraft:carrot_on_a_stick",
    "Item.chickenCooked": "minecraft:cooked_chicken",
    "Item.chickenRaw": "minecraft:chicken",
    "Item.diamond": "minecraft:diamond",
    "Item.doorIron": "minecraft:iron_door",
    "Item.doorWood": "minecraft:oak_door",
    "Item.emerald": "minecraft:emerald",
    "Item.enderPearl": "minecraft:ender_pearl",
    "Item.eyeOfEnder": "minecraft:ender_eye",
    "Item.fishCooked": "minecraft:cooked_cod",
    "Item.fishRaw": "minecraft:cod",
    "Item.fishingRod": "minecraft:fishing_rod",
    "Item.flintAndSteel": "minecraft:flint_and_steel",
    "Item.flowerPot": "minecraft:flower_pot",
    "Item.ghastTear": "minecraft:ghast_tear",
    "Item.glassBottle": "minecraft:glass_bottle",
    "Item.goldenCarrot": "minecraft:golden_carrot",
    "Item.gunpowder": "minecraft:gunpowder",
    "Item.ingotGold": "minecraft:gold_ingot",
    "Item.ingotIron": "minecraft:iron_ingot",
    "Item.minecartEmpty": "minecraft:minecart",
    "Item.netherQuartz": "minecraft:quartz",
    "Item.netherStar": "minecraft:nether_star",
    "Item.paper": "minecraft:paper",
    "Item.pocketSundial": "minecraft:clock",
    "Item.porkCooked": "minecraft:cooked_porkchop",
    "Item.porkRaw": "minecraft:porkchop",
    "Item.redstoneRepeater": "minecraft:repeater",
    "Item.saddle": "minecraft:saddle",
    "Item.skull": "#minecraft:skulls",
    "Item.spiderEye": "minecraft:spider_eye",
    "Item.writtenBook": "minecraft:written_book",
    "Item.record11": "minecraft:music_disc_11",
    "Item.record13": "minecraft:music_disc_13",
    "Item.recordBlocks": "minecraft:music_disc_blocks",
    "Item.recordCat": "minecraft:music_disc_cat",
    "Item.recordChirp": "minecraft:music_disc_chirp",
    "Item.recordFar": "minecraft:music_disc_far",
    "Item.recordMall": "minecraft:music_disc_mall",
    "Item.recordMellohi": "minecraft:music_disc_mellohi",
    "Item.recordStal": "minecraft:music_disc_stal",
    "Item.recordStrad": "minecraft:music_disc_strad",
    "Item.recordWait": "minecraft:music_disc_wait",
    "Item.recordWard": "minecraft:music_disc_ward",
    "itemNugget": [
        "minecraft:iron_nugget", "minecraft:copper_nugget", "thaumcraft:tin_nugget", "thaumcraft:silver_nugget",
        "thaumcraft:lead_nugget", "thaumcraft:quicksilver_drop", "thaumcraft:native_copper_cluster",
        "thaumcraft:native_tin_cluster", "thaumcraft:native_silver_cluster", "thaumcraft:native_lead_cluster",
    ],
    "itemNuggetBeef": "thaumcraft:beef_nugget",
    "itemNuggetChicken": "thaumcraft:chicken_nugget",
    "itemNuggetPork": "thaumcraft:pork_nugget",
    "itemTripleMeatTreat": "thaumcraft:triple_meat_treat",
    "itemThaumometer": "thaumcraft:thaumometer",
    "itemGoggles": "thaumcraft:goggles_of_revealing",
    "itemWandCastingApprentice": "thaumcraft:wand_apprentice",
    "itemThaumonomicon": "thaumcraft:thaumonomicon",
}

METADATA = {
    ("Item.dyePowder", 0): "minecraft:ink_sac",
    ("Item.dyePowder", 2): "minecraft:green_dye",
    ("Item.dyePowder", 3): "minecraft:cocoa_beans",
    ("Item.dyePowder", 4): "minecraft:lapis_lazuli",
    ("Item.coal", 0): "minecraft:coal",
    ("Item.coal", 1): "minecraft:charcoal",
    ("Item.appleGold", 0): "minecraft:golden_apple",
    ("Item.appleGold", 1): "minecraft:enchanted_golden_apple",
    ("Block.anvil", 0): "minecraft:anvil",
    ("Block.anvil", 1): "minecraft:chipped_anvil",
    ("Block.anvil", 2): "minecraft:damaged_anvil",
    ("Block.sandStone", -1): "minecraft:sandstone",
    ("Block.sandStone", 0): "minecraft:sandstone",
    ("Block.sandStone", 1): "minecraft:chiseled_sandstone",
    ("Block.sandStone", 2): "minecraft:cut_sandstone",
    ("Block.stoneBrick", 0): "minecraft:stone_bricks",
    ("Block.stoneBrick", 1): "minecraft:mossy_stone_bricks",
    ("Block.stoneBrick", 2): "minecraft:cracked_stone_bricks",
    ("Block.stoneBrick", 3): "minecraft:chiseled_stone_bricks",
    ("itemNugget", 16): "thaumcraft:native_iron_cluster",
    ("itemNugget", 31): "thaumcraft:native_gold_cluster",
    ("itemResource", 3): "thaumcraft:quicksilver",
    ("itemResource", 5): "thaumcraft:zombie_brain",
    ("itemResource", 6): "thaumcraft:amber",
    ("itemResource", 9): "thaumcraft:knowledge_fragment",
    ("itemShard", 0): "thaumcraft:air_shard",
    ("itemShard", 1): "thaumcraft:fire_shard",
    ("itemShard", 2): "thaumcraft:water_shard",
    ("itemShard", 3): "thaumcraft:earth_shard",
    ("itemShard", 4): "thaumcraft:vis_shard",
    ("itemShard", 5): "thaumcraft:dull_shard",
    ("itemEssence", 0): "thaumcraft:essentia_phial",
    ("blockCandle", 0): "thaumcraft:white_tallow_candle",
    ("blockCosmeticSolid", 0): "thaumcraft:obsidian_totem",
    ("blockCrucible", 0): "thaumcraft:crucible",
    ("blockCustomOre", 0): "thaumcraft:cinnabar_ore",
    ("blockCustomOre", 7): "thaumcraft:amber_ore",
    ("blockCustomPlant", 0): "thaumcraft:greatwood_sapling",
    ("blockCustomPlant", 1): "thaumcraft:silverwood_sapling",
    ("blockCustomPlant", 2): "thaumcraft:shimmerleaf",
    ("blockCustomPlant", 3): "thaumcraft:cinderpearl",
    ("blockMagicalLeaves", 0): "thaumcraft:greatwood_leaves",
    ("blockMagicalLeaves", 1): "thaumcraft:silverwood_leaves",
    ("blockMagicalLog", 0): "thaumcraft:greatwood_log",
    ("blockMagicalLog", 1): "thaumcraft:silverwood_log",
    ("blockTable", -1): "thaumcraft:table",
    ("blockTable", 0): "thaumcraft:table",
    ("blockTable", 15): "thaumcraft:arcane_worktable",
}

CALL = re.compile(
    r"ThaumcraftApi\.(registerObjectTag|registerComplexObjectTag)\(\s*([A-Za-z0-9_.]+)\.(?:blockID|itemID)\s*,\s*(-?\d+)\s*,\s*(.*?)\);",
    re.S,
)
BASE = re.compile(r"new ObjectTags\(\s*(?:([A-Za-z0-9_.]+)\.(?:blockID|itemID)\s*,\s*(-?\d+))?\s*\)")
OPERATION = re.compile(r"\.(add|remove|merge)\(EnumTag\.([A-Z]+),\s*(\d+)\)")


def resolve(reference, meta):
    if (reference, meta) in METADATA:
        return METADATA[(reference, meta)]
    target = SIMPLE.get(reference)
    if target is None:
        raise KeyError(f"no mapping for {reference}:{meta}")
    return target


def java_target(target):
    if isinstance(target, list):
        return "List.of(" + ", ".join(f'"{entry}"' for entry in target) + ")"
    return f'"{target}"'


def convert_expression(expression):
    base = BASE.match(expression.strip())
    if not base:
        raise ValueError(expression)
    if base.group(1):
        source = resolve(base.group(1), int(base.group(2)))
        result = f"copy({java_target(source)})"
    else:
        result = "tags()"
    for method, aspect, amount in OPERATION.findall(expression):
        result += f".{method}(Aspect.{aspect}, {amount})"
    return result


def main():
    text = CONFIG.read_text(encoding="utf-8")
    start = text.index("public static void initTags()")
    end = text.index("for (EnumTag tag : EnumTag.values())", start)
    body = text[start:end]
    potion_start = body.index("Map lhm")
    potion_end = body.index("}", body.index("for (int var7")) + 1
    body = body[:potion_start] + body[potion_end:]

    lines = []
    for method, reference, meta, expression in CALL.findall(body):
        target = resolve(reference, int(meta))
        call = "complex" if method == "registerComplexObjectTag" else "register"
        lines.append(f"        {call}({java_target(target)}, {convert_expression(expression)});")

    java = f"""package thaumcraft.aspect;

import java.util.List;

public final class ConfigAspects extends AspectRegistrar {{
    public ConfigAspects(AspectRegistry registry) {{
        super(registry);
    }}

    public void registerAll() {{
{chr(10).join(lines)}
        registerEssences();
    }}
}}
"""
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(java, encoding="utf-8")
    print(f"converted {len(lines)} registrations")


if __name__ == "__main__":
    main()
