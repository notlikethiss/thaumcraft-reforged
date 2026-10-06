package thaumcraft.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;

public class FrostShard extends Projectile {
    private static final EntityDataAccessor<Integer> DATA_TICKS_IN_GROUND = SynchedEntityData.defineId(FrostShard.class, EntityDataSerializers.INT);

    private @Nullable BlockPos inPos;
    private @Nullable BlockState inState;
    private boolean inGround;
    private int ticksInAir;
    private double damage = 1.0;
    private int knockbackStrength;

    public FrostShard(EntityType<? extends FrostShard> type, Level level) {
        super(type, level);
    }

    public FrostShard(Level level, LivingEntity shooter, float speed) {
        this(ModEntities.FROST_SHARD.get(), level);
        this.setOwner(shooter);
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        Vec3 look = shooter.getViewVector(1.0F);
        double x = shooter.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F + look.x;
        double y = shooter.getEyeY() - 0.15F + look.y;
        double z = shooter.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F + look.z;
        this.moveTo(x, y, z, yaw, pitch);
        double motionX = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double motionZ = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double motionY = -Mth.sin(pitch / 180.0F * (float) Math.PI);
        this.shoot(motionX, motionY, motionZ, speed * 1.5F, 1.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_TICKS_IN_GROUND, 0);
    }

    public int getTicksInGround() {
        return this.entityData.get(DATA_TICKS_IN_GROUND);
    }

    private void setTicksInGround(int ticks) {
        this.entityData.set(DATA_TICKS_IN_GROUND, ticks);
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setKnockbackStrength(int knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }

    @Override
    public void shoot(double xd, double yd, double zd, float pow, float uncertainty) {
        Vec3 direction = new Vec3(xd, yd, zd).normalize()
            .add(
                this.random.nextGaussian() * 0.0075F * uncertainty,
                this.random.nextGaussian() * 0.0075F * uncertainty,
                this.random.nextGaussian() * 0.0075F * uncertainty
            )
            .scale(pow);
        this.setDeltaMovement(direction);
        double horizontal = direction.horizontalDistance();
        this.setYRot((float) (Mth.atan2(direction.x, direction.z) * 180.0 / Math.PI));
        this.setXRot((float) (Mth.atan2(direction.y, horizontal) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.setTicksInGround(0);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            Vec3 motion = this.getDeltaMovement();
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * 180.0 / Math.PI));
            this.setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * 180.0 / Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        if (this.inPos != null) {
            BlockState state = level.getBlockState(this.inPos);
            VoxelShape shape = state.getCollisionShape(level, this.inPos);
            if (!shape.isEmpty() && shape.bounds().move(this.inPos).contains(this.position())) {
                this.inGround = true;
            }
        }
        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        if (this.inGround) {
            if (this.inPos != null && level.getBlockState(this.inPos) == this.inState) {
                int ticks = this.getTicksInGround() + 1;
                if (!level.isClientSide()) {
                    this.setTicksInGround(ticks);
                }
                if (ticks >= 200) {
                    this.discard();
                }
            } else {
                this.inGround = false;
                this.setDeltaMovement(this.getDeltaMovement().multiply(
                    this.random.nextFloat() * 0.2F,
                    this.random.nextFloat() * 0.2F,
                    this.random.nextFloat() * 0.2F
                ));
                this.setTicksInGround(0);
                this.ticksInAir = 0;
            }
        } else {
            flyTick(level);
        }
        BlockPos previous = BlockPos.containing(oldX, oldY, oldZ);
        if (!level.isClientSide() && level.getFluidState(previous).is(Fluids.WATER) && level.getBlockState(previous).is(Blocks.WATER)) {
            level.setBlock(previous, Blocks.ICE.defaultBlockState(), 3);
            this.discard();
        }
    }

    private void flyTick(Level level) {
        Fx.get().sparkle(
            (float) this.getX() - 0.1F + this.random.nextFloat() * 0.2F,
            (float) this.getY() + this.random.nextFloat() * 0.2F,
            (float) this.getZ() - 0.1F + this.random.nextFloat() * 0.2F,
            0.3F,
            6,
            0.005F
        );
        this.ticksInAir++;
        Vec3 from = this.position();
        Vec3 to = from.add(this.getDeltaMovement());
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            to = hit.getLocation();
        }
        AABB area = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level,
            this,
            from,
            to,
            area,
            entity -> entity.isPickable() && this.ticksInAir > 2,
            0.3F
        );
        if (entityHit != null) {
            hit = entityHit;
        }
        if (hit instanceof EntityHitResult result) {
            if (level instanceof ServerLevel serverLevel) {
                hitEntity(serverLevel, result.getEntity());
            }
        } else if (hit instanceof BlockHitResult result && hit.getType() == HitResult.Type.BLOCK) {
            hitBlock(level, result);
        }
        if (this.isRemoved()) {
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        float yaw = (float) (Mth.atan2(motion.x, motion.z) * 180.0 / Math.PI);
        float pitch = (float) (Mth.atan2(motion.y, motion.horizontalDistance()) * 180.0 / Math.PI);
        while (pitch - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }
        while (pitch - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }
        while (yaw - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }
        while (yaw - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }
        this.setXRot(this.xRotO + (pitch - this.xRotO) * 0.2F);
        this.setYRot(this.yRotO + (yaw - this.yRotO) * 0.2F);
        float drag = 0.99F;
        if (this.isInWater()) {
            for (int index = 0; index < 4; index++) {
                level.addParticle(
                    ParticleTypes.BUBBLE,
                    this.getX() - motion.x * 0.25,
                    this.getY() - motion.y * 0.25,
                    this.getZ() - motion.z * 0.25,
                    motion.x,
                    motion.y,
                    motion.z
                );
            }
            drag = 0.1F;
        }
        this.setDeltaMovement(motion.scale(drag).add(0.0, -0.05F, 0.0));
        this.checkInsideBlocks();
    }

