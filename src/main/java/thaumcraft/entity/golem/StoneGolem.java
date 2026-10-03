package thaumcraft.entity.golem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.goal.HomeTakeGoal;
import thaumcraft.entity.golem.goal.ProvideGotoGoal;
import thaumcraft.entity.golem.goal.ProvidePlaceGoal;
import thaumcraft.registry.ModSounds;

public class StoneGolem extends GolemWorker {
    public StoneGolem(EntityType<? extends StoneGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 3, 1);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.STONE;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ProvidePlaceGoal(this));
        this.goalSelector.addGoal(2, new ProvideGotoGoal(this));
        this.goalSelector.addGoal(3, new HomeTakeGoal(this));
        addBasicGoals(false, 4, 5, 7);
    }

    @Override
    protected int baseHealth() {
        return 15;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.GOLEMSTONE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GOLEMSTONE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GOLEMSTONE.get();
    }
}
