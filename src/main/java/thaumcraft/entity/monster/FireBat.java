package thaumcraft.entity.monster;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.Config;

public class FireBat extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(FireBat.class, EntityDataSerializers.BYTE);
    private static final TargetingConditions RESTING_TARGETING = TargetingConditions.forNonCombat().range(4.0);
    private static final TargetingConditions ATTACK_TARGETING = TargetingConditions.forCombat().range(12.0).ignoreLineOfSight();

    private @Nullable BlockPos flightTarget;
    private int damBonus;
    private int attackTime;
    private @Nullable UUID summoner;

    public FireBat(EntityType<? extends FireBat> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) {
            this.setHanging(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_FLAGS, (byte) 0);
    }

    public boolean isHanging() {
        return (this.entityData.get(DATA_FLAGS) & 1) != 0;
    }

    public void setHanging(boolean hanging) {
        setFlag(1, hanging);
    }

    public boolean isSummoned() {
        return (this.entityData.get(DATA_FLAGS) & 2) != 0;
    }

    public void setSummoned(boolean summoned) {
        setFlag(2, summoned);
        updateAttackDamage();
    }

    public void setDamBonus(int damBonus) {
        this.damBonus = damBonus;
        updateAttackDamage();
    }

    public void setSummoner(@Nullable UUID summoner) {
        this.summoner = summoner;
    }

    private void setFlag(int mask, boolean value) {
        byte flags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, (byte) (value ? flags | mask : flags & ~mask));
    }

    private void updateAttackDamage() {
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.isSummoned() ? 2 + this.damBonus : 1);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95F;
    }

    @Override
    public @Nullable SoundEvent getAmbientSound() {
        return this.isHanging() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return !this.isSummoned() && super.shouldDropLoot(level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isHanging()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        if (this.level().isClientSide()) {
            RandomSource random = this.level().getRandom();
            double y = this.yo + this.getBbHeight() / 2.0F;
            this.level().addParticle(ParticleTypes.SMOKE, this.xo + jitter(random), y + jitter(random), this.zo + jitter(random), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.FLAME, this.xo + jitter(random), y + jitter(random), this.zo + jitter(random), 0.0, 0.0, 0.0);
        }
    }

    private static double jitter(RandomSource random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2F;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        BlockPos pos = this.blockPosition();
        BlockPos above = pos.above();
        if (this.isHanging()) {
            if (!level.getBlockState(above).isRedstoneConductor(level, above)) {
                this.setHanging(false);
                level.levelEvent(null, 1025, pos, 0);
            } else {
                if (this.random.nextInt(200) == 0) {
                    this.yHeadRot = this.random.nextInt(360);
                }
                if (level.getNearestPlayer(RESTING_TARGETING, this) != null) {
                    this.setHanging(false);
                    level.levelEvent(null, 1025, pos, 0);
                }
            }
            return;
        }
        updateTarget(level);
        LivingEntity target = this.getTarget();
        if (target == null) {
            if (this.isSummoned()) {
                this.hurt(this.damageSources().generic(), 2.0F);
            }
            if (this.flightTarget != null && (!level.isEmptyBlock(this.flightTarget) || this.flightTarget.getY() < level.getMinBuildHeight() + 1)) {
                this.flightTarget = null;
            }
            if (this.flightTarget == null || this.random.nextInt(30) == 0 || this.flightTarget.distSqr(pos) < 4.0) {
                this.flightTarget = BlockPos.containing(
                    this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                    this.getY() + this.random.nextInt(6) - 2.0,
                    this.getZ() + this.random.nextInt(7) - this.random.nextInt(7)
                );
            }
            steer(this.flightTarget.getX() + 0.5, this.flightTarget.getY() + 0.1, this.flightTarget.getZ() + 0.5);
            if (this.random.nextInt(100) == 0 && level.getBlockState(above).isRedstoneConductor(level, above)) {
                this.setHanging(true);
            }
        } else {
            steer(target.getX(), target.getEyeY(), target.getZ());
            if (this.hasLineOfSight(target)) {
                attack(level, target, this.distanceTo(target));
            }
        }
        if (this.getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            this.setTarget(null);
        }
    }

    private void updateTarget(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            this.setTarget(null);
        }
        if (this.getTarget() == null && !this.isSummoned()) {
            this.setTarget(level.getNearestPlayer(ATTACK_TARGETING, this));
        }
    }

    private void steer(double x, double y, double z) {
        Vec3 movement = this.getDeltaMovement();
        Vec3 newMovement = movement.add(
            (Math.signum(x - this.getX()) * 0.5 - movement.x) * 0.1F,
            (Math.signum(y - this.getY()) * 0.7F - movement.y) * 0.1F,
            (Math.signum(z - this.getZ()) * 0.5 - movement.z) * 0.1F
        );
        this.setDeltaMovement(newMovement);
        float yRot = (float) (Mth.atan2(newMovement.z, newMovement.x) * 180.0F / (float) Math.PI) - 90.0F;
        this.zza = 0.5F;
        this.setYRot(this.getYRot() + Mth.wrapDegrees(yRot - this.getYRot()));
    }

    private void attack(ServerLevel level, LivingEntity target, float distance) {
        if (this.attackTime > 0
            || distance >= Math.max(2.5F, target.getBbWidth() * 1.1F)
            || target.getBoundingBox().maxY <= this.getBoundingBox().minY
            || target.getBoundingBox().minY >= this.getBoundingBox().maxY) {
            return;
        }
        if (this.isSummoned() && this.summoner != null) {
            target.setLastHurtByPlayer(this.summoner, 100);
        }
        this.attackTime = 20;
        if (this.random.nextInt(10) == 0) {
            target.setInvulnerableTime(0);
            level.explode(this, this.getX(), this.getY(), this.getZ(), 1.5F, Level.ExplosionInteraction.NONE);
            this.discard();
        } else if (this.random.nextBoolean()) {
            Vec3 motion = target.getDeltaMovement();
            this.doHurtTarget(level, target);
            target.setDeltaMovement(motion);
        } else {
            target.igniteForSeconds(this.isSummoned() ? 4 : 2);
        }
        this.playSound(SoundEvents.BAT_HURT, 0.5F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        if (this.isHanging()) {
            this.setHanging(false);
        }
        if (source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.setTarget(attacker);
        }
        return super.hurt(source, damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_FLAGS, input.getByteOr("BatFlags", (byte) 0));
        this.damBonus = input.getByteOr("damBonus", (byte) 0);
        this.summoner = input.read("Summoner", UUIDUtil.CODEC).orElse(null);
        updateAttackDamage();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("BatFlags", this.entityData.get(DATA_FLAGS));
        output.putByte("damBonus", (byte) this.damBonus);
        output.storeNullable("Summoner", UUIDUtil.CODEC, this.summoner);
    }

    public static boolean checkFireBatSpawnRules(
        EntityType<? extends Monster> type,
        ServerLevelAccessor level,
        MobSpawnType spawnReason,
        BlockPos pos,
        RandomSource random
    ) {
        if (spawnReason == MobSpawnType.NATURAL && !Config.SPAWN_FIRE_BATS.getAsBoolean()) {
            return false;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) > random.nextInt(7)) {
            return false;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.checkMobSpawnRules(type, level, spawnReason, pos, random);
    }
}
