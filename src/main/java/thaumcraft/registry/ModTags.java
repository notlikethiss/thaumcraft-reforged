package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import thaumcraft.Thaumcraft;

public final class ModTags {
    public static final TagKey<Item> THAUMIUM_TOOL_MATERIALS = item("thaumium_tool_materials");
    public static final TagKey<Item> REPAIRS_THAUMIUM_ARMOR = item("repairs_thaumium_armor");
    public static final TagKey<Item> REPAIRS_SPECIAL_ARMOR = item("repairs_special_armor");
    public static final TagKey<Block> GOLEM_HARVESTABLE = block("golem_harvestable");
    public static final TagKey<Block> PORTABLE_HOLE_BLACKLIST = block("portable_hole_blacklist");

    private ModTags() {
    }

    private static TagKey<Item> item(String name) {
        return TagKey.create(Registries.ITEM, Thaumcraft.id(name));
    }

    private static TagKey<Block> block(String name) {
        return TagKey.create(Registries.BLOCK, Thaumcraft.id(name));
    }
}
