package thaumcraft.entity.golem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.goal.GotoGrownCropsGoal;
import thaumcraft.entity.golem.goal.HarvestCropGoal;

public class StrawGolem extends GolemWorker {
    public StrawGolem(EntityType<? extends StrawGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 1, 1);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.STRAW;
    }

    @Override
    protected void registerGoals() {
        addBasicGoals(true, 5, 6, 7);
        this.goalSelector.addGoal(2, new HarvestCropGoal(this));
        this.goalSelector.addGoal(3, new GotoGrownCropsGoal(this));
    }

    @Override
    protected int baseHealth() {
        return 8;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.GRASS_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GRASS_STEP;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GRASS_STEP;
    }
}
