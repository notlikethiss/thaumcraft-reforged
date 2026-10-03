package thaumcraft.entity.golem.goal;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thaumcraft.entity.golem.GolemUtils;
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
        ResourceHandler<ItemResource> handler = this.golem.homeHandler();
        return handler != null && GolemUtils.contains(handler, filter());
    }

    @Override
    public void start() {
        this.count = 200;
        ResourceHandler<ItemResource> handler = this.golem.homeHandler();
        if (handler == null) {
            return;
        }
        ItemStack taken = GolemUtils.extractFirst(handler, filter(), this.golem.getCarrySpace());
        if (!taken.isEmpty()) {
            this.golem.setCarried(taken);
            openChest(this.golem.getHomeContainerPos());
        }
    }
}
