package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.entity.golem.TallowGolem;
import thaumcraft.item.EssenceItem;

public class JarPlaceGoal extends Goal {
    private final TallowGolem golem;
    private @Nullable JarBlockEntity jar;
    private int delayUntil;

    public JarPlaceGoal(TallowGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public static boolean accepts(TallowGolem golem, JarBlockEntity jar, boolean empty) {
        ItemStack essences = golem.getProvideStack();
        Aspect aspect = EssenceItem.getAspect(essences);
        if (aspect == null) {
            return false;
        }
        return empty || jar.getAmount() <= JarBlockEntity.MAX_AMOUNT - 8 && jar.getAspect() == aspect;
    }

    @Override
    public boolean canUse() {
        if (this.golem.getProvideStack().isEmpty() || !this.golem.getNavigation().isDone() || this.golem.tickCount < this.delayUntil) {
            return false;
        }
        this.jar = JarGoals.findJar(this.golem, 8.0, 5.0, (jar, empty) -> accepts(this.golem, jar, empty));
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
        ItemStack essences = this.golem.getProvideStack().copy();
        Aspect aspect = EssenceItem.getAspect(essences);
        if (this.jar == null || aspect == null || this.jar.getAmount() != 0 && this.jar.getAspect() != aspect) {
            return;
        }
        while (this.jar.getAmount() <= JarBlockEntity.MAX_AMOUNT - 8 && !essences.isEmpty()) {
            essences.shrink(1);
            this.jar.addToSource(aspect, 8);
        }
        this.golem.setProvideStack(essences);
        this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.15F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
    }
}
