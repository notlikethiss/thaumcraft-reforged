package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import thaumcraft.entity.golem.DecantingGolem;
import thaumcraft.entity.golem.GolemUtils;

public class LiquidEmptyGoal extends Goal {
    private final DecantingGolem golem;

    public LiquidEmptyGoal(DecantingGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockPos home = this.golem.getRestrictCenter();
        if (this.golem.getAmount() < DecantingGolem.SPACE_PER_UNIT
            || !this.golem.getNavigation().isDone()
            || this.golem.distanceToSqr(home.getX() + 0.5F, home.getY() + 0.5F, home.getZ() + 0.5F) > 5.0) {
            return false;
        }
        for (DecantingGolem.MissingLiquid liquid : this.golem.getMissingLiquids()) {
            if (liquid.fluid().isSame(this.golem.getLiquid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        if (this.golem.homeFill(this.golem.getLiquid(), DecantingGolem.UNIT, true) == DecantingGolem.UNIT) {
            this.golem.homeFill(this.golem.getLiquid(), DecantingGolem.UNIT, false);
            this.golem.setAmount(this.golem.getAmount() - DecantingGolem.SPACE_PER_UNIT);
            this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.15F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
        }
    }
}
