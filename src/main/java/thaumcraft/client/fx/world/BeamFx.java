package thaumcraft.client.fx.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.TcRenderTypes;

public class BeamFx extends WorldFx {
    private static final ResourceLocation[] TEXTURES = {
        Thaumcraft.id("textures/misc/beam.png"),
        Thaumcraft.id("textures/misc/beam1.png"),
        Thaumcraft.id("textures/misc/beam2.png")
    };
    private static final int ROTATION_SPEED = 5;

    protected final Level level;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected final float red;
    protected final float green;
    protected final float blue;
    protected final int type;
    protected float endMod;
    protected final boolean reverse;
    protected boolean pulse;
    protected boolean dayTimeSlide;
    protected float length;
    protected float yaw;
    protected float pitch;
    protected float prevYaw;
    protected float prevPitch;
    protected float prevSize;

    public BeamFx(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, int color, int type, boolean reverse, float endMod,
                  int maxAge) {
        super(x, y, z, maxAge);
        this.level = level;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        red = (color >> 16 & 255) / 255.0F;
        green = (color >> 8 & 255) / 255.0F;
        blue = (color & 255) / 255.0F;
        this.type = Math.clamp(type, 0, TEXTURES.length - 1);
        this.reverse = reverse;
        this.endMod = endMod;
        updateAngles();
        prevYaw = yaw;
        prevPitch = pitch;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() != null && minecraft.getCameraEntity().position().distanceTo(new Vec3(x, y, z)) > 50.0) {
            this.maxAge = 0;
        }
    }

    protected void updateAngles() {
        float dx = (float) (x - targetX);
        float dy = (float) (y - targetY);
        float dz = (float) (z - targetZ);
        length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        yaw = (float) (Math.atan2(dx, dz) * 180.0 / Math.PI);
        pitch = (float) (Math.atan2(dy, horizontal) * 180.0 / Math.PI);
    }

    @Override
    public void tick() {
        super.tick();
        prevYaw = yaw;
        prevPitch = pitch;
        updateAngles();
    }

    @Override
    public RenderType renderType() {
        return TcRenderTypes.additive(TEXTURES[type]);
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer buffer, float partialTick, Vec3 camera) {
        float slide = (dayTimeSlide ? level.getDefaultClockTime() : level.getGameTime()) + partialTick;
        float size = 1.0F;
        float opacity = 0.4F;
        if (pulse) {
            size = Math.min(age / 4.0F, 1.0F);
            size = prevSize + (size - prevSize) * partialTick;
            if (maxAge - age <= 4) {
                opacity = 0.4F - (4 - (maxAge - age)) * 0.1F;
            }
            prevSize = size;
        }
        float rotation = (float) (level.getDefaultClockTime() % (360 / ROTATION_SPEED) * ROTATION_SPEED) + ROTATION_SPEED * partialTick;
        if (reverse) {
            slide *= -1.0F;
        }
        float offset = -slide * 0.2F - Mth.floor(-slide * 0.1F);
        Matrix4f matrix = new Matrix4f(pose.pose())
            .translate(renderX(partialTick, camera), renderY(partialTick, camera), renderZ(partialTick, camera))
            .rotateX((float) Math.toRadians(90.0F))
            .rotateZ((float) Math.toRadians(-(180.0F + Mth.lerp(partialTick, prevYaw, yaw))))
            .rotateX((float) Math.toRadians(Mth.lerp(partialTick, prevPitch, pitch)))
            .rotateY((float) Math.toRadians(rotation));
        float near = 0.15F * size;
        float far = 0.15F * size * endMod;
        float beamLength = length * size;
        for (int pass = 0; pass < 3; pass++) {
            float v0 = -1.0F + offset + pass / 3.0F;
            float v1 = beamLength + v0;
            matrix.rotateY((float) Math.toRadians(60.0F));
            vertex(matrix, buffer, -far, beamLength, 1.0F, v1, opacity);
            vertex(matrix, buffer, -near, 0.0F, 1.0F, v0, opacity);
            vertex(matrix, buffer, near, 0.0F, 0.0F, v0, opacity);
            vertex(matrix, buffer, far, beamLength, 0.0F, v1, opacity);
        }
    }

    private void vertex(Matrix4f matrix, VertexConsumer buffer, float x, float y, float u, float v, float opacity) {
        buffer.addVertex(matrix, x, y, 0.0F).setUv(u, v).setColor(red, green, blue, opacity).setLight(FULL_BRIGHT);
    }
}
