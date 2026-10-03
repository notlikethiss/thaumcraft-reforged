package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4f;

public final class RayRendering {
    private static final float HALF_SQRT_3 = 0.866F;

    private RayRendering() {
    }

    public static void submitRays(PoseStack poseStack, SubmitNodeCollector collector, int count, float age, int outerColor) {
        collector.submitCustomGeometry(poseStack, RenderTypes.dragonRays(), (pose, buffer) -> {
            float time = age / 500.0F;
            float grow = 30.0F / (Math.min(age, 10.0F) / 10.0F);
            Random random = new Random(245L);
            Matrix4f matrix = new Matrix4f(pose.pose());
            for (int index = 0; index < count; index++) {
                matrix.rotateX((float) Math.toRadians(random.nextFloat() * 360.0F));
                matrix.rotateY((float) Math.toRadians(random.nextFloat() * 360.0F));
                matrix.rotateZ((float) Math.toRadians(random.nextFloat() * 360.0F));
                matrix.rotateX((float) Math.toRadians(random.nextFloat() * 360.0F));
                matrix.rotateY((float) Math.toRadians(random.nextFloat() * 360.0F));
                matrix.rotateZ((float) Math.toRadians(random.nextFloat() * 360.0F + time * 360.0F));
                float length = (random.nextFloat() * 20.0F + 5.0F) / grow;
                float width = (random.nextFloat() * 2.0F + 1.0F) / grow;
                float[][] corners = {{-HALF_SQRT_3 * width, length, -0.5F * width}, {HALF_SQRT_3 * width, length, -0.5F * width}, {0.0F, length, width}};
                for (int side = 0; side < 3; side++) {
                    float[] first = corners[side];
                    float[] second = corners[(side + 1) % 3];
                    buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(0xFFFFFFFF);
                    buffer.addVertex(matrix, first[0], first[1], first[2]).setColor(outerColor);
                    buffer.addVertex(matrix, second[0], second[1], second[2]).setColor(outerColor);
                }
            }
        });
    }
}
