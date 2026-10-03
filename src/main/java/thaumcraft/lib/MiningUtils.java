package thaumcraft.lib;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import thaumcraft.registry.ModItems;

public final class MiningUtils {
    private static final List<Cluster> CLUSTERS = List.of(
        new Cluster("iron", ModItems.NATIVE_IRON_CLUSTER.get()),
        new Cluster("gold", ModItems.NATIVE_GOLD_CLUSTER.get()),
        new Cluster("copper", ModItems.NATIVE_COPPER_CLUSTER.get()),
        new Cluster("tin", ModItems.NATIVE_TIN_CLUSTER.get()),
        new Cluster("silver", ModItems.NATIVE_SILVER_CLUSTER.get()),
        new Cluster("lead", ModItems.NATIVE_LEAD_CLUSTER.get())
    );

    private MiningUtils() {
    }

    public static int enchantmentLevel(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return level.registryAccess()
            .lookup(Registries.ENCHANTMENT)
            .flatMap(registry -> registry.get(key))
            .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
            .orElse(0);
    }

    public static ItemStack findSpecialMiningResult(ItemStack stack, float chance, RandomSource random) {
        if (random.nextFloat() > chance) {
            return stack.copy();
        }
        for (Cluster cluster : CLUSTERS) {
            if (stack.is(cluster.ore()) || stack.is(cluster.raw())) {
                return new ItemStack(cluster.item(), stack.getCount());
            }
        }
        return stack.copy();
    }

    private record Cluster(TagKey<Item> ore, TagKey<Item> raw, Item item) {
        Cluster(String metal, Item item) {
            this(tag("ores/" + metal), tag("raw_materials/" + metal), item);
        }

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }
    }
}
