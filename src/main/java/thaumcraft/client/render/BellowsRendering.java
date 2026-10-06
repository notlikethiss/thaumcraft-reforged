package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;

public final class BellowsRendering {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/bellows.png");

    private BellowsRendering() {
    }

    public record Models(ModelPart bag, ModelPart topPlank, ModelPart bottomPlank, ModelPart middlePlank, ModelPart nozzle) {
        public static Models bake(EntityModelSet models) {
            ModelPart root = models.bakeLayer(TcModelLayers.BELLOWS);
            return new Models(root.getChild("bag"), root.getChild("top_plank"), root.getChild("bottom_plank"), root.getChild("middle_plank"), root.getChild("nozzle"));
        }
    }

    public static void render(Models models, PoseStack poseStack, MultiBufferSource buffers, int light, Direction facing, float inflation) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        float plankScale = 0.125F + inflation * 0.875F;
        poseStack.pushPose();
        poseStack.translate(0.5F, -0.5F, 0.5F);
        float rotation = switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0.0F, 1.0F, 0.0F);
        poseStack.pushPose();
        poseStack.scale(0.5F, (inflation + 0.1F) / 2.0F, 0.5F);
        models.bag().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        poseStack.translate(0.0F, -1.0F, 0.0F);
        poseStack.pushPose();
        poseStack.translate(0.0F, -plankScale / 2.0F + 0.5F, 0.0F);
        models.topPlank().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0F, plankScale / 2.0F - 0.5F, 0.0F);
        models.bottomPlank().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        models.middlePlank().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        models.nozzle().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
