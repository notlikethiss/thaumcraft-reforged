package thaumcraft.entity.golem.goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.IronGuardianGolem;

public class GolemNearestTargetGoal extends TargetGoal {
    private final IronGuardianGolem golem;
    private @Nullable LivingEntity candidate;

    public GolemNearestTargetGoal(IronGuardianGolem golem) {
        super(golem, true);
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    protected double getFollowDistance() {
        return this.golem.getRange();
    }

    @Override
    public boolean canUse() {
        double range = getFollowDistance();
        List<LivingEntity> entities = this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(range, 4.0, range));
        entities.sort(Comparator.comparingDouble(this.golem::distanceToSqr));
        TargetingConditions conditions = TargetingConditions.forCombat().range(range);
        for (LivingEntity entity : entities) {
            if (entity != this.golem && this.golem.isValidTarget(entity) && this.canAttack(entity, conditions)) {
                this.candidate = entity;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.candidate);
        super.start();
    }
}
