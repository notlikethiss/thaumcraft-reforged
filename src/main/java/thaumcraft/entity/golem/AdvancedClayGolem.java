package thaumcraft.entity.golem;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
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

public class AdvancedClayGolem extends GolemWorker implements MultiColorGolem {
    private static final EntityDataAccessor<Boolean> DATA_TOGGLE = SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BOOLEAN);
    private static final List<EntityDataAccessor<Byte>> DATA_COLORS = List.of(
        SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BYTE),
        SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BYTE),
        SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BYTE),
        SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BYTE),
        SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BYTE),
        SynchedEntityData.defineId(AdvancedClayGolem.class, EntityDataSerializers.BYTE)
    );

    public AdvancedClayGolem(EntityType<? extends AdvancedClayGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 6, 64);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.ADVANCED_CLAY;
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
        for (EntityDataAccessor<Byte> color : DATA_COLORS) {
            entityData.define(color, (byte) -1);
        }
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
    public int getSlotColor(int slot) {
        return this.entityData.get(DATA_COLORS.get(slot));
    }

    @Override
    public void setSlotColor(int slot, int color) {
        this.entityData.set(DATA_COLORS.get(slot), (byte) color);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putBoolean("toggle", isToggled());
        int[] colors = new int[6];
        for (int slot = 0; slot < 6; slot++) {
            colors[slot] = getSlotColor(slot);
        }
        output.putIntArray("colors", colors);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        setToggled(input.getBooleanOr("toggle", false));
        int[] colors = input.getIntArray("colors").orElse(new int[0]);
        for (int slot = 0; slot < 6; slot++) {
            setSlotColor(slot, slot < colors.length ? colors[slot] : -1);
        }
    }
}
