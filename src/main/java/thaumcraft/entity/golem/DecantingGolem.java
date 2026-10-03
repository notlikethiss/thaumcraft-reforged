package thaumcraft.entity.golem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class DecantingGolem extends GolemWorker {
    public DecantingGolem(EntityType<? extends DecantingGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 1, 16);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.DECANTING;
    }

    @Override
    protected void registerGoals() {
        addBasicGoals(true, 4, 5, 7);
    }

    @Override
    protected int baseHealth() {
        return 10;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_HURT;
    }
}
