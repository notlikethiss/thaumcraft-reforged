package thaumcraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.ModEntities;

public class SpecialItem extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(SpecialItem.class, EntityDataSerializers.ITEM_STACK);

    public int age;
    public int pickupDelay;
    public final float hoverStart = (float) (Math.random() * Math.PI * 2.0);
    protected int health = 5;
    protected int lifespan = 6000;
    protected double gravity = 0.04F;

    public SpecialItem(EntityType<? extends SpecialItem> type, Level level) {
        super(type, level);
    }

    public SpecialItem(Level level, double x, double y, double z, ItemStack stack) {
        this(ModEntities.SPECIAL_ITEM.get(), level, x, y, z, stack);
        this.setDeltaMovement(Math.random() * 0.2F - 0.1F, 0.2F, Math.random() * 0.2F - 0.1F);
    }

    protected SpecialItem(EntityType<? extends SpecialItem> type, Level level, double x, double y, double z, ItemStack stack) {
        this(type, level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM, stack);
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.pickupDelay > 0) {
            this.pickupDelay--;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 motion = this.getDeltaMovement();
        if (motion.y > 0.0) {
            motion = motion.subtract(0.0, this.gravity, 0.0);
        }
        if (this.gravity == 0.0 && motion.y < 0.01F) {
            motion = new Vec3(motion.x, 0.0, motion.z);
        }
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            motion = new Vec3(
                (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                0.2F,
                (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
            this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
        }
        this.setDeltaMovement(motion);
        if (!this.noPhysics && !this.level().noCollision(this.getBoundingBox().deflate(1.0E-7))) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = 0.875F;
        if (this.onGround()) {
            BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1.0, this.getZ());
            friction = this.level().getBlockState(below).getFriction(this.level(), below, this) * 0.98F;
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(friction));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.5, 1.0));
        }
        this.age++;
        if (this.age >= this.lifespan) {
            this.discard();
        }
        if (!this.level().isClientSide() && this.getItem().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        this.markHurt();
        this.health -= (int) damage;
        if (this.health <= 0) {
            this.discard();
        }
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide() || this.pickupDelay > 0) {
            return;
        }
        ItemStack stack = this.getItem();
        int count = stack.getCount();
        ItemStack remaining = stack.copy();
        if (player.getInventory().add(remaining)) {
            player.take(this, count);
            player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), count);
            this.discard();
        } else if (remaining.getCount() != count) {
            player.take(this, count - remaining.getCount());
            this.setItem(remaining);
        }
    }

    @Override
    protected Component getTypeName() {
        return this.getItem().isEmpty() ? super.getTypeName() : this.getItem().getHoverName();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putShort("Health", (short) this.health);
        output.putShort("Age", (short) this.age);
        output.putInt("Lifespan", this.lifespan);
        output.putDouble("gravity", this.gravity);
        if (!this.getItem().isEmpty()) {
            output.store("Item", ItemStack.CODEC, this.getItem());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.health = input.getShortOr("Health", (short) 5) & 255;
        this.age = input.getShortOr("Age", (short) 0);
        this.lifespan = input.getIntOr("Lifespan", 6000);
        this.gravity = input.getDoubleOr("gravity", 0.04F);
        this.setItem(input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        if (this.getItem().isEmpty()) {
            this.discard();
        }
    }
}
