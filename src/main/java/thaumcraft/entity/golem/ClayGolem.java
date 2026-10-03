package thaumcraft.entity.golem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.registry.ModSounds;

public class ClayGolem extends GolemWorker {
    public ClayGolem(EntityType<? extends ClayGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 3, 64);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.CLAY;
    }

    @Override
    protected void registerGoals() {
        addBasicGoals(true, 4, 5, 7);
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
