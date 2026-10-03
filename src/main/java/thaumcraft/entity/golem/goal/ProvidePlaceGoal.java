package thaumcraft.entity.golem.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.GolemWorker;
import thaumcraft.entity.golem.MultiColorGolem;

public class ProvidePlaceGoal extends GolemChestGoal {
    public ProvidePlaceGoal(GolemWorker golem) {
        super(golem);
    }

    private List<GolemUtils.MarkedContainer> containers() {
        if (!(this.golem instanceof MultiColorGolem multi)) {
            return GolemUtils.adjacentMarkedContainers(this.golem);
        }
        boolean hasSomething = this.golem.getInventory().hasSomething();
        boolean anyColor = multi.hasAnyColor();
        return GolemUtils.adjacentMarkedContainers(this.golem, marker -> {
            int color = GolemUtils.markerColor(this.golem.level().getBlockState(marker));
            for (int slot = 0; slot < 6; slot++) {
                if (!hasSomething || GolemUtils.sameItem(this.golem.getCarried(), this.golem.getInventory().getItem(slot))) {
                    int slotColor = multi.getSlotColor(slot);
                    if (color == slotColor || slotColor == -1 && (!anyColor || hasSomething)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private GolemUtils.@Nullable MarkedContainer target() {
        BlockPos home = this.golem.getHomeContainerPos();
        for (GolemUtils.MarkedContainer container : containers()) {
            if (container.pos().equals(home)) {
                continue;
            }
            ResourceHandler<ItemResource> handler = GolemUtils.handler(this.golem.level(), container.pos(), container.side());
            if (handler != null && GolemUtils.insert(handler, this.golem.getCarried(), true) > 0) {
                return container;
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        return !this.golem.getCarried().isEmpty() && this.golem.getNavigation().isDone() && target() != null;
    }

    @Override
    public void start() {
        this.count = 200;
        GolemUtils.MarkedContainer container = target();
        if (container == null) {
            return;
        }
        ResourceHandler<ItemResource> handler = GolemUtils.handler(this.golem.level(), container.pos(), container.side());
        if (handler == null) {
            return;
        }
        ItemStack carried = this.golem.getCarried().copy();
        carried.shrink(GolemUtils.insert(handler, carried, false));
        this.golem.setCarried(carried);
        openChest(container.pos());
    }
}
