package thaumcraft.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WorkbenchResultSlot extends Slot {
    private final MagicWorkbenchMenu menu;

    public WorkbenchResultSlot(MagicWorkbenchMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = getItem().copy();
        container.setItem(getContainerSlot(), ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        menu.handleTake(player, stack);
        super.onTake(player, stack);
    }
}
