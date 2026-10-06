package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;

public final class BoreRendering {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/bore.png");
    private static final ResourceLocation VORTEX = Thaumcraft.id("textures/misc/vortex.png");
    private static final ResourceLocation JAR = Thaumcraft.id("textures/model/jar.png");

    private BoreRendering() {
    }

    public record Models(List<ModelPart> base, List<ModelPart> nozzle, List<ModelPart> emitter, List<ModelPart> stand, List<ModelPart> standNozzle, ModelPart jarCore) {
        public static Models bake(EntityModelSet models) {
            ModelPart bore = models.bakeLayer(TcModelLayers.BORE);
            ModelPart stand = models.bakeLayer(TcModelLayers.BORE_BASE);
            return new Models(
                List.of(bore.getChild("base"), bore.getChild("side1"), bore.getChild("side2"), bore.getChild("crossbar")),
                List.of(bore.getChild("nozzle_front"), bore.getChild("nozzle_mid")),
                List.of(bore.getChild("knob"), bore.getChild("cross1"), bore.getChild("cross3"), bore.getChild("cross2"), bore.getChild("rod")),
                List.of(stand.getChild("base1"), stand.getChild("base2"), stand.getChild("pillar_mid"), stand.getChild("pillar2"), stand.getChild("pillar3"),
                    stand.getChild("pillar4"), stand.getChild("pillar1")),
                List.of(stand.getChild("nozzle1"), stand.getChild("nozzle2")),
                models.bakeLayer(TcModelLayers.JAR).getChild("core")
            );
        }
    }

    public record BoreState(float yaw, float pitch, boolean baseAbove, boolean hasWand, float topRotation, float vortex) {
    }

    private static void parts(List<ModelPart> parts, PoseStack poseStack, MultiBufferSource buffers, int light) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        for (ModelPart part : parts) {
            part.render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void renderBase(Models models, PoseStack poseStack, MultiBufferSource buffers, int light, Direction facing) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        parts(models.stand(), poseStack, buffers, light);
        float rotation = switch (facing) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        parts(models.standNozzle(), poseStack, buffers, light);
        poseStack.popPose();
    }

    public static void renderBore(Models models, PoseStack poseStack, MultiBufferSource buffers, int light, BoreState state) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw()));
        poseStack.pushPose();
        if (state.baseAbove()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
        poseStack.translate(0.0F, -0.5F, 0.0F);
        parts(models.base(), poseStack, buffers, light);
        poseStack.popPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.pitch()));
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, -0.5F, 0.0F);
        parts(models.nozzle(), poseStack, buffers, light);
        poseStack.popPose();
        if (state.hasWand()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(state.topRotation()));
            poseStack.translate(0.0F, 0.5F, 0.0F);
            parts(models.emitter(), poseStack, buffers, light);
            poseStack.popPose();
        }
        vortex(poseStack, buffers, -0.17F, -(state.vortex() * 8.0F), 10.0F, 0.4F, 1.0F);
        vortex(poseStack, buffers, -0.21F, state.vortex() * 8.0F, 10.0F, 0.3F, 0.8F);
        vortex(poseStack, buffers, -0.25F, -(state.vortex() * 8.0F), -10.0F, 0.2F, 0.8F);
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, 0.3F, 0.0F);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        models.jarCore().render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(JAR)), light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void vortex(PoseStack poseStack, MultiBufferSource buffers, float offset, float spin, float tilt, float scale, float opacity) {
        poseStack.pushPose();
        poseStack.translate(0.0F, offset, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        poseStack.mulPose(Axis.YP.rotationDegrees(tilt));
        poseStack.scale(scale, scale, scale);
        VertexConsumer buffer = buffers.getBuffer(TcRenderTypes.translucent(VORTEX));
        PoseStack.Pose pose = poseStack.last();
        buffer.addVertex(pose, -0.5F, 0.5F, 0.0F).setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, opacity).setLight(200);
        buffer.addVertex(pose, 0.5F, 0.5F, 0.0F).setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, opacity).setLight(200);
        buffer.addVertex(pose, 0.5F, -0.5F, 0.0F).setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, opacity).setLight(200);
        buffer.addVertex(pose, -0.5F, -0.5F, 0.0F).setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, opacity).setLight(200);
        poseStack.popPose();
    }
}
