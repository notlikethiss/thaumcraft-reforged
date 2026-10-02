package thaumcraft.aspect;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface AspectProvidingItem {
    @Nullable AspectList getStackAspects(ItemStack stack, @Nullable AspectList base);
}
