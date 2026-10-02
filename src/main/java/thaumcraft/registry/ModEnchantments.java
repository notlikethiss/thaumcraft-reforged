package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumcraft.Thaumcraft;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> POTENCY = key("potency");
    public static final ResourceKey<Enchantment> FRUGAL = key("frugal");
    public static final ResourceKey<Enchantment> CHARGING = key("charging");
    public static final ResourceKey<Enchantment> TREASURE = key("treasure");
    public static final ResourceKey<Enchantment> HASTE = key("haste");
    public static final ResourceKey<Enchantment> REPAIR = key("repair");

    private ModEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Thaumcraft.id(name));
    }
}
