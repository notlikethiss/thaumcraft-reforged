package thaumcraft.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;

public class BurstParticle extends TcParticle {
    private final float particleScale;

    public BurstParticle(ClientLevel level, double x, double y, double z, float size) {
        super(level, x, y, z);
        this.particleScale = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F * size;
        this.lifetime = 31;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return particleScale;
    }

    @Override
    protected void prepareRender() {
        float u0 = this.age % 32 / 32.0F;
        setUv(u0, u0 + 0.03125F, 0.0F, 1.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TcParticleLayers.additive(TcParticleLayers.BURST);
    }

    @Override
    public void tick() {
        savePrevious();
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }
}
