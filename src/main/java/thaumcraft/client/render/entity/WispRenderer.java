package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.TcRenderTypes;
import thaumcraft.entity.monster.Wisp;

public class WispRenderer extends EntityRenderer<Wisp, WispRenderState> {
    private static final ResourceLocation WISP = Thaumcraft.id("textures/misc/wisp.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public WispRenderState createRenderState() {
        return new WispRenderState();
    }

    @Override
    public void extractRenderState(Wisp entity, WispRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.alive = entity.getHealth() > 0.0F;
        state.color = entity.getAspect().color;
        state.hurt = entity.hurtTime > 0;
        state.frame = entity.tickCount % 16;
    }

    @Override
    public void submit(WispRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.alive) {
            int red = state.color >> 16 & 0xFF;
            int green = state.color >> 8 & 0xFF;
            int blue = state.color & 0xFF;
            if (state.hurt) {
                red = 255;
                green = (int) (green / 300.0F * 255.0F);
                blue = (int) (blue / 300.0F * 255.0F);
            }
            poseStack.pushPose();
            poseStack.rotate(camera.orientation);
            quad(submitNodeCollector, poseStack, TcRenderTypes.additive(WISP), state.frame, 4, 1.0F, red, green, blue);
            float size = 0.4F + Mth.sin(state.ageInTicks / 10.0F) * 0.1F;
            quad(submitNodeCollector, poseStack, TcRenderTypes.additive(PARTICLES), 24 + state.frame, 8, size, 255, 255, 255);
            poseStack.popPose();
        }
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void quad(
        SubmitNodeCollector collector,
        PoseStack poseStack,
        RenderType renderType,
        int frame,
        int grid,
        float size,
        int red,
        int green,
        int blue
    ) {
        float u0 = (float) (frame % grid) / grid;
        float u1 = (frame % grid + 0.99F) / grid;
        float v0 = (float) (frame / grid) / grid;
        float v1 = (frame / grid + 0.99F) / grid;
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            vertex(buffer, pose, -size, -size, u1, v1, red, green, blue);
            vertex(buffer, pose, -size, size, u1, v0, red, green, blue);
            vertex(buffer, pose, size, size, u0, v0, red, green, blue);
            vertex(buffer, pose, size, -size, u0, v1, red, green, blue);
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int red, int green, int blue) {
        buffer.addVertex(pose, x, y, 0.0F).setUv(u, v).setColor(red, green, blue, 255).setLight(FULL_BRIGHT);
    }
}
