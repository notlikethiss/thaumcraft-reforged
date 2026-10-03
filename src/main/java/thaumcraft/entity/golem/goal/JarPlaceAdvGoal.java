package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.entity.golem.DecantingGolem;

public class JarPlaceAdvGoal extends Goal {
    private final DecantingGolem golem;
    private @Nullable JarBlockEntity jar;
    private int delayUntil;

    public JarPlaceAdvGoal(DecantingGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Aspect aspect = this.golem.getAspect();
        if (this.golem.getAmount() <= 0 || aspect == null || !this.golem.getNavigation().isDone() || this.golem.tickCount < this.delayUntil) {
            return false;
        }
        this.jar = JarGoals.findJar(this.golem, 8.0, 5.0, (jar, empty) -> empty || jar.getAmount() < JarBlockEntity.MAX_AMOUNT && jar.getAspect() == aspect);
        return this.jar != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void stop() {
        this.jar = null;
    }

    @Override
    public void start() {
        this.delayUntil = this.golem.tickCount + 20;
        Aspect aspect = this.golem.getAspect();
        if (this.jar == null || aspect == null || this.jar.getAmount() != 0 && this.jar.getAspect() != aspect) {
            return;
        }
        int moved = Math.min(4, Math.min(this.golem.getAmount(), JarBlockEntity.MAX_AMOUNT - this.jar.getAmount()));
        this.golem.setAmount(this.golem.getAmount() - moved);
        this.jar.addToSource(aspect, moved);
        this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.15F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
        this.delayUntil = this.golem.tickCount + 2;
    }
}
