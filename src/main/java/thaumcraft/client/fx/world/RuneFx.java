package thaumcraft.client.fx.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import thaumcraft.client.fx.TcParticleLayers;
import thaumcraft.client.render.TcRenderTypes;

public class RuneFx extends WorldFx {
    private final float red;
    private final float green;
    private final float blue;
    private final float rotation;
    private final int runeIndex;
    private final double offsetX;
    private final double offsetY;
    private final float scale;
    private final float gravity;
    private double motionY;
    private float alpha;

    public RuneFx(double x, double y, double z, float red, float green, float blue, int duration, float gravity, RandomSource random) {
        super(x, y, z, 3 * duration);
        this.red = red == 0.0F ? 1.0F : red;
        this.green = green;
        this.blue = blue;
        this.gravity = gravity;
        rotation = random.nextInt(4) * 90;
        runeIndex = (int) (Math.random() * 16.0 + 160.0);
        offsetX = random.nextFloat() * 0.2;
        offsetY = -0.3 + random.nextFloat() * 0.6;
        scale = (float) (1.0 + random.nextGaussian() * 0.1F);
    }

    @Override
    public void tick() {
        super.tick();
        float threshold = maxAge / 5.0F;
        if (age - 1 <= threshold) {
            alpha = (age - 1) / threshold;
        } else {
            alpha = (float) (maxAge - age + 1) / maxAge;
        }
        motionY -= 0.04 * gravity;
        y += motionY;
    }

    @Override
    public RenderType renderType() {
        return TcRenderTypes.additive(TcParticleLayers.PARTICLES);
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer buffer, float partialTick, Vec3 camera) {
        Matrix4f matrix = new Matrix4f(pose.pose())
            .translate(renderX(partialTick, camera), renderY(partialTick, camera), renderZ(partialTick, camera))
            .rotateY((float) Math.toRadians(rotation))
            .rotateZ((float) Math.toRadians(90.0F))
            .translate((float) offsetX, (float) offsetY, -0.51F);
        float u0 = runeIndex % 16 / 16.0F;
        float u1 = u0 + 0.0624375F;
        float v0 = runeIndex / 16 / 16.0F;
        float v1 = v0 + 0.0624375F;
        float size = 0.3F * scale * 0.5F;
        float a = alpha / 2.0F;
        buffer.addVertex(matrix, -size, size, 0.0F).setUv(u1, v1).setColor(red, green, blue, a).setLight(FULL_BRIGHT);
        buffer.addVertex(matrix, size, size, 0.0F).setUv(u1, v0).setColor(red, green, blue, a).setLight(FULL_BRIGHT);
        buffer.addVertex(matrix, size, -size, 0.0F).setUv(u0, v0).setColor(red, green, blue, a).setLight(FULL_BRIGHT);
        buffer.addVertex(matrix, -size, -size, 0.0F).setUv(u0, v1).setColor(red, green, blue, a).setLight(FULL_BRIGHT);
    }
}
