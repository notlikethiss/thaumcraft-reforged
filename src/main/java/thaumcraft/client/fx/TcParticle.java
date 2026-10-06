package thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LightTexture;

public abstract class TcParticle extends SingleQuadParticle {
    protected float u0;
    protected float u1 = 1.0F;
    protected float v0;
    protected float v1 = 1.0F;

    protected TcParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.setSize(0.01F, 0.01F);
    }

    protected void setUv(float u0, float u1, float v0, float v1) {
        this.u0 = u0;
        this.u1 = u1;
        this.v0 = v0;
        this.v1 = v1;
    }

    @Override
    protected float getU0() {
        return u0;
    }

    @Override
    protected float getU1() {
        return u1;
    }

    @Override
    protected float getV0() {
        return v0;
    }

    @Override
    protected float getV1() {
        return v1;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    protected void prepareRender() {
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        prepareRender();
        super.render(buffer, camera, partialTick);
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void setNoClip(boolean noClip) {
        this.hasPhysics = !noClip;
    }

    protected void savePrevious() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    protected void moveRaw() {
        this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
    }
}
