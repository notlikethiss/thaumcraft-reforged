package thaumcraft.entity.golem.goal;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.DecantingGolem;

public class LiquidGotoGoal extends GolemMoveGoal {
    private final DecantingGolem decanting;
    private @Nullable BlockPos target;
    private int delay;

    public LiquidGotoGoal(DecantingGolem golem) {
        super(golem);
        this.decanting = golem;
    }

    @Override
    public boolean canUse() {
        if (this.delay > 0) {
            this.delay--;
        }
        if (this.delay > 0 || this.decanting.getAmount() > 0) {
            return false;
        }
        this.delay = 20;
        for (DecantingGolem.MissingLiquid missing : this.decanting.getMissingLiquids()) {
            this.target = this.decanting.findPossibleLiquid(missing.fluid());
            if (this.target != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        startMoving(this.target.getX(), this.target.getY(), this.target.getZ());
    }

    @Override
    public void stop() {
        this.count = 0;
    }
}
