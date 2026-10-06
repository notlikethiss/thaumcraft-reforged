package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class WispParticle extends TcParticle {
    public boolean shrink;
    public boolean tinkle;
    public int blendMode = 1;
    private final float moteScale;
    private final int moteHalfLife;

    public WispParticle(ClientLevel level, double x, double y, double z, float size, float red, float green, float blue) {
        super(level, x, y, z);
        if (red == 0.0F) {
            red = 1.0F;
        }
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.alpha = 0.5F;
        float particleScale = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F * size;
        this.moteScale = particleScale;
        this.lifetime = (int) (36.0 / (Math.random() * 0.3 + 0.7));
        this.moteHalfLife = this.lifetime / 2;
        Entity camera = Minecraft.getInstance().getCameraEntity();
        int visibleDistance = 50;
        if (camera != null && camera.distanceToSqr(x, y, z) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }
    }

    public WispParticle(ClientLevel level, double x, double y, double z, float size, int type) {
        this(level, x, y, z, size, 0.0F, 0.0F, 0.0F);
        float[] color = TcColors.typeColor(type == 7 ? -1 : type, level.getRandom());
        this.rCol = color[0];
        this.gCol = color[1];
        this.bCol = color[2];
        if (type == 5) {
            this.blendMode = 771;
        }
    }

    public WispParticle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float size, int type) {
        this(level, x, y, z, size, type);
        aimAt(tx, ty, tz);
    }

    public WispParticle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float size, float red, float green, float blue) {
        this(level, x, y, z, size, red, green, blue);
        aimAt(tx, ty, tz);
    }

    private void aimAt(double tx, double ty, double tz) {
        if (this.lifetime > 0) {
            this.xd = (tx - this.x) / this.lifetime;
            this.yd = (ty - this.y) / this.lifetime;
            this.zd = (tz - this.z) / this.lifetime;
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float ageScale;
        if (shrink) {
            ageScale = ((float) this.lifetime - this.age) / this.lifetime;
        } else {
            ageScale = (float) this.age / moteHalfLife;
            if (ageScale > 1.0F) {
                ageScale = 2.0F - ageScale;
            }
        }
        return 0.5F * moteScale * ageScale;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TcParticleLayers.byBlend(TcParticleLayers.P_LARGE, blendMode);
    }

    @Override
    public void tick() {
        savePrevious();
        if (this.age == 0 && tinkle && this.level.getRandom().nextInt(3) == 0) {
            this.level.playLocalSound(
                this.x,
                this.y,
                this.z,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.AMBIENT,
                0.02F,
                0.5F * ((this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.6F + 2.0F),
                false
            );
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        this.yd -= 0.04 * this.gravity;
        moveRaw();
        this.xd *= 0.98F;
        this.yd *= 0.98F;
        this.zd *= 0.98F;
    }
}
