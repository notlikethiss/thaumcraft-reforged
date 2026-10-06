package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entity.projectile.Dart;

public class DartRenderer extends EntityRenderer<Dart> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private static final int TINT = 0xFF7F7F99;

    public DartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Dart entity) {
        return TEXTURE;
    }

    @Override
    public void render(Dart entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        poseStack.scale(0.75F, 1.0F, 1.0F);
        float shake = entity.shakeTime - partialTick;
        if (shake > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        vertex(pose, buffer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, light);
        vertex(pose, buffer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, light);
        vertex(pose, buffer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, light);
        vertex(pose, buffer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, light);
        vertex(pose, buffer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, light);
        vertex(pose, buffer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, light);
        vertex(pose, buffer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, light);
        vertex(pose, buffer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, light);
        for (int side = 0; side < 4; side++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(pose, buffer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, light);
            vertex(pose, buffer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, light);
            vertex(pose, buffer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, light);
            vertex(pose, buffer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, light);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, int x, int y, int z, float u, float v, int normalX, int normalY, int normalZ, int light) {
        buffer.addVertex(pose, (float) x, (float) y, (float) z)
            .setColor(TINT)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, (float) normalX, (float) normalZ, (float) normalY);
    }
}
