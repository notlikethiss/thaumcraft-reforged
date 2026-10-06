package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.HungryChestModel;

public final class HungryChestRendering {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/chesthungry.png");

    private HungryChestRendering() {
    }

    public static void render(HungryChestModel model, PoseStack poseStack, MultiBufferSource buffers, int light, Direction facing, float openness) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.0F, 1.0F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        float rotation = switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        model.setOpenness(openness);
        model.renderToBuffer(poseStack, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
}
