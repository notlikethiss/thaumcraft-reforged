package thaumcraft.entity.golem.goal;

import net.minecraft.world.item.ItemStack;
import thaumcraft.entity.golem.GolemWorker;

public class HomePlaceGoal extends GolemChestGoal {
    public HomePlaceGoal(GolemWorker golem) {
        super(golem);
    }

    @Override
    public boolean canUse() {
        if (this.golem.getCarried().isEmpty() || !this.golem.getNavigation().isDone() || homeDistance() > 5.0) {
            return false;
        }
        return this.golem.hasHomeInventory() && this.golem.homeInsert(this.golem.getCarried(), true) > 0;
    }

    @Override
    public void start() {
        this.count = 200;
        if (!this.golem.hasHomeInventory()) {
            return;
        }
        ItemStack carried = this.golem.getCarried().copy();
        int inserted = this.golem.homeInsert(carried, false);
        if (inserted > 0) {
            carried.shrink(inserted);
            this.golem.setCarried(carried);
            openChest(this.golem.getHomeContainerPos());
        }
    }
}
