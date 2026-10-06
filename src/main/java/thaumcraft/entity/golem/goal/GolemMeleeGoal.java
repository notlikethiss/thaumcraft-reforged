package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.IronGuardianGolem;

public class GolemMeleeGoal extends Goal {
    private final IronGuardianGolem golem;
    private @Nullable LivingEntity target;
    private @Nullable Path path;
    private int attackTick;
    private int counter;

    public GolemMeleeGoal(IronGuardianGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        if (target == null || !this.golem.isValidTarget(target)) {
            return false;
        }
        this.target = target;
        this.path = this.golem.getNavigation().createPath(target, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.golem.getTarget() != null && this.target != null && this.target.isAlive() && this.golem.isWithinHome(this.target.blockPosition());
    }

    @Override
    public void start() {
        this.golem.getNavigation().moveTo(this.path, 1.0);
        this.counter = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.golem.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.target;
        if (target == null) {
            return;
        }
        this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.golem.getSensing().hasLineOfSight(target) && --this.counter <= 0) {
            this.counter = 4 + this.golem.getRandom().nextInt(7);
            this.golem.getNavigation().moveTo(target, 1.0);
        }
        this.attackTick = Math.max(this.attackTick - 1, 0);
        double range = Math.max(target.getBbWidth() * 2.0F, target.getBbHeight() * 2.0F) + 0.25;
        if (this.golem.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= range * range && this.attackTick <= 0) {
            this.attackTick = 20;
            if (!this.golem.getMainHandItem().isEmpty()) {
                this.golem.swingForAttack(InteractionHand.MAIN_HAND);
            } else {
                this.golem.startActionTimer();
            }
            if (this.golem.level() instanceof ServerLevel level) {
                this.golem.doHurtTarget(level, target);
            }
        }
    }
}
