package thaumcraft.entity.golem.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.GolemWorker;
import thaumcraft.entity.golem.MultiColorGolem;

public class SupplyGotoGoal extends GolemMoveGoal {
    private final GolemWorker worker;
    private @Nullable BlockPos destination;

    public SupplyGotoGoal(GolemWorker golem) {
        super(golem);
        this.worker = golem;
    }

    @Override
    public boolean canUse() {
        if (!this.worker.getCarried().isEmpty() || !this.worker.getInventory().hasSomething()) {
            return false;
        }
        List<ItemStack> missing = this.worker.getMissingItems();
        if (missing == null || missing.isEmpty()) {
            return false;
        }
        List<GolemUtils.MarkedContainer> results = List.of();
        for (ItemStack stack : missing) {
            this.worker.itemWatched = stack.copy();
            if (this.worker instanceof MultiColorGolem multi) {
                this.worker.setColor(-1);
                for (int slot = 0; slot < 6; slot++) {
                    if (GolemUtils.sameItem(this.worker.itemWatched, this.worker.getInventory().getItem(slot))) {
                        this.worker.setColor(multi.getSlotColor(slot));
                        break;
                    }
                }
            }
            results = GolemUtils.containersWithGoods(this.worker, this.worker.itemWatched);
            if (!results.isEmpty()) {
                break;
            }
        }
        this.destination = GolemGoals.nearest(this.worker, results);
        return this.destination != null;
    }

    @Override
    public void start() {
        startMoving(this.destination.getX(), this.destination.getY(), this.destination.getZ());
    }

    @Override
    public void stop() {
        this.destination = null;
        this.count = 0;
    }
}
