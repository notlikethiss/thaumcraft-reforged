package thaumcraft.entity.monster;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;

public class GiantBrainyZombie extends BrainyZombie {
    private static final EntityDataAccessor<Float> DATA_ANGER = SynchedEntityData.defineId(GiantBrainyZombie.class, EntityDataSerializers.FLOAT);

    public GiantBrainyZombie(EntityType<? extends GiantBrainyZombie> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BrainyZombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_ANGER, 1.0F);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
    }

    public float getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    private void setAnger(float anger) {
        this.entityData.set(DATA_ANGER, anger);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((int) (8.0F + (anger - 1.0F) * 5.0F));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_ANGER.equals(accessor)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(1.2F + this.getAnger());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.getAnger() > 1.0F) {
            this.setAnger(Math.max(1.0F, this.getAnger() - 0.002F));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        this.setAnger(Math.min(2.0F, this.getAnger() + 0.1F));
        return super.hurt(source, damage);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("Anger", this.getAnger());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setAnger(input.getFloatOr("Anger", 1.0F));
    }
}
