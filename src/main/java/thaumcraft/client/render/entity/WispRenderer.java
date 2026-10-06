package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.TcRenderTypes;
import thaumcraft.entity.monster.Wisp;

public class WispRenderer extends EntityRenderer<Wisp> {
    private static final ResourceLocation WISP = Thaumcraft.id("textures/misc/wisp.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(Wisp entity) {
        return WISP;
    }

    @Override
    public void render(Wisp entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (entity.getHealth() > 0.0F) {
            int color = entity.getAspect().color;
            int red = color >> 16 & 0xFF;
            int green = color >> 8 & 0xFF;
            int blue = color & 0xFF;
            if (entity.hurtTime > 0) {
                red = 255;
                green = (int) (green / 300.0F * 255.0F);
                blue = (int) (blue / 300.0F * 255.0F);
            }
            int frame = entity.tickCount % 16;
            float ageInTicks = entity.tickCount + partialTick;
            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            quad(bufferSource.getBuffer(TcRenderTypes.additive(WISP)), poseStack, frame, 4, 1.0F, red, green, blue);
            float size = 0.4F + Mth.sin(ageInTicks / 10.0F) * 0.1F;
            quad(bufferSource.getBuffer(TcRenderTypes.additive(PARTICLES)), poseStack, 24 + frame, 8, size, 255, 255, 255);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, light);
    }

    private static void quad(VertexConsumer buffer, PoseStack poseStack, int frame, int grid, float size, int red, int green, int blue) {
        float u0 = (float) (frame % grid) / grid;
        float u1 = (frame % grid + 0.99F) / grid;
        float v0 = (float) (frame / grid) / grid;
        float v1 = (frame / grid + 0.99F) / grid;
        PoseStack.Pose pose = poseStack.last();
        vertex(buffer, pose, -size, -size, u1, v1, red, green, blue);
        vertex(buffer, pose, -size, size, u1, v0, red, green, blue);
        vertex(buffer, pose, size, size, u0, v0, red, green, blue);
        vertex(buffer, pose, size, -size, u0, v1, red, green, blue);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int red, int green, int blue) {
        buffer.addVertex(pose, x, y, 0.0F)
            .setColor(red, green, blue, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
