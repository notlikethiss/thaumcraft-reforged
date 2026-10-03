package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.IronGuardianGolem;

public class DartAttackGoal extends Goal {
    private final IronGuardianGolem golem;
    private @Nullable LivingEntity target;
    private int rangedAttackTime;

    public DartAttackGoal(IronGuardianGolem golem) {
        this.golem = golem;
        this.rangedAttackTime = maxAttackTime() / 2;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private int maxAttackTime() {
        return this.golem.getCore() == 1 ? 20 : 30;
    }

    private float rangeSq() {
        float range = this.golem.getCore() == 3 ? 16.0F : 12.0F;
        return range * range;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        if (target == null || !this.golem.isValidTarget(target)) {
            return false;
        }
        if (this.golem.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) < 10.0) {
            return false;
        }
        this.target = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !this.golem.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.rangedAttackTime = maxAttackTime() / 2;
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
        double distance = this.golem.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        boolean visible = this.golem.getSensing().hasLineOfSight(target);
        this.golem.getNavigation().moveTo(target, 1.0);
        if (visible) {
            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.rangedAttackTime = Math.max(this.rangedAttackTime - 1, 0);
            if (this.rangedAttackTime <= 0 && distance <= rangeSq()) {
                this.golem.shootDart(target);
                this.rangedAttackTime = maxAttackTime();
            }
        }
    }
}
