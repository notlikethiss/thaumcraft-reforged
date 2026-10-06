package thaumcraft.entity.golem.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemInventories;
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
        boolean hasSomething = this.golem.hasSomething();
        boolean anyColor = multi.hasAnyColor();
        return GolemUtils.adjacentMarkedContainers(this.golem, marker -> {
            int color = GolemUtils.markerColor(this.golem.level().getBlockState(marker));
            for (int slot = 0; slot < 6; slot++) {
                if (!hasSomething || GolemUtils.sameItem(this.golem.getProvideStack(), this.golem.getInventory().getItem(slot))) {
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
            if (GolemInventories.insert(this.golem.level(), container.pos(), container.side(), this.golem.getProvideStack(), true) > 0) {
                return container;
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        return !this.golem.getProvideStack().isEmpty() && this.golem.getNavigation().isDone() && target() != null;
    }

    @Override
    public void start() {
        this.count = 200;
        GolemUtils.MarkedContainer container = target();
        if (container == null) {
            return;
        }
        if (!GolemInventories.hasItems(this.golem.level(), container.pos(), container.side())) {
            return;
        }
        ItemStack carried = this.golem.getProvideStack().copy();
        carried.shrink(GolemInventories.insert(this.golem.level(), container.pos(), container.side(), carried, false));
        this.golem.setProvideStack(carried);
        openChest(container.pos());
    }
}
