package thaumcraft.entity.projectile;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModItems;

public class Alumentum extends ThrowableItemProjectile {
    public Alumentum(EntityType<? extends Alumentum> type, Level level) {
        super(type, level);
    }

    public Alumentum(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.ALUMENTUM.get(), owner, level, stack);
    }

    public Alumentum(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.ALUMENTUM.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ALUMENTUM.get();
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (!level.isClientSide()) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int index = 0; index < 3; index++) {
            Fx.get().wispFX2(level, this.getX() + jitter(random, 0.3F), this.getY() + jitter(random, 0.3F), this.getZ() + jitter(random, 0.3F), 0.3F, 5, true, 0.02F);
            double x = (this.getX() + this.xo) / 2.0 + jitter(random, 0.3F);
            double y = (this.getY() + this.yo) / 2.0 + jitter(random, 0.3F);
            double z = (this.getZ() + this.zo) / 2.0 + jitter(random, 0.3F);
            Fx.get().wispFX2(level, x, y, z, 0.3F, 5, true, 0.02F);
            Fx.get().sparkle(
                (float) this.getX() + jitter(random, 0.1F),
                (float) this.getY() + jitter(random, 0.1F),
                (float) this.getZ() + jitter(random, 0.1F),
                6
            );
        }
    }

    private static float jitter(RandomSource random, float scale) {
        return (random.nextFloat() - random.nextFloat()) * scale;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), 1.5F, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }
}
