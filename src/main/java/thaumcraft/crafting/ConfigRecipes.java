package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class ConfigRecipes {
    public static final String[] WOOL_COLORS = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
    };
    public static final String[] DYE_COLORS = {
        "black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray",
        "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white",
    };
    public static final String[] GOLEM_CORES = {"basic", "speed", "intelligence", "perception", "strength"};
    public static final String[] SHARDS = {"air", "fire", "water", "earth", "vis", "dull"};
    private static final String EMPTY_SPACE = "thaumcraft:hole";

    private ConfigRecipes() {
    }

    private static AspectList tags() {
        return new AspectList();
    }

    private static String tc(String name) {
        return "thaumcraft:" + name;
    }

    private static String mc(String name) {
        return "minecraft:" + name;
    }

    private static String shard(int index) {
        return tc(SHARDS[index] + "_shard");
    }

    private static String core(int index) {
        return tc("golem_core_" + GOLEM_CORES[index]);
    }

    private static TcResult result(String id) {
        return TcResult.of(id);
    }

    private static TcResult result(String id, int count) {
        return TcResult.of(id, count);
    }

    private static void arcane(String key, String recipeKey, int cost, TcResult output, Object... pattern) {
        ThaumcraftRecipes.addShaped(WorkbenchRecipe.Kind.ARCANE, key, recipeKey, cost, tags(), output, pattern);
    }

    private static void arcaneShapeless(String key, String recipeKey, int cost, TcResult output, Object... ingredients) {
        ThaumcraftRecipes.addShapeless(WorkbenchRecipe.Kind.ARCANE, key, recipeKey, cost, tags(), output, ingredients);
    }

    private static void infusion(String key, String recipeKey, int cost, AspectList aspects, TcResult output, Object... pattern) {
        ThaumcraftRecipes.addShaped(WorkbenchRecipe.Kind.INFUSION, key, recipeKey, cost, aspects, output, pattern);
    }

    private static void infusionShapeless(String key, String recipeKey, int cost, AspectList aspects, TcResult output, Object... ingredients) {
        ThaumcraftRecipes.addShapeless(WorkbenchRecipe.Kind.INFUSION, key, recipeKey, cost, aspects, output, ingredients);
    }

    private static void vanilla(String recipeKey, String recipeId) {
        ThaumcraftRecipes.addResearchRecipe(recipeKey, new RecipeReference.Vanilla(recipeId));
    }

    private static void compound(String recipeKey, int width, int height, int depth, int cost, String... blocks) {
        ThaumcraftRecipes.addResearchRecipe(recipeKey, new RecipeReference.Compound(width, height, depth, cost, Arrays.asList(blocks)));
    }

    private static void fake(String recipeKey, WorkbenchRecipe.Kind kind, int cost, TcResult output, Object... pattern) {
        int index = 0;
        List<String> rows = new ArrayList<>();
        while (pattern[index] instanceof String row) {
            rows.add(row);
            index++;
        }
        List<Character> symbols = new ArrayList<>();
        List<TcIngredient> values = new ArrayList<>();
        while (index < pattern.length) {
            symbols.add((Character) pattern[index]);
            Object value = pattern[index + 1];
            values.add(value instanceof TcIngredient ingredient ? ingredient : TcIngredient.parse((String) value));
            index += 2;
        }
        int width = rows.getFirst().length();
        List<TcIngredient> grid = new ArrayList<>();
        for (String row : rows) {
            for (char symbol : row.toCharArray()) {
                int position = symbols.indexOf(symbol);
                grid.add(position < 0 ? null : values.get(position));
            }
        }
        ThaumcraftRecipes.addResearchRecipe(recipeKey, new RecipeReference.FakeShaped(kind, width, rows.size(), grid, output, cost));
    }

    public static void registerAll() {
        ThaumcraftRecipes.addSmeltingBonus(mc("gold_ingot"), mc("gold_nugget"));
        ThaumcraftRecipes.addSmeltingBonus(mc("iron_ingot"), mc("iron_nugget"));
        ThaumcraftRecipes.addSmeltingBonus(mc("copper_ingot"), mc("copper_nugget"));
        ThaumcraftRecipes.addSmeltingBonus(tc("quicksilver"), tc("quicksilver_drop"));
        ThaumcraftRecipes.addSmeltingBonus(mc("cooked_chicken"), tc("chicken_nugget"));
        ThaumcraftRecipes.addSmeltingBonus(mc("cooked_beef"), tc("beef_nugget"));
        ThaumcraftRecipes.addSmeltingBonus(mc("cooked_porkchop"), tc("pork_nugget"));

        fake("MARKERBLOCK", null, 0, result(tc("white_marker"), 2), " W ", "WSW", " W ", 'W', "#minecraft:planks", 'S', "#minecraft:wool");
        vanilla("KNOWLEDGE", tc("research_notes_from_fragments"));
        vanilla("PHIAL", tc("essentia_phial"));
        fake("TABLE", null, 0, result(tc("table")), "SSS", "W W", 'S', "#minecraft:wooden_slabs", 'W', "#minecraft:planks");
        compound("THAUMONOMICON", 1, 2, 1, 25, tc("wand_apprentice"), mc("bookshelf"));
        compound("CRUCIBLE", 1, 2, 1, 25, tc("wand_apprentice"), mc("cauldron"));
        compound("ACTABLE", 1, 2, 1, 0, tc("wand_apprentice"), tc("table"));
        compound("RESTABLE", 1, 2, 2, 0, null, tc("scribing_tools"), tc("table"), tc("table"));

        arcane("TALLOW", "TALLOW", 3, result(tc("white_tallow_candle"), 2), "S", "T", "T", 'S', mc("string"), 'T', tc("magic_tallow"));
        for (int color = 1; color < 16; color++) {
            arcaneShapeless("TALLOW", null, 1, result(tc(WOOL_COLORS[color] + "_tallow_candle")), "#c:dyes/" + WOOL_COLORS[color], tc("white_tallow_candle"));
        }
        arcaneShapeless("TALLOW", null, 1, result(tc("white_tallow_candle")), "#c:dyes/white", "#thaumcraft:tallow_candles");
        arcane("MAGBLOCK", "MAGBLOCK", 20, result(tc("arcane_stone"), 8), "TST", "SSS", "TST", 'S', mc("stone"), 'T', tc("thaumium_ingot"));
        arcane("MAGBLOCK", "ARCWOOD1", 20, result(tc("arcane_wood"), 2), "WWW", "WWW", "WWW", 'W', "#minecraft:logs");
        arcane("MAGBLOCK", "ARCWOOD2", 20, result(tc("arcane_wood"), 4), "WW", "WW", 'W', tc("greatwood_log"));
        compound("INFBENCH", 2, 1, 2, 25, tc("arcane_stone"), tc("arcane_stone"), tc("arcane_stone"), tc("arcane_stone"));
        compound(
            "INFERNALFURNACE", 3, 3, 3, 100,
            mc("nether_bricks"), mc("obsidian"), mc("nether_bricks"), mc("obsidian"), EMPTY_SPACE, mc("obsidian"), mc("nether_bricks"), mc("obsidian"), mc("nether_bricks"),
            mc("nether_bricks"), mc("obsidian"), mc("nether_bricks"), mc("obsidian"), mc("lava_bucket"), mc("iron_bars"), mc("nether_bricks"), mc("obsidian"), mc("nether_bricks"),
            mc("nether_bricks"), mc("obsidian"), mc("nether_bricks"), mc("obsidian"), mc("obsidian"), mc("obsidian"), mc("nether_bricks"), mc("obsidian"), mc("nether_bricks")
        );
        infusion(
            "CRYSTALCORE", "CRYSTALCORE", 50,
            tags().add(Aspect.VOID, 8).add(Aspect.MAGIC, 8).add(Aspect.FLUX, 8).add(Aspect.ELDRITCH, 8),
            result(tc("crystal_core")), " C ", "CNC", " C ", 'C', tc("mixed_crystal_cluster"), 'N', mc("nether_star")
        );
        compound(
            "NODEMAGNET", 3, 3, 3, 300,
            EMPTY_SPACE, null, EMPTY_SPACE, null, tc("crystal_core"), null, EMPTY_SPACE, null, EMPTY_SPACE,
            tc("obsidian_totem"), null, tc("obsidian_totem"), null, null, null, tc("obsidian_totem"), null, tc("obsidian_totem"),
            tc("obsidian_totem"), null, tc("obsidian_totem"), null, null, null, tc("obsidian_totem"), null, tc("obsidian_totem")
        );
        arcane("ENCHFABRIC", "ENCHFABRIC", 5, result(tc("enchanted_fabric")), " S ", "SCS", " S ", 'S', mc("string"), 'C', "#minecraft:wool");
        arcane("ROBES", "ROBE_CHEST", 50, result(tc("robe_chestplate")), "I I", "III", "III", 'I', tc("enchanted_fabric"));
        arcane("ROBES", "ROBE_LEGS", 50, result(tc("robe_leggings")), "III", "I I", "I I", 'I', tc("enchanted_fabric"));
        arcane("ROBES", "ROBE_FEET", 40, result(tc("robe_boots")), "I I", "I I", 'I', tc("enchanted_fabric"));
        infusion(
            "HUNGRYCHEST", "HUNGRYCHEST", 25, tags().add(Aspect.VOID, 8).add(Aspect.MOTION, 8).add(Aspect.SPIRIT, 4),
            result(tc("hungry_chest")), "WTW", "W W", "WWW", 'W', tc("arcane_wood"), 'T', "#minecraft:wooden_trapdoors"
        );
        infusion(
            "LEVITATOR", "LEVITATOR", 50, tags().add(Aspect.EARTH, 8).add(Aspect.MOTION, 8).add(Aspect.MECHANISM, 8).add(Aspect.FLIGHT, 8),
            result(tc("arcane_levitator")), "WEW", "BNB", "WAW",
            'W', tc("arcane_wood"), 'E', shard(3), 'A', shard(0), 'N', tc("nitor"), 'B', tc("arcane_stone")
        );
        infusion(
            "ARCANEEAR", "ARCANEEAR", 30, tags().add(Aspect.SOUND, 16).add(Aspect.WIND, 8).add(Aspect.MECHANISM, 8),
            result(tc("arcane_ear")), "GIG", "GBG", "WRW",
            'W', tc("arcane_wood"), 'R', mc("redstone"), 'I', mc("iron_ingot"), 'G', mc("gold_ingot"), 'B', tc("zombie_brain")
        );
        infusion(
            "PORTABLEHOLE", "PORTABLEHOLE", 200, tags().add(Aspect.VOID, 24).add(Aspect.ELDRITCH, 24).add(Aspect.EXCHANGE, 16),
            result(tc("portable_hole")), " C ", "CEC", " C ", 'C', tc("enchanted_fabric"), 'E', mc("ender_pearl")
        );
        infusionShapeless(
            "MIRROR", "MIRRORGLASS", 10, tags().add(Aspect.CRYSTAL, 4).add(Aspect.VISION, 4),
            result(tc("mirrored_glass")), tc("quicksilver"), mc("glass_pane")
        );
        infusion(
            "MIRROR", "MIRROR", 100, tags().add(Aspect.MOTION, 16),
            result(tc("magic_mirror"), 2), "IGI", "GPG", "IGI", 'G', tc("mirrored_glass"), 'P', tc("portable_hole"), 'I', mc("gold_ingot")
        );
        infusion(
            "HANDMIRROR", "HANDMIRROR", 150, tags().add(Aspect.TOOL, 16),
            result(tc("hand_mirror")), " M", "W ", 'M', tc("magic_mirror"), 'W', tc("wand_apprentice")
        );
        arcane("THAUMOMETER", "THAUMOMETER", 20, result(tc("thaumometer")), " G ", "GCG", " G ", 'G', mc("gold_ingot"), 'C', shard(2));
        infusion(
            "GOGGLES", "GOGGLES", 60, tags().add(Aspect.VISION, 8).add(Aspect.MAGIC, 8).add(Aspect.FLUX, 8),
            result(tc("goggles_of_revealing")), "LGL", "L L", "TGT", 'T', tc("thaumometer"), 'G', mc("gold_ingot"), 'L', mc("leather")
        );
        infusion(
            "ARCANEDOOR", "ARCANEDOOR", 50, tags().add(Aspect.MECHANISM, 8).add(Aspect.KNOWLEDGE, 4).add(Aspect.CONTROL, 4),
            result(tc("arcane_door")), "TDT", "TBT", "TDT", 'T', tc("thaumium_ingot"), 'B', tc("zombie_brain"), 'D', tc("greatwood_log")
        );
        arcane("ARCANEDOOR", "IRONKEY", 10, result(tc("iron_arcane_key"), 2), "NNI", "N  ", 'I', mc("iron_ingot"), 'N', mc("iron_nugget"));
        arcane("ARCANEDOOR", "GOLDKEY", 10, result(tc("gold_arcane_key"), 2), "NNI", "N  ", 'I', mc("gold_ingot"), 'N', mc("gold_nugget"));
        for (int color = 0; color < 16; color++) {
            arcane(
                "ARCANEDOOR", null, 10, result(tc(DYE_COLORS[color] + "_warded_stone"), 32), "SSS", "TBT", "SDS",
                'S', mc("stone"), 'T', tc("arcane_stone"), 'B', tc("zombie_brain"), 'D', "#c:dyes/" + DYE_COLORS[color]
            );
        }
        fake(
            "WARDEDSTONE", WorkbenchRecipe.Kind.ARCANE, 10, result(tc("black_warded_stone"), 32), "SSS", "TBT", "SDS",
            'S', mc("stone"), 'T', tc("arcane_stone"), 'B', tc("zombie_brain"), 'D', "#c:dyes"
        );
        arcane(
            "ARCANEDOOR", "WARDEDGLASS", 10, result(tc("warded_glass"), 12), "GGG", "TBT", "GGG",
            'G', mc("glass"), 'T', tc("thaumium_ingot"), 'B', tc("zombie_brain")
        );
        infusion(
            "ARCANEPLATE", "ARCANEPLATE", 25, tags().add(Aspect.MECHANISM, 4).add(Aspect.KNOWLEDGE, 4).add(Aspect.CONTROL, 4),
            result(tc("arcane_pressure_plate")), " B ", "TDT", 'T', tc("thaumium_ingot"), 'B', tc("zombie_brain"), 'D', tc("arcane_wood")
        );
        arcane("JAR", "JAR", 20, result(tc("warded_jar")), "GWG", "G G", "GGG", 'G', mc("glass_pane"), 'W', tc("arcane_wood"));
        infusion(
            "JARBRAIN", "JARBRAIN", 66, tags().add(Aspect.EVIL, 8).add(Aspect.KNOWLEDGE, 8).add(Aspect.SPIRIT, 8),
            result(tc("brain_jar")), "EBE", " J ", " W ",
            'E', mc("spider_eye"), 'B', tc("zombie_brain"), 'W', mc("water_bucket"), 'J', tc("warded_jar")
        );
        String[] clusters = {"air", "fire", "water", "earth", "vis"};
        for (int index = 0; index < 5; index++) {
            infusion(
                "CRYSTALCLUSTER", "C_CRYSTALCLUSTER" + index, 100, tags().add(Aspect.CRYSTAL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.MAGIC, 8),
                result(tc(clusters[index] + "_crystal_cluster")), " C ", "CCC", " C ", 'C', shard(index)
            );
        }
        infusion(
            "CRYSTALCLUSTER", "C_CRYSTALCLUSTER5", 100, tags().add(Aspect.CRYSTAL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.MAGIC, 8),
            result(tc("mixed_crystal_cluster")), " 0 ", "241", " 3 ",
            '0', shard(0), '1', shard(1), '2', shard(2), '3', shard(3), '4', shard(4)
        );
        infusion(
            "CRYSTALCAPACITOR", "CRYSTALCAPACITOR", 100, tags().add(Aspect.EXCHANGE, 16).add(Aspect.MAGIC, 16).add(Aspect.CRYSTAL, 16),
            result(tc("crystal_capacitor")), "CCC", "CWC", "CCC", 'C', shard(5), 'W', tc("arcane_wood")
        );
        infusion(
            "BASICFLUX", "FLUXFILTER", 25, tags().add(Aspect.PURE, 8).add(Aspect.EXCHANGE, 8),
            result(tc("flux_filter")), "GFG", 'F', tc("silverwood_log"), 'G', mc("gold_ingot")
        );
        infusion(
            "BASICFLUX", "ARCANEALEMBIC", 75, tags().add(Aspect.WIND, 8).add(Aspect.WATER, 8).add(Aspect.CRYSTAL, 8),
            result(tc("alembic")), "GFG", "J G", "B  ",
            'F', tc("flux_filter"), 'J', tc("warded_jar"), 'B', mc("brewing_stand"), 'G', mc("gold_ingot")
        );
        infusion(
            "GOLEMANCY", "GOLEMANCY", 30, tags().add(Aspect.MOTION, 8).add(Aspect.CONTROL, 8),
            result(core(0)), " B ", "BTB", " B ", 'T', tc("nitor"), 'B', mc("brick")
        );
        infusionShapeless("GOLEMFAST", "GOLEMFAST", 20, tags().add(Aspect.MOTION, 12), result(core(1)), core(0), shard(0));
        infusionShapeless("GOLEMSMART", "GOLEMSMART", 20, tags().add(Aspect.KNOWLEDGE, 12), result(core(2)), core(0), shard(1));
        infusionShapeless("GOLEMSIGHT", "GOLEMSIGHT", 20, tags().add(Aspect.VISION, 12), result(core(3)), core(0), shard(2));
        infusionShapeless("GOLEMSTRONG", "GOLEMSTRONG", 20, tags().add(Aspect.POWER, 12), result(core(4)), core(0), shard(3));
        for (int index = 0; index <= 4; index++) {
            infusion(
                "GOLEMANCY", "C_GOLEMWOOD" + index, 40, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8),
                TcResult.golem(tc("wood_golem"), index), "MCM", " M ", "M M", 'C', core(index), 'M', tc("greatwood_log")
            );
        }
        for (int index = 0; index <= 4; index++) {
            infusion(
                "GOLEMSTONE", "C_GOLEMSTONE" + index, 50, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8),
                TcResult.golem(tc("stone_golem"), index), "MCM", " M ", "M M", 'C', core(index), 'M', mc("stone_bricks")
            );
        }
        for (int index = 0; index <= 4; index++) {
            infusion(
                "GOLEMCLAY", "C_GOLEMCLAY" + index, 50, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8),
                TcResult.golem(tc("clay_golem"), index), "MCM", " M ", "M M", 'C', core(index), 'M', mc("brick")
            );
        }
        infusion(
            "GOLEMSTONEADV", "GOLEMSTONEADV", 100, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8).add(Aspect.KNOWLEDGE, 16),
            TcResult.golem(tc("advanced_stone_golem"), 0), " B ", " S ", " G ",
            'G', TcIngredient.golem(tc("stone_golem"), 2), 'S', mc("slime_ball"), 'B', tc("brain_jar")
        );
        infusion(
            "GOLEMCLAYADV", "GOLEMCLAYADV", 100, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8).add(Aspect.KNOWLEDGE, 16),
            TcResult.golem(tc("advanced_clay_golem"), 0), " B ", " S ", " G ",
            'G', TcIngredient.golem(tc("clay_golem"), 2), 'S', mc("slime_ball"), 'B', tc("brain_jar")
        );
        infusion(
            "GOLEMTALLOWADV", "GOLEMTALLOWADV", 100, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8).add(Aspect.KNOWLEDGE, 16),
            TcResult.golem(tc("decanting_golem"), 0), " B ", " S ", "JG ",
            'G', TcIngredient.golem(tc("tallow_golem"), 4), 'S', mc("slime_ball"), 'B', tc("brain_jar"), 'J', tc("warded_jar")
        );
        int count = 0;
        for (int index = 0; index <= 4; index++) {
            if (index != 2) {
                infusion(
                    "GOLEMTALLOW", "C_GOLEMTALLOW" + count, 40, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8),
                    TcResult.golem(tc("tallow_golem"), index), "MCM", " M ", "M M", 'C', core(index), 'M', tc("magic_tallow")
                );
                count++;
            }
        }
        count = 0;
        for (int index = 0; index <= 4; index++) {
            if (index != 4) {
                infusion(
                    "GOLEMSTRAW", "C_GOLEMSTRAW" + count, 50, tags().add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8),
                    TcResult.golem(tc("straw_golem"), index), "MCM", " M ", "M M", 'C', core(index), 'M', mc("wheat")
                );
                count++;
            }
        }
        for (int index = 0; index <= 4; index++) {
            infusion(
                "GOLEMANCYADV", "C_GOLEMIRON" + index, 50,
                tags().add(Aspect.SPIRIT, 12).add(Aspect.LIFE, 12).add(Aspect.ARMOR, 12).add(Aspect.WEAPON, 12),
                TcResult.golem(tc("iron_guardian_golem"), index), "MCM", " M ", "M M", 'C', core(index), 'M', mc("iron_ingot")
            );
        }
        infusion("TINYHAT", "TINYHAT", 25, tags().add(Aspect.POWER, 8), result(tc("golem_top_hat")), " C ", " G ", "CCC", 'C', mc("black_wool"), 'G', mc("gold_ingot"));
        infusion("TINYFEZ", "TINYFEZ", 25, tags().add(Aspect.LIFE, 8), result(tc("golem_fez")), "CCS", "CCS", "  S", 'C', mc("red_wool"), 'S', mc("string"));
        infusion("TINYBOWTIE", "TINYBOWTIE", 25, tags().add(Aspect.MOTION, 8), result(tc("golem_bowtie")), "CSC", "C C", 'C', mc("black_wool"), 'S', mc("string"));
        infusion("TINYGLASSES", "TINYGLASSES", 25, tags().add(Aspect.VISION, 8), result(tc("golem_spectacles")), "GIG", 'G', mc("glass"), 'I', mc("iron_ingot"));
        infusion(
            "TINYDART", "TINYDART", 50, tags().add(Aspect.WEAPON, 16).add(Aspect.WIND, 16).add(Aspect.MECHANISM, 16),
            result(tc("golem_dart_launcher")), "AIA", "ADA", "AIA", 'I', mc("iron_ingot"), 'D', mc("dispenser"), 'A', mc("arrow")
        );
        infusion(
            "TINYVISOR", "TINYVISOR", 50, tags().add(Aspect.DEATH, 16).add(Aspect.VISION, 16).add(Aspect.ARMOR, 8),
            result(tc("golem_visor")), "IHI", 'I', mc("iron_ingot"), 'H', mc("iron_helmet")
        );
        infusion(
            "TINYARMOR", "TINYARMOR", 50, tags().add(Aspect.METAL, 16).add(Aspect.ARMOR, 16),
            result(tc("golem_iron_plating")), "I I", "IAI", 'I', mc("iron_ingot"), 'A', mc("iron_chestplate")
        );
        infusion(
            "ARCANEBELLOWS", "ARCANEBELLOWS", 50, tags().add(Aspect.MOTION, 16).add(Aspect.WIND, 24),
            result(tc("arcane_bellows")), "WW ", "LCI", "WW ",
            'W', tc("arcane_wood"), 'C', shard(0), 'I', mc("iron_ingot"), 'L', mc("leather")
        );
        vanilla("WAND", tc("wand_apprentice"));
        infusion(
            "UTFT", "WANDADEPT", 50, tags().add(Aspect.MAGIC, 16),
            result(tc("wand_adept")), " A ", "WSF", " E ",
            'S', tc("wand_apprentice"), 'A', shard(0), 'F', shard(1), 'W', shard(2), 'E', shard(3)
        );
        infusion("TTOE", "WANDMAGE", 250, tags().add(Aspect.MAGIC, 32), result(tc("wand_thaumaturge")), " N", "S ", 'S', tc("wand_adept"), 'N', mc("nether_star"));
        infusion(
            "HELLROD", "HELLROD", 250, tags().add(Aspect.EVIL, 32).add(Aspect.FIRE, 32).add(Aspect.BEAST, 32),
            result(tc("hellrod")), " GN", " SG", "W  ",
            'S', tc("wand_fire"), 'W', tc("wand_adept"), 'N', mc("tnt"), 'G', mc("gold_ingot")
        );
        String[][] wands = {
            {"WANDFIRE", "wand_fire", "1"},
            {"WANDFROST", "wand_frost", "2"},
            {"WANDLIGHTNING", "wand_lightning", "0"},
            {"WANDEXCHANGE", "wand_equal_trade", "4"},
            {"WANDEXCAVATE", "wand_excavation", "3"},
        };
        Aspect[][] wandAspects = {
            {Aspect.FIRE, Aspect.WEAPON},
            {Aspect.COLD, Aspect.WEAPON},
            {Aspect.POWER, Aspect.WEAPON},
            {Aspect.EXCHANGE, Aspect.TOOL},
            {Aspect.METAL, Aspect.TOOL},
        };
        for (int index = 0; index < wands.length; index++) {
            infusion(
                wands[index][0], wands[index][0], 50, tags().add(wandAspects[index][0], 16).add(wandAspects[index][1], 4),
                result(tc(wands[index][1])), "SS", "WS", 'W', tc("wand_apprentice"), 'S', shard(Integer.parseInt(wands[index][2]))
            );
        }
        infusion(
            "ARCANEBORE", "ARCANEBORE", 125,
            tags().add(Aspect.POWER, 32).add(Aspect.METAL, 64).add(Aspect.MOTION, 16).add(Aspect.MECHANISM, 32).add(Aspect.VOID, 24),
            result(tc("arcane_bore")), "GPG", "WHW", "GJG",
            'W', tc("arcane_wood"), 'P', mc("piston"), 'G', mc("gold_ingot"), 'H', tc("portable_hole"), 'J', tc("warded_jar")
        );
        arcane(
            "ARCANEBORE", "ARCANEBOREBASE", 50, result(tc("arcane_bore_base")), "WIW", "IDI", "WIW",
            'W', tc("arcane_wood"), 'I', mc("iron_ingot"), 'D', mc("dispenser")
        );
        for (String piece : new String[]{"HELM", "CHEST", "LEGS", "FEET", "SHOVEL", "PICKAXE", "AXE", "HOE", "SWORD"}) {
            vanilla("THAUMIUM_" + piece, tc("thaumium_" + piece.toLowerCase()));
        }
        infusion(
            "ELEMENTALAXE", "ELEMENTALAXE", 100, tags().add(Aspect.WATER, 16).add(Aspect.TOOL, 16).add(Aspect.MOTION, 8).add(Aspect.WOOD, 8),
            result(tc("elemental_axe")), "T", "W", 'T', tc("thaumium_axe"), 'W', tc("wand_frost")
        );
        infusion(
            "ELEMENTALPICKAXE", "ELEMENTALPICKAXE", 100, tags().add(Aspect.FIRE, 16).add(Aspect.TOOL, 16).add(Aspect.VISION, 8).add(Aspect.METAL, 8),
            result(tc("elemental_pickaxe")), "T", "W", 'T', tc("thaumium_pickaxe"), 'W', tc("wand_fire")
        );
        infusion(
            "ELEMENTALSWORD", "ELEMENTALSWORD", 100, tags().add(Aspect.WIND, 16).add(Aspect.WEAPON, 16).add(Aspect.MOTION, 8).add(Aspect.DESTRUCTION, 8),
            result(tc("elemental_sword")), "T", "W", 'T', tc("thaumium_sword"), 'W', tc("wand_lightning")
        );
        infusion(
            "ELEMENTALSHOVEL", "ELEMENTALSHOVEL", 100, tags().add(Aspect.EARTH, 16).add(Aspect.TOOL, 16).add(Aspect.CRAFT, 8).add(Aspect.DESTRUCTION, 8),
            result(tc("elemental_shovel")), "T", "W", 'T', tc("thaumium_shovel"), 'W', tc("wand_excavation")
        );
        infusion(
            "ELEMENTALHOE", "ELEMENTALHOE", 100, tags().add(Aspect.PLANT, 16).add(Aspect.TOOL, 16).add(Aspect.CROP, 8).add(Aspect.LIFE, 8),
            result(tc("elemental_hoe")), "T", "W", 'T', tc("thaumium_hoe"), 'W', tc("wand_equal_trade")
        );
        infusion(
            "BOOTSTRAVELLER", "BOOTSTRAVELLER", 100,
            tags().add(Aspect.MOTION, 24).add(Aspect.FLIGHT, 16).add(Aspect.ARMOR, 8).add(Aspect.EARTH, 8).add(Aspect.WATER, 8),
            result(tc("boots_traveller")), "S S", "C C", "L L", 'S', shard(0), 'C', tc("enchanted_fabric"), 'L', mc("leather")
        );
        infusion(
            "TRAVELSTONE", "TRAVELSTONE", 100, tags().add(Aspect.MOTION, 16).add(Aspect.FLIGHT, 8).add(Aspect.ROCK, 8).add(Aspect.EARTH, 8),
            result(tc("travel_paving_stone"), 8), "Q", "S", "B", 'S', shard(3), 'Q', mc("chiseled_quartz_block"), 'B', tc("arcane_stone")
        );
        vanilla("SCRIBE2", tc("scribing_tools_from_phial"));
        vanilla("SCRIBE1", tc("scribing_tools"));
        infusion(
            "\t", "HOVERHARNESS", 500,
            tags().add(Aspect.FLIGHT, 64).add(Aspect.POWER, 24).add(Aspect.WIND, 24).add(Aspect.MECHANISM, 32).add(Aspect.ARMOR, 16),
            result(tc("hover_harness")), "WRW", "GAG", "CIC",
            'C', shard(0), 'W', tc("arcane_wood"), 'R', mc("comparator"), 'G', mc("gold_ingot"), 'I', mc("iron_ingot"), 'A', mc("leather_chestplate")
        );

        ThaumcraftRecipes.addCrucible("ALUMENTUM", "ALUMENTUM", result(tc("alumentum")), 5, tags().merge(Aspect.POWER, 6).merge(Aspect.FIRE, 6).merge(Aspect.DESTRUCTION, 3));
        ThaumcraftRecipes.addCrucible("GUNPOWDER", "GUNPOWDER", result(mc("gunpowder")), 5, tags().merge(Aspect.FIRE, 6).merge(Aspect.DESTRUCTION, 6));
        ThaumcraftRecipes.addCrucible("NITOR", "NITOR", result(tc("nitor")), 5, tags().merge(Aspect.POWER, 4).merge(Aspect.FIRE, 4).merge(Aspect.LIGHT, 6));
        ThaumcraftRecipes.addCrucible("THAUMIUM", "THAUMIUM", result(tc("thaumium_ingot")), 5, tags().merge(Aspect.METAL, 8).merge(Aspect.MAGIC, 4));
        ThaumcraftRecipes.addCrucible("TALLOW", "TALLOW", result(tc("magic_tallow")), 5, tags().merge(Aspect.FLESH, 4));
        ThaumcraftRecipes.addCrucible("BASTRANS", "BASTRANS", result(mc("gold_nugget"), 2), 5, tags().merge(Aspect.METAL, 2).merge(Aspect.VALUABLE, 1));
        ThaumcraftRecipes.addCrucible("TRANSCOPPER", "TRANSCOPPER", result(mc("copper_nugget"), 3), 5, tags().merge(Aspect.METAL, 3).merge(Aspect.LIFE, 1));
        ThaumcraftRecipes.addCrucible("TRANSTIN", "TRANSTIN", result(tc("tin_nugget"), 3), 5, tags().merge(Aspect.METAL, 3).merge(Aspect.CRYSTAL, 1));
        ThaumcraftRecipes.addCrucible("TRANSSILVER", "TRANSSILVER", result(tc("silver_nugget"), 3), 5, tags().merge(Aspect.METAL, 3).merge(Aspect.EXCHANGE, 1));
        ThaumcraftRecipes.addCrucible("TRANSLEAD", "TRANSLEAD", result(tc("lead_nugget"), 3), 5, tags().merge(Aspect.METAL, 3).merge(Aspect.VOID, 1));
        ThaumcraftRecipes.addCrucible("TRANSIRON", "TRANSIRON", result(mc("iron_nugget"), 2), 5, tags().merge(Aspect.METAL, 2));
    }
}
