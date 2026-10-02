package thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface WorkbenchGrid {
    ItemStack get(int x, int y);
}
