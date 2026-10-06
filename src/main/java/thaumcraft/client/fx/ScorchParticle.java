package thaumcraft.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class ScorchParticle extends TcParticle {
    private static final ResourceLocation FLAME = ResourceLocation.withDefaultNamespace("textures/particle/flame.png");

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float baseScale;

    public ScorchParticle(ClientLevel level, double x, double y, double z, Vec3 direction, float spread) {
        super(level, x, y, z);
        this.targetX = x + direction.x * 100.0 + (this.random.nextFloat() - this.random.nextFloat()) * spread;
        this.targetY = y + direction.y * 100.0 + (this.random.nextFloat() - this.random.nextFloat()) * spread;
        this.targetZ = z + direction.z * 100.0 + (this.random.nextFloat() - this.random.nextFloat()) * spread;
        this.baseScale = this.random.nextFloat() * 0.5F + 2.0F;
        this.lifetime = 50;
        this.setSize(0.1F, 0.1F);
        this.hasPhysics = false;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = (float) this.age / this.lifetime;
        return 0.1F * baseScale * (progress + 0.25F) * 2.0F;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        float color = Math.min(1.0F, this.age * 9.0F / this.lifetime);
        this.rCol = color;
        this.gCol = color;
        this.bCol = 1.0F;
        return TcParticleLayers.translucent(FLAME);
    }

    @Override
    public void tick() {
        double dx = targetX - this.x;
        double dy = targetY - this.y;
        double dz = targetZ - this.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double fade = (double) (this.lifetime - this.age) / this.lifetime;
        this.xd = dx / (distance * 1.25) * fade;
        this.yd = dy / (distance * 1.25) * fade;
        this.zd = dz / (distance * 1.25) * fade;
        savePrevious();
        this.xd += this.random.nextFloat() * 0.07F - 0.035F;
        this.yd += this.random.nextFloat() * 0.07F - 0.035F;
        this.zd += this.random.nextFloat() * 0.07F - 0.035F;
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        if (this.age > 1 && this.level.getBlockState(pos).isSolidRender()) {
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
            this.age += 10;
        }
        moveRaw();
        if (++this.age >= this.lifetime) {
            this.remove();
        }
    }
}
