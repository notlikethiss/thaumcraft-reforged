package thaumcraft.entity.golem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GolemInventory extends SimpleContainer {
    private final GolemBase golem;
    private final int stackLimit;

    public GolemInventory(GolemBase golem, int size, int stackLimit) {
        super(size);
        this.golem = golem;
        this.stackLimit = stackLimit;
    }

    @Override
    public int getMaxStackSize() {
        return stackLimit;
    }

    @Override
    public boolean stillValid(Player player) {
        return golem.isAlive() && player.distanceToSqr(golem) <= 64.0;
    }

    public int getAmountNeeded(ItemStack stack) {
        int amount = 0;
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack filter = getItem(slot);
            if (!filter.isEmpty() && ItemStack.isSameItemSameComponents(filter, stack)) {
                amount += filter.getCount();
            }
        }
        return amount;
    }

    public boolean hasSomething() {
        return !isEmpty();
    }

    public List<ItemStack> saveStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < getContainerSize(); slot++) {
            stacks.add(getItem(slot).copy());
        }
        return stacks;
    }

    public void loadStacks(List<ItemStack> stacks) {
        clearContent();
        for (int slot = 0; slot < Math.min(stacks.size(), getContainerSize()); slot++) {
            setItem(slot, stacks.get(slot));
        }
    }
}
