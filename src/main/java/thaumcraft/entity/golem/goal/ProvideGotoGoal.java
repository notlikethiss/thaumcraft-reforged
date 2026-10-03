package thaumcraft.entity.golem.goal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.GolemWorker;
import thaumcraft.entity.golem.MultiColorGolem;

public class ProvideGotoGoal extends GolemMoveGoal {
    private final GolemWorker worker;
    private @Nullable BlockPos destination;

    public ProvideGotoGoal(GolemWorker golem) {
        super(golem);
        this.worker = golem;
    }

    private List<GolemUtils.MarkedContainer> containers() {
        if (!(this.worker instanceof MultiColorGolem multi)) {
            return GolemUtils.containersWithRoom(this.worker);
        }
        List<GolemUtils.MarkedContainer> results = new ArrayList<>();
        boolean hasSomething = this.worker.getInventory().hasSomething();
        for (int slot = 0; slot < 6; slot++) {
            if ((!hasSomething || GolemUtils.sameItem(this.worker.getCarried(), this.worker.getInventory().getItem(slot)))
                && (hasSomething || !multi.hasAnyColor() || multi.getSlotColor(slot) != -1)) {
                this.worker.setColor(multi.getSlotColor(slot));
                results.addAll(GolemUtils.containersWithRoom(this.worker));
            }
        }
        return results;
    }

    @Override
    public boolean canUse() {
        if (this.worker.getCarried().isEmpty()) {
            return false;
        }
        this.destination = GolemGoals.nearest(this.worker, containers());
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
