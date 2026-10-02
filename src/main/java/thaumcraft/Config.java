package thaumcraft;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final int NODE_REFRESH = 10;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue NODE_RARITY = BUILDER
        .translation("thaumcraft.configuration.node_rarity")
        .defineInRange("general.node_rarity", 23, 1, 10000);
    public static final ModConfigSpec.IntValue SPECIAL_NODE_RARITY = BUILDER
        .translation("thaumcraft.configuration.special_node_rarity")
        .defineInRange("general.special_node_rarity", 75, 1, 10000);
    public static final ModConfigSpec.BooleanValue DISPLAY_ASPECTS = BUILDER
        .translation("thaumcraft.configuration.display_aspects")
        .define("general.display_aspects", false);
    public static final ModConfigSpec.BooleanValue ALLOW_CHEAT_SHEET = BUILDER
        .translation("thaumcraft.configuration.allow_cheat_sheet")
        .define("general.allow_cheat_sheet", false);
    public static final ModConfigSpec.BooleanValue ALLOW_WARDED_STONE = BUILDER
        .translation("thaumcraft.configuration.allow_warded_stone")
        .define("general.allow_warded_stone", true);
    public static final ModConfigSpec.BooleanValue GOLEM_CHEST_INTERACT = BUILDER
        .translation("thaumcraft.configuration.golem_chest_interact")
        .define("general.golem_chest_interact", true);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PORTABLE_HOLE_BLACKLIST = BUILDER
        .translation("thaumcraft.configuration.portablehole_blacklist")
        .defineListAllowEmpty("general.portablehole_blacklist", List.of("minecraft:iron_door"), () -> "", value -> value instanceof String);
    public static final ModConfigSpec.IntValue RESEARCH_EXP_CHANCE = BUILDER
        .translation("thaumcraft.configuration.base_exp_chance")
        .defineInRange("research.base_exp_chance", 33, 0, 100);
    public static final ModConfigSpec.IntValue RESEARCH_SAFE_CHANCE = BUILDER
        .translation("thaumcraft.configuration.base_safe_chance")
        .defineInRange("research.base_safe_chance", 15, 0, 100);
    public static final ModConfigSpec.IntValue RESEARCH_SAFE_LOSS = BUILDER
        .translation("thaumcraft.configuration.base_safe_loss")
        .defineInRange("research.base_safe_loss", 25, 0, 100);
    public static final ModConfigSpec.IntValue RESEARCH_THOROUGH_CHANCE = BUILDER
        .translation("thaumcraft.configuration.base_thorough_chance")
        .defineInRange("research.base_thorough_chance", 55, 0, 100);
    public static final ModConfigSpec.IntValue RESEARCH_THOROUGH_LOSS = BUILDER
        .translation("thaumcraft.configuration.base_thorough_loss")
        .defineInRange("research.base_thorough_loss", 75, 0, 100);
    public static final ModConfigSpec.BooleanValue SPAWN_ANGRY_ZOMBIES = BUILDER
        .translation("thaumcraft.configuration.spawn_angry_zombies")
        .define("monster_spawning.spawn_angry_zombies", true);
    public static final ModConfigSpec.BooleanValue SPAWN_FIRE_BATS = BUILDER
        .translation("thaumcraft.configuration.spawn_fire_bats")
        .define("monster_spawning.spawn_fire_bats", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean wardedStone() {
        return ALLOW_WARDED_STONE.getAsBoolean();
    }
}
