package thaumcraft.item;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModTags;

public final class ModMaterials {
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Thaumcraft.MODID);

    public static final int ARMOR_DURABILITY = 25;

    public static final Tier THAUMIUM_TOOL = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 400, 7.0F, 2.0F, 22, () -> Ingredient.of(ModTags.THAUMIUM_TOOL_MATERIALS));
    public static final Tier ELEMENTAL_TOOL = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 10.0F, 3.0F, 18, () -> Ingredient.of(ModTags.THAUMIUM_TOOL_MATERIALS));

    public static final Holder<ArmorMaterial> THAUMIUM_ARMOR = register(
        "thaumium", defense(2, 5, 6, 2), 25, SoundEvents.ARMOR_EQUIP_IRON, ModTags.REPAIRS_THAUMIUM_ARMOR
    );
    public static final Holder<ArmorMaterial> ROBE_ARMOR = special("robes", ModTags.REPAIRS_SPECIAL_ARMOR);
    public static final Holder<ArmorMaterial> GOGGLES_ARMOR = special("goggles", ModTags.REPAIRS_GOGGLES);
    public static final Holder<ArmorMaterial> TRAVELLER_ARMOR = special("boots_traveller", ModTags.REPAIRS_SPECIAL_ARMOR);
    public static final Holder<ArmorMaterial> HARNESS_ARMOR = special("hover_harness", ModTags.REPAIRS_GOGGLES);

    private ModMaterials() {
    }

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }

    private static Holder<ArmorMaterial> special(String name, TagKey<Item> repairs) {
        return register(name, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, repairs);
    }

    private static Holder<ArmorMaterial> register(String name, Map<ArmorItem.Type, Integer> defense, int enchantability, Holder<net.minecraft.sounds.SoundEvent> sound, TagKey<Item> repairs) {
        DeferredHolder<ArmorMaterial, ArmorMaterial> holder = ARMOR_MATERIALS.register(
            name,
            () -> new ArmorMaterial(defense, enchantability, sound, () -> Ingredient.of(repairs), List.of(new ArmorMaterial.Layer(Thaumcraft.id(name))), 0.0F, 0.0F)
        );
        return holder;
    }

    private static Map<ArmorItem.Type, Integer> defense(int boots, int legs, int chest, int helmet) {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, boots);
        defense.put(ArmorItem.Type.LEGGINGS, legs);
        defense.put(ArmorItem.Type.CHESTPLATE, chest);
        defense.put(ArmorItem.Type.HELMET, helmet);
        defense.put(ArmorItem.Type.BODY, chest);
        return defense;
    }
}
