package thaumcraft.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;

public class SmokeSpiralParticle extends TcParticle {
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final float radius;
    private final int start;
    private final int minY;
    private final float scale;
    private int particle;

    public SmokeSpiralParticle(ClientLevel level, double x, double y, double z, float radius, int start, int minY) {
        super(level, x, y, z);
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.radius = radius;
        this.start = start;
        this.minY = minY;
        this.scale = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.lifetime = 20 + this.random.nextInt(10);
        this.alpha = 0.66F;
        this.hasPhysics = false;
        this.setSize(0.01F, 0.01F);
        place();
        savePrevious();
    }

    private void place() {
        float progress = (float) this.age / this.lifetime;
        float yaw = (this.start + 720.0F * progress) / 180.0F * (float) Math.PI;
        float pitch = (90.0F - 180.0F * progress) / 180.0F * (float) Math.PI;
        double offsetX = -Mth.sin(yaw) * Mth.cos(pitch) * radius;
        double offsetY = -Mth.sin(pitch) * radius;
        double offsetZ = Mth.cos(yaw) * Mth.cos(pitch) * radius;
        this.setPos(centerX + offsetX, Math.max(centerY + offsetY, minY + 0.1F), centerZ + offsetZ);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return 0.15F * scale;
    }

    @Override
    protected void prepareRender() {
        float u0 = particle % 16 / 16.0F;
        float v0 = particle / 16 / 16.0F;
        setUv(u0, u0 + 0.0624375F, v0, v0 + 0.0624375F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TcParticleLayers.translucent(TcParticleLayers.PARTICLES);
    }

    @Override
    public void tick() {
        savePrevious();
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }
        particle = 5 + this.age % 4;
        place();
    }
}
