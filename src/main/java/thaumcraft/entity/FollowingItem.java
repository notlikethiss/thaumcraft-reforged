package thaumcraft.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;

public class FollowingItem extends SpecialItem implements IEntityWithComplexSpawn {
    private @Nullable Entity target;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int type;
    private int approach = 20;

    public FollowingItem(EntityType<? extends FollowingItem> type, Level level) {
        super(type, level);
    }

    public FollowingItem(Level level, double x, double y, double z, ItemStack stack, Entity target, int type) {
        super(ModEntities.FOLLOWING_ITEM.get(), level, x, y, z, stack);
        this.target = target;
        this.targetX = target.getX();
        this.targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0F;
        this.targetZ = target.getZ();
        this.type = type;
        this.noPhysics = true;
    }

    public FollowingItem(Level level, double x, double y, double z, ItemStack stack, double targetX, double targetY, double targetZ) {
        super(ModEntities.FOLLOWING_ITEM.get(), level, x, y, z, stack);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    @Override
    public void tick() {
        if (this.target != null) {
            this.targetX = this.target.getX();
            this.targetY = this.target.getBoundingBox().minY + this.target.getBbHeight() / 2.0F;
            this.targetZ = this.target.getZ();
        }
        if (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, this.gravity, 0.0));
        } else {
            float dx = (float) (this.targetX - this.getX());
            float dy = (float) (this.targetY - this.getY());
            float dz = (float) (this.targetZ - this.getZ());
            if (this.approach > 1) {
                this.approach--;
            }
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0.5) {
                distance *= this.approach;
                this.setDeltaMovement(dx / distance, dy / distance, dz / distance);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.1F));
                this.targetX = 0.0;
                this.targetY = 0.0;
                this.targetZ = 0.0;
                this.target = null;
                this.noPhysics = false;
            }
            if (this.level().isClientSide()) {
                float x = (float) this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F;
                float y = (float) this.yo + this.getBbHeight() / 2.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F;
                float z = (float) this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.125F;
                if (this.type != 10) {
                    Fx.get().sparkle(x, y, z, this.type);
                } else {
                    Fx.get().crucibleBubble(this.level(), x, y, z, 0.33F, 0.33F, 1.0F);
                }
            }
        }
        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("type", (short) this.type);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.type = input.getShortOr("type", (short) 0);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.target == null ? -1 : this.target.getId());
        buffer.writeDouble(this.targetX);
        buffer.writeDouble(this.targetY);
        buffer.writeDouble(this.targetZ);
        buffer.writeByte(this.type);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        int targetId = buffer.readInt();
        if (targetId > -1) {
            this.target = this.level().getEntity(targetId);
            this.noPhysics = true;
        }
        this.targetX = buffer.readDouble();
        this.targetY = buffer.readDouble();
        this.targetZ = buffer.readDouble();
        this.type = buffer.readByte();
    }
}
