package thaumcraft.entity.golem.goal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entity.SpecialItem;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.GolemWorker;

final class GolemItemEntities {
    private GolemItemEntities() {
    }

    static ItemStack stackOf(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return item.getItem();
        }
        if (entity instanceof SpecialItem item) {
            return item.getItem();
        }
        return ItemStack.EMPTY;
    }

    static int pickupDelay(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return item.pickupDelay;
        }
        if (entity instanceof SpecialItem item) {
            return item.pickupDelay;
        }
        return Integer.MAX_VALUE;
    }

    static boolean wanted(GolemWorker golem, ItemStack stack) {
        if (golem.getCore() == 2 && golem.getInventory().hasSomething() && golem.getInventory().getAmountNeeded(stack) <= 0) {
            return false;
        }
        return true;
    }

    static void setStack(Entity entity, ItemStack stack) {
        if (entity instanceof ItemEntity item) {
            item.setItem(stack);
        } else if (entity instanceof SpecialItem item) {
            item.setItem(stack);
        }
        if (stack.isEmpty()) {
            entity.discard();
        }
    }

    static boolean canCarry(GolemWorker golem, ItemStack stack, boolean whole) {
        ItemStack carried = golem.getCarried();
        if (carried.isEmpty()) {
            return true;
        }
        if (!GolemUtils.sameItem(carried, stack)) {
            return false;
        }
        return whole ? stack.getCount() <= golem.getCarrySpace() : golem.getCarrySpace() > 0;
    }
}
