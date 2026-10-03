package thaumcraft.client.fx.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class WorldFx {
    protected static final int FULL_BRIGHT = 0xF000F0;

    protected double x;
    protected double y;
    protected double z;
    protected double prevX;
    protected double prevY;
    protected double prevZ;
    protected int age;
    protected int maxAge;

    protected WorldFx(double x, double y, double z, int maxAge) {
        this.x = x;
        this.y = y;
        this.z = z;
        prevX = x;
        prevY = y;
        prevZ = z;
        this.maxAge = maxAge;
    }

    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        age++;
    }

    public boolean isDead() {
        return age > maxAge;
    }

    public double distanceTo(Vec3 position) {
        return position.distanceTo(new Vec3(x, y, z));
    }

    protected float renderX(float partialTick, Vec3 camera) {
        return (float) (Mth.lerp(partialTick, prevX, x) - camera.x);
    }

    protected float renderY(float partialTick, Vec3 camera) {
        return (float) (Mth.lerp(partialTick, prevY, y) - camera.y);
    }

    protected float renderZ(float partialTick, Vec3 camera) {
        return (float) (Mth.lerp(partialTick, prevZ, z) - camera.z);
    }

    public abstract RenderType renderType();

    public abstract void render(PoseStack.Pose pose, VertexConsumer buffer, float partialTick, Vec3 camera);
}
