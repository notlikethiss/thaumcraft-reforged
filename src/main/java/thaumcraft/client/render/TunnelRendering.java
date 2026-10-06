package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public final class TunnelRendering {
    private TunnelRendering() {
    }

    public static void submitPlane(PoseStack poseStack, MultiBufferSource bufferSource, Direction side, float depth, float inset) {
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = bufferSource.getBuffer(TcRenderTypes.tunnel());
        float plane = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F - depth : depth;
        float min = inset;
        float max = 1.0F - inset;
        float[][] corners = switch (side.getAxis()) {
            case X -> new float[][] {{plane, min, min}, {plane, max, min}, {plane, max, max}, {plane, min, max}};
            case Y -> new float[][] {{min, plane, min}, {min, plane, max}, {max, plane, max}, {max, plane, min}};
            case Z -> new float[][] {{min, min, plane}, {min, max, plane}, {max, max, plane}, {max, min, plane}};
        };
        for (float[] corner : corners) {
            vertex(pose, buffer, corner);
        }
        for (int index = corners.length - 1; index >= 0; index--) {
            vertex(pose, buffer, corners[index]);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float[] corner) {
        buffer.addVertex(pose, corner[0], corner[1], corner[2]);
    }
}
