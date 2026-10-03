package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BoreDigParticle extends TerrainParticle {
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    public BoreDigParticle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, BlockState state, BlockPos pos) {
        super(level, x, y, z, 0.0, 0.0, 0.0, state, pos);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        rCol *= 0.6F;
        gCol *= 0.6F;
        bCol *= 0.6F;
        quadSize = 0.1F * (random.nextFloat() * 0.3F + 0.4F);
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0));
        lifetime = base / 2 + random.nextInt(base);
        xd = random.nextGaussian() * 0.01;
        yd = random.nextGaussian() * 0.01;
        zd = random.nextGaussian() * 0.01;
        hasPhysics = false;
        if (Minecraft.getInstance().getCameraEntity() != null && Minecraft.getInstance().getCameraEntity().position().distanceTo(new Vec3(x, y, z)) > 64.0) {
            lifetime = 0;
        }
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        boolean arrived = Mth.floor(x) == Mth.floor(targetX) && Mth.floor(y) == Mth.floor(targetY) && Mth.floor(z) == Mth.floor(targetZ);
        if (age++ >= lifetime || arrived) {
            remove();
            return;
        }
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double pull = 0.3;
        if (distance < 4.0) {
            quadSize *= 0.9F;
            pull = 0.6;
        }
        xd = Mth.clamp(xd + dx / distance * pull, -0.35, 0.35);
        yd = Mth.clamp(yd + dy / distance * pull, -0.35, 0.35);
        zd = Mth.clamp(zd + dz / distance * pull, -0.35, 0.35);
    }
}
