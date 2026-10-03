package thaumcraft.entity.golem.goal;

import net.minecraft.core.BlockPos;
import thaumcraft.entity.golem.GolemBase;

public class ReturnHomeGoal extends GolemMoveGoal {
    public ReturnHomeGoal(GolemBase golem) {
        super(golem);
    }

    private double homeDistance() {
        BlockPos home = this.golem.getHomePosition();
        return this.golem.distanceToSqr(home.getX() + 0.5F, home.getY() + 0.5F, home.getZ() + 0.5F);
    }

    @Override
    public boolean canUse() {
        return homeDistance() >= 1.5;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && homeDistance() > 1.5;
    }

    @Override
    public void start() {
        BlockPos home = this.golem.getHomePosition();
        startMoving(home.getX(), home.getY(), home.getZ());
    }
}
