package thaumcraft.entity.golem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.goal.HomePlaceGoal;
import thaumcraft.entity.golem.goal.ItemEntityGotoGoal;
import thaumcraft.entity.golem.goal.ItemEntityPickupGoal;
import thaumcraft.registry.ModSounds;

public class WoodGolem extends GolemWorker {
    public WoodGolem(EntityType<? extends WoodGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 2, 1);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.WOOD;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new HomePlaceGoal(this));
        this.goalSelector.addGoal(2, new ItemEntityPickupGoal(this));
        this.goalSelector.addGoal(3, new ItemEntityGotoGoal(this));
        addBasicGoals(true, 4, 5, 7);
    }

    @Override
    protected int baseHealth() {
        return 10;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.GOLEMWOOD.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GOLEMWOOD.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GOLEMWOOD.get();
    }
}
