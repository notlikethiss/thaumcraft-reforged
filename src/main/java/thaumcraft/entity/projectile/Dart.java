package thaumcraft.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.ModEntities;

public class Dart extends AbstractArrow {
    private boolean first = true;

    public Dart(EntityType<? extends Dart> type, Level level) {
        super(type, level);
    }

    public Dart(Level level, LivingEntity shooter, LivingEntity target, float speed, float inaccuracy) {
        super(ModEntities.DART.get(), level);
        this.setOwner(shooter);
        double y = shooter.getEyeY() - 0.1F;
        double dx = target.getX() - shooter.getX();
        double dy = target.getEyeY() - 0.7F - y;
        double dz = target.getZ() - shooter.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal >= 1.0E-7) {
            float yaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float pitch = (float) -(Mth.atan2(dy, horizontal) * 180.0 / Math.PI);
            this.snapTo(shooter.getX() + dx / horizontal / 5.0, y, shooter.getZ() + dz / horizontal / 5.0, yaw, pitch);
            this.shoot(dx, dy + horizontal * 0.2F, dz, speed, inaccuracy);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        if (this.first && this.level().isClientSide()) {
            this.first = false;
            Vec3 motion = this.getDeltaMovement();
            for (int index = 0; index < 5; index++) {
                this.level().addParticle(
                    ParticleTypes.SMOKE,
                    this.getX() - motion.x / 1.5,
                    this.getY() - motion.y / 1.5,
                    this.getZ() - motion.z / 1.5,
                    motion.x / 9.0 + this.random.nextGaussian() * 0.01,
                    motion.y / 9.0 + this.random.nextGaussian() * 0.01,
                    motion.z / 9.0 + this.random.nextGaussian() * 0.01
                );
            }
        }
        super.tick();
    }
}
