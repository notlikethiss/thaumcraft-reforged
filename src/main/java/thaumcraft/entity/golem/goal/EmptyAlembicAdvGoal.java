package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.entity.golem.DecantingGolem;
import thaumcraft.entity.golem.GolemUtils;

public class EmptyAlembicAdvGoal extends Goal {
    private final DecantingGolem golem;
    private int delayUntil;

    public EmptyAlembicAdvGoal(DecantingGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private boolean canTake(AlembicBlockEntity alembic) {
        Aspect aspect = this.golem.getAspect();
        int amount = this.golem.getAmount();
        return alembic.getAmount() > 0 && (amount == 0 || (aspect == null || aspect == alembic.getAspect()) && amount < DecantingGolem.CAPACITY);
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getNavigation().isDone() || this.golem.tickCount < this.delayUntil) {
            return false;
        }
        BlockPos home = this.golem.getHomeContainerPos();
        if (this.golem.distanceToSqr(home.getX() + 0.5F, home.getY() + 0.5F, home.getZ() + 0.5F) > 6.0) {
            return false;
        }
        for (AlembicBlockEntity alembic : GolemUtils.alembicsAroundCrucible(this.golem.level(), home)) {
            if (canTake(alembic)) {
                this.delayUntil = this.golem.tickCount + 20;
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
        for (AlembicBlockEntity alembic : GolemUtils.alembicsAroundCrucible(this.golem.level(), this.golem.getHomeContainerPos())) {
            if (!canTake(alembic)) {
                continue;
            }
            int taken = Math.min(4, Math.min(alembic.getAmount(), DecantingGolem.CAPACITY - this.golem.getAmount()));
            Aspect previousAspect = this.golem.getAspect();
            int previousAmount = this.golem.getAmount();
            this.golem.setAspect(alembic.getAspect());
            this.golem.setAmount(previousAmount + taken);
            if (this.golem.findJarWithRoom() != null) {
                alembic.takeFromSource(alembic.getAspect(), taken);
                this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.15F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
                this.delayUntil = this.golem.tickCount + 2;
                return;
            }
            this.golem.setAmount(previousAmount);
            if (previousAmount == 0) {
                this.golem.setAspect(previousAspect);
            }
        }
    }
}
