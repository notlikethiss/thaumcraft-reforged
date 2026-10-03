package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entity.golem.GolemBase;

public abstract class GolemMoveGoal extends Goal {
    protected final GolemBase golem;
    protected int count;
    private BlockPos previous = BlockPos.ZERO;

    protected GolemMoveGoal(GolemBase golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    protected void startMoving(double x, double y, double z) {
        this.count = 200;
        this.previous = this.golem.blockPosition();
        this.golem.getNavigation().moveTo(x, y, z, 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        return this.count > 0 && !this.golem.getNavigation().isDone();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.count--;
        if (this.count == 0 && this.previous.equals(this.golem.blockPosition())) {
            Vec3 target = DefaultRandomPos.getPos(this.golem, 2, 1);
            if (target != null) {
                this.count = 20;
                this.golem.getNavigation().moveTo(target.x, target.y, target.z, 1.0);
            }
        }
    }
}
