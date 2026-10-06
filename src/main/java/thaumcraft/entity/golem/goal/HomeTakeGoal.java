package thaumcraft.entity.golem.goal;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entity.golem.GolemWorker;

public class HomeTakeGoal extends GolemChestGoal {
    public HomeTakeGoal(GolemWorker golem) {
        super(golem);
    }

    private Predicate<ItemStack> filter() {
        return stack -> !this.golem.hasSomething() || this.golem.getInventory().getAmountNeeded(stack) > 0;
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getCarried().isEmpty() || !this.golem.getNavigation().isDone() || homeDistance() > 6.0) {
            return false;
        }
        return this.golem.homeContains(filter());
    }

    @Override
    public void start() {
        this.count = 200;
        ItemStack taken = this.golem.homeExtractFirst(filter(), this.golem.getCarrySpace());
        if (!taken.isEmpty()) {
            this.golem.setCarried(taken);
            openChest(this.golem.getHomeContainerPos());
        }
    }
}
