package thaumcraft.entity.golem.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.GolemWorker;
import thaumcraft.entity.golem.MultiColorGolem;

public class SupplyTakeGoal extends GolemChestGoal {
    public SupplyTakeGoal(GolemWorker golem) {
        super(golem);
    }

    private List<GolemUtils.MarkedContainer> containers() {
        if (!(this.golem instanceof MultiColorGolem multi)) {
            return GolemUtils.adjacentMarkedContainers(this.golem);
        }
        boolean hasSomething = this.golem.hasSomething();
        return GolemUtils.adjacentMarkedContainers(this.golem, marker -> {
            int color = GolemUtils.markerColor(this.golem.level().getBlockState(marker));
            for (int slot = 0; slot < 6; slot++) {
                if (!hasSomething || GolemUtils.sameItem(this.golem.itemWatched, this.golem.getInventory().getItem(slot))) {
                    int slotColor = multi.getSlotColor(slot);
                    if (color == slotColor || slotColor == -1) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getCarried().isEmpty()
            || this.golem.itemWatched.isEmpty()
            || !this.golem.getNavigation().isDone()
            || !this.golem.hasSomething()) {
            return false;
        }
        BlockPos home = this.golem.getHomeContainerPos();
        ItemStack watched = this.golem.itemWatched;
        int wanted = Math.min(this.golem.isToggled() ? 64 : watched.getCount(), this.golem.getCarrySpace());
        for (GolemUtils.MarkedContainer container : containers()) {
            if (container.pos().equals(home)) {
                continue;
            }
            ResourceHandler<ItemResource> handler = GolemUtils.handler(this.golem.level(), container.pos(), container.side());
            if (handler == null) {
                continue;
            }
            ItemStack taken = GolemUtils.extractFirst(handler, stack -> GolemUtils.sameItem(stack, watched), wanted);
            if (!taken.isEmpty()) {
                this.golem.setCarried(taken);
                openChest(container.pos());
                this.count = 200;
                this.golem.itemWatched = ItemStack.EMPTY;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.count > 0 && (!this.golem.getNavigation().isDone() || this.countChest > 0);
    }
}
