package thaumcraft.entity.golem.goal;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thaumcraft.entity.golem.GolemUtils;
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
        ResourceHandler<ItemResource> handler = this.golem.homeHandler();
        return handler != null && GolemUtils.insert(handler, this.golem.getCarried(), true) > 0;
    }

    @Override
    public void start() {
        this.count = 200;
        ResourceHandler<ItemResource> handler = this.golem.homeHandler();
        if (handler == null) {
            return;
        }
        ItemStack carried = this.golem.getCarried().copy();
        int inserted = GolemUtils.insert(handler, carried, false);
        if (inserted > 0) {
            carried.shrink(inserted);
            this.golem.setCarried(carried);
            openChest(this.golem.getHomeContainerPos());
        }
    }
}
