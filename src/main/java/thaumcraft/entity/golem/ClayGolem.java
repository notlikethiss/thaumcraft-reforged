package thaumcraft.entity.golem;

import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.goal.HomePlaceGoal;
import thaumcraft.entity.golem.goal.SupplyGotoGoal;
import thaumcraft.entity.golem.goal.SupplyTakeGoal;
import thaumcraft.registry.ModSounds;

public class ClayGolem extends GolemWorker {
    private static final EntityDataAccessor<Boolean> DATA_TOGGLE = SynchedEntityData.defineId(ClayGolem.class, EntityDataSerializers.BOOLEAN);

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
        this.goalSelector.addGoal(1, new HomePlaceGoal(this));
        this.goalSelector.addGoal(2, new SupplyTakeGoal(this));
        this.goalSelector.addGoal(3, new SupplyGotoGoal(this));
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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TOGGLE, false);
    }

    @Override
    public boolean isToggled() {
        return this.entityData.get(DATA_TOGGLE);
    }

    public void setToggled(boolean toggle) {
        this.entityData.set(DATA_TOGGLE, toggle);
    }

    @Override
    public @Nullable List<ItemStack> getMissingItems() {
        return ClayLogic.missingItems(this, isToggled());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("toggle", isToggled());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setToggled(input.getBooleanOr("toggle", false));
    }
}
