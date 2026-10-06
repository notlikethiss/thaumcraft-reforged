package thaumcraft.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.network.ModNetwork;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aura.AuraManager;
import thaumcraft.fx.Fx;
import thaumcraft.item.EssenceItem;
import thaumcraft.network.WispZapPayload;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

public class Wisp extends Mob implements Enemy, AuraManager.AspectTyped {
    private static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(Wisp.class, EntityDataSerializers.BYTE);
    private static final TargetingConditions AGGRO_TARGETING = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight();
    private static final double ATTACK_RANGE = 16.0;

    private int courseChangeCooldown;
    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private @Nullable LivingEntity targetedEntity;
    private int aggroCooldown;
    private int attackCounter;

    public Wisp(EntityType<? extends Wisp> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TYPE, (byte) Aspect.FLUX.getId());
    }

    public Aspect getAspect() {
        return Aspect.get(this.entityData.get(DATA_TYPE));
    }

    @Override
    public void setAspect(Aspect aspect) {
        this.entityData.set(DATA_TYPE, (byte) aspect.getId());
    }

    @Override
    public boolean fireImmune() {
        return this.getAspect() == Aspect.FIRE || super.fireImmune();
    }

    @Override
    public void travel(Vec3 input) {
        this.moveRelative(0.02F, input);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
        } else if (this.isInLava()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected int decreaseAirSupply(int currentSupply) {
        return currentSupply;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
        return level.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof LivingEntity living) {
            this.targetedEntity = living;
            this.aggroCooldown = 200;
        }
        if (source.getEntity() instanceof LivingEntity living) {
            this.targetedEntity = living;
            this.aggroCooldown = 200;
        }
        return super.hurt(source, damage);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (!level.isClientSide()) {
            return;
        }
        if (this.tickCount <= 1 || this.deathTime == 1) {
            Fx.get().burst(level, this.getX(), this.getY(), this.getZ(), 1.2F);
        }
        if (level.getRandom().nextBoolean()) {
            int color = this.getAspect().color;
            Fx.get().wispFX(
                level,
                this.getX() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F,
                this.getY() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F,
                this.getZ() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F,
                0.1F,
                (color >> 16 & 0xFF) / 255.0F,
                (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
            );
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();
        double dx = this.waypointX - this.getX();
        double dy = this.waypointY - this.getY();
        double dz = this.waypointZ - this.getZ();
        double distance = dx * dx + dy * dy + dz * dz;
        if (distance < 1.0 || distance > 3600.0) {
            this.waypointX = this.getX() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0;
            this.waypointY = this.getY() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0;
            this.waypointZ = this.getZ() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0;
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.random.nextInt(5) + 2;
            distance = Math.sqrt(distance);
            if (this.isCourseTraversable(level, distance)) {
                this.setDeltaMovement(this.getDeltaMovement().add(dx / distance * 0.1, dy / distance * 0.1, dz / distance * 0.1));
            } else {
                this.waypointX = this.getX();
                this.waypointY = this.getY();
                this.waypointZ = this.getZ();
            }
        }
        if (this.targetedEntity != null && !this.targetedEntity.isAlive()) {
            this.targetedEntity = null;
        }
        this.aggroCooldown--;
        if (this.getAspect().aggro && (this.targetedEntity == null || this.aggroCooldown-- <= 0)) {
            this.targetedEntity = level.getNearestPlayer(AGGRO_TARGETING, this);
            if (this.targetedEntity != null) {
                this.aggroCooldown = 50;
            }
        }
        LivingEntity target = this.targetedEntity;
        if (target != null && target.distanceToSqr(this) < ATTACK_RANGE * ATTACK_RANGE) {
            double tx = target.getX() - this.getX();
            double tz = target.getZ() - this.getZ();
            float yaw = -((float) Mth.atan2(tx, tz)) * 180.0F / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (this.hasLineOfSight(target)) {
                this.attackCounter++;
                if (this.attackCounter == 20) {
                    zap(level, target);
                    this.attackCounter = -20 + this.random.nextInt(20);
                }
            } else if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        } else {
            Vec3 motion = this.getDeltaMovement();
            float yaw = -((float) Mth.atan2(motion.x, motion.z)) * 180.0F / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        }
    }

    private void zap(ServerLevel level, LivingEntity target) {
        this.playSound(ModSounds.ZAP.get(), 1.0F, 1.1F);
        ModNetwork.sendToNear(level, null, this.getX(), this.getY(), this.getZ(), 64.0, new WispZapPayload(this.getId(), target.getId()));
        Vec3 motion = target.getDeltaMovement();
        if (Math.abs(motion.x) <= 0.1F && Math.abs(motion.y) <= 0.1F && Math.abs(motion.z) <= 0.1F) {
            if (this.random.nextFloat() < 0.66F) {
                target.hurt(this.damageSources().mobAttack(this), 4.0F);
            }
        } else if (this.random.nextFloat() < 0.4F) {
            target.hurt(this.damageSources().mobAttack(this), 3.0F);
        }
    }

    private boolean isCourseTraversable(ServerLevel level, double distance) {
        double stepX = (this.waypointX - this.getX()) / distance;
        double stepY = (this.waypointY - this.getY()) / distance;
        double stepZ = (this.waypointZ - this.getZ()) / distance;
        AABB box = this.getBoundingBox();
        for (int i = 1; i < distance; i++) {
            box = box.move(stepX, stepY, stepZ);
            if (!level.noCollision(this, box)) {
                return false;
            }
        }
        BlockPos pos = BlockPos.containing(this.waypointX, this.waypointY, this.waypointZ);
        if (level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos).is(FluidTags.LAVA)) {
            return false;
        }
        for (int depth = 0; depth < 11; depth++) {
            if (!level.isEmptyBlock(pos.below(depth))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.WISPLIVE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FIRE_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WISPDEAD.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.25F;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        Aspect aspect = this.getAspect();
        Aspect[] aspects = Aspect.values();
        while (aspect == Aspect.UNKNOWN) {
            aspect = aspects[this.random.nextInt(aspects.length)];
        }
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
                killer
            );
        }
        ItemStack essence = new ItemStack(ModItems.WISP_ESSENCE.get(), 1 + this.random.nextInt(looting + 1) / 2);
        EssenceItem.setAspect(essence, aspect);
        this.spawnAtLocation(essence);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putByte("Type", (byte) this.getAspect().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.entityData.set(DATA_TYPE, input.getByteOr("Type", (byte) Aspect.FLUX.getId()));
    }
}
