package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.GolemWorker;

public abstract class GolemChestGoal extends Goal {
    protected final GolemWorker golem;
    protected int count;
    protected int countChest;
    private @Nullable BlockPos chest;

    protected GolemChestGoal(GolemWorker golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    protected void openChest(BlockPos pos) {
        GolemUtils.chestInteract(this.golem.level(), pos, true);
        this.countChest = 5;
        this.chest = pos;
    }

    @Override
    public boolean canContinueToUse() {
        return this.count > 0 && (canUse() || this.countChest > 0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.countChest--;
        this.count--;
    }

    @Override
    public void stop() {
        if (this.chest != null) {
            GolemUtils.chestInteract(this.golem.level(), this.chest, false);
            this.chest = null;
        }
    }

    protected double homeDistance() {
        BlockPos home = this.golem.getHomePosition();
        return this.golem.distanceToSqr(home.getX() + 0.5F, home.getY() + 0.5F, home.getZ() + 0.5F);
    }
}