    private void hitEntity(ServerLevel level, Entity target) {
        Vec3 motion = this.getDeltaMovement();
        float speed = (float) motion.length();
        if (target instanceof Blaze || target instanceof FireBat || target instanceof EnderMan) {
            speed = (int) (this.damage * 2.0);
        }
        int amount = Mth.ceil(speed * this.damage);
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().thrown(this, owner == null ? this : owner);
        if (target.hurt(source, amount)) {
            if (target instanceof LivingEntity living) {
                if (this.knockbackStrength > 0) {
                    double horizontal = motion.horizontalDistance();
                    if (horizontal > 0.0) {
                        living.push(motion.x * this.knockbackStrength * 0.6F / horizontal, 0.1, motion.z * this.knockbackStrength * 0.6F / horizontal);
                    }
                }
                EnchantmentHelper.doPostAttackEffects(level, living, source);
                if (owner instanceof ServerPlayer player && target != owner && target instanceof Player) {
                    player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
            }
            this.playSound(SoundEvents.PLAYER_HURT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (!(target instanceof EnderMan)) {
                this.discard();
            }
        } else {
            this.setDeltaMovement(motion.scale(-0.1F));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            this.ticksInAir = 0;
        }
    }

    private void hitBlock(Level level, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        this.inPos = pos;
        this.inState = level.getBlockState(pos);
        Vec3 motion = this.getDeltaMovement();
        Direction side = hit.getDirection();
        if (side != Direction.UP && (Math.abs(motion.x) >= 0.1F || Math.abs(motion.y) >= 0.1F || Math.abs(motion.z) >= 0.1F)) {
            this.setDeltaMovement(
                side.getStepX() * Math.abs(motion.x) * (0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                side.getStepY() * Math.abs(motion.y) * (0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                side.getStepZ() * Math.abs(motion.z) * (0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F)
            );
            this.ticksInAir = 0;
        } else {
            Vec3 offset = hit.getLocation().subtract(this.position());
            this.setDeltaMovement(offset);
            Vec3 back = offset.normalize().scale(0.05F);
            this.setPos(this.getX() - back.x, this.getY() - back.y, this.getZ() - back.z);
            this.inGround = true;
        }
        if (this.inState.isAir()) {
            return;
        }
        float hardness = this.inState.getDestroySpeed(level, pos);
        if (hardness > -1.0F && hardness <= 0.33F) {
            if (!level.isClientSide()) {
                level.destroyBlock(pos, true, this);
            }
        } else {
            this.playSound(this.inState.getSoundType(level, pos, this).getBreakSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.storeNullable("inPos", BlockPos.CODEC, this.inPos);
        output.storeNullable("inState", BlockState.CODEC, this.inState);
        output.putBoolean("inGround", this.inGround);
        output.putShort("life", (short) this.getTicksInGround());
        output.putDouble("damage", this.damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.inPos = input.read("inPos", BlockPos.CODEC).orElse(null);
        this.inState = input.read("inState", BlockState.CODEC).orElse(null);
        this.inGround = input.getBooleanOr("inGround", false);
        this.setTicksInGround(input.getShortOr("life", (short) 0));
        this.damage = input.getDoubleOr("damage", 1.0);
    }
}
