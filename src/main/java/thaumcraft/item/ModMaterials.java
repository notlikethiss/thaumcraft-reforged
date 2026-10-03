package thaumcraft.item;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModTags;

public final class ModMaterials {
    public static final ToolMaterial THAUMIUM_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 400, 7.0F, 2.0F, 22, ModTags.THAUMIUM_TOOL_MATERIALS);
    public static final ToolMaterial ELEMENTAL_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 10.0F, 3.0F, 18, ModTags.THAUMIUM_TOOL_MATERIALS);

    public static final ArmorMaterial THAUMIUM_ARMOR = new ArmorMaterial(
        25, defense(2, 5, 6, 2), 25, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ModTags.REPAIRS_THAUMIUM_ARMOR, asset("thaumium")
    );
    public static final ArmorMaterial ROBE_ARMOR = special("robes");
    public static final ArmorMaterial GOGGLES_ARMOR = special("goggles", ModTags.REPAIRS_GOGGLES);
    public static final ArmorMaterial TRAVELLER_ARMOR = special("boots_traveller");
    public static final ArmorMaterial HARNESS_ARMOR = special("hover_harness");

    private ModMaterials() {
    }

    private static ArmorMaterial special(String assetName) {
        return special(assetName, ModTags.REPAIRS_SPECIAL_ARMOR);
    }

    private static ArmorMaterial special(String assetName, TagKey<Item> repairs) {
        return new ArmorMaterial(25, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, repairs, asset(assetName));
    }

    private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helmet) {
        return Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, legs, ArmorType.CHESTPLATE, chest, ArmorType.HELMET, helmet, ArmorType.BODY, chest);
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id(name));
    }
}
