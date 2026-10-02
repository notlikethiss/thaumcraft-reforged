package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.world.entity.Entity;

public class BubbleParticle extends TcParticle {
    public int particle = 16;
    public double bubbleSpeed = 0.002;
    private float particleScale;

    public BubbleParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, int age) {
        super(level, x, y, z);
        this.rCol = 1.0F;
        this.gCol = 0.0F;
        this.bCol = 0.5F;
        this.alpha = 0.66F;
        this.setSize(0.02F, 0.02F);
        this.hasPhysics = false;
        this.particleScale = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F * (this.random.nextFloat() * 0.3F + 0.2F);
        this.xd = xd * 0.2F + (float) (Math.random() * 2.0 - 1.0) * 0.02F;
        this.yd = yd * 0.2F + (float) Math.random() * 0.02F;
        this.zd = zd * 0.2F + (float) (Math.random() * 2.0 - 1.0) * 0.02F;
        this.lifetime = (int) (age + 2 + 8.0 / (Math.random() * 0.8 + 0.2));
        Entity camera = Minecraft.getInstance().getCameraEntity();
        if (camera != null && camera.distanceToSqr(x, y, z) > 50 * 50) {
            this.lifetime = 0;
        }
    }

    public void setFroth() {
        particleScale *= 0.75F;
        this.lifetime = 4 + this.random.nextInt(3);
        bubbleSpeed = -0.001;
        this.xd /= 5.0;
        this.yd /= 10.0;
        this.zd /= 5.0;
    }

    public void setFroth2() {
        particleScale *= 0.75F;
        this.lifetime = 12 + this.random.nextInt(12);
        bubbleSpeed = -0.005;
        this.xd /= 5.0;
        this.yd /= 10.0;
        this.zd /= 5.0;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return 0.1F * particleScale;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        float u0 = particle % 16 / 16.0F;
        float v0 = particle / 16 / 16.0F;
        setUv(u0, u0 + 0.0624375F, v0, v0 + 0.0624375F);
        return TcParticleLayers.additive(TcParticleLayers.PARTICLES);
    }

    @Override
    public void tick() {
        savePrevious();
        this.yd += bubbleSpeed;
        if (bubbleSpeed > 0.0) {
            this.xd += (this.random.nextFloat() - this.random.nextFloat()) * 0.01F;
            this.zd += (this.random.nextFloat() - this.random.nextFloat()) * 0.01F;
        }
        moveRaw();
        this.xd *= 0.85F;
        this.yd *= 0.85F;
        this.zd *= 0.85F;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else if (this.lifetime <= 2) {
            particle++;
        }
    }
}
