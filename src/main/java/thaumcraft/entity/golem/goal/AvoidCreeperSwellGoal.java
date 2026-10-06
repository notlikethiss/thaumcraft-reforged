package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemBase;

public class AvoidCreeperSwellGoal extends Goal {
    private final GolemBase golem;
    private @Nullable Creeper creeper;
    private @Nullable Path path;
    private @Nullable Vec3 target;

    public AvoidCreeperSwellGoal(GolemBase golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        List<Creeper> creepers = this.golem.level().getEntitiesOfClass(Creeper.class, this.golem.getBoundingBox().inflate(5.0, 3.0, 5.0));
        if (creepers.isEmpty() || creepers.get(0).getSwellDir() != 1) {
            return false;
        }
        this.creeper = creepers.get(0);
        if (!this.golem.getSensing().hasLineOfSight(this.creeper)) {
            return false;
        }
        Vec3 away = DefaultRandomPos.getPosAway(this.golem, 16, 7, this.creeper.position());
        if (away == null || this.creeper.distanceToSqr(away) < this.creeper.distanceToSqr(this.golem)) {
            return false;
        }
        this.path = this.golem.getNavigation().createPath(away.x, away.y, away.z, 0);
        this.target = away;
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.golem.getNavigation().isDone();
    }

    @Override
    public void start() {
        double dx = this.target.x + 0.5 - this.golem.getX();
        double dz = this.target.z + 0.5 - this.golem.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        Vec3 motion = this.golem.getDeltaMovement();
        this.golem.setDeltaMovement(
            motion.x + dx / distance * 0.8F + motion.x * 0.2F,
            0.3,
            motion.z + dz / distance * 0.8F + motion.z * 0.2F
        );
        this.golem.getNavigation().moveTo(this.path, 1.25);
    }

    @Override
    public void stop() {
        this.creeper = null;
    }

    @Override
    public void tick() {
        if (this.creeper != null) {
            this.golem.getNavigation().setSpeedModifier(this.golem.distanceToSqr(this.creeper) < 49.0 ? 1.25 : 1.125);
        }
    }
}
