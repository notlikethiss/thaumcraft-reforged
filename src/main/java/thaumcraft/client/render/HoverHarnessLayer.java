package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.ObjModel;
import thaumcraft.item.armor.Hover;
import thaumcraft.registry.ModItems;

public class HoverHarnessLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation BACK_TEXTURE = Thaumcraft.id("textures/model/hoverharness2.png");
    private static final ResourceLocation RING_TEXTURE = Thaumcraft.id("textures/item/lightningring.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int RING_FRAMES = 16;

    private static ObjModel back;

    public HoverHarnessLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(ModItems.HOVER_HARNESS.get())) {
            return;
        }
        if (back == null) {
            back = ObjModel.load(Thaumcraft.id("models/obj/hoverharness.obj"));
        }
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        poseStack.pushPose();
        poseStack.scale(0.1F, 0.1F, 0.1F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, 0.33F, -3.7F);
        back.render(poseStack.last(), bufferSource.getBuffer(RenderType.entityCutout(BACK_TEXTURE)), FULL_BRIGHT);
        poseStack.popPose();
        if (Hover.isHovering(chest)) {
            if (player.isCrouching()) {
                poseStack.translate(0.0F, 0.075F, -0.05F);
            }
            poseStack.translate(0.0F, 0.2F, 0.55F);
            int frame = (int) ageInTicks % RING_FRAMES;
            VertexConsumer ringBuffer = bufferSource.getBuffer(TcRenderTypes.additive(RING_TEXTURE));
            ring(ringBuffer, poseStack, frame, 2.5F, 1.0F, 1.0F, 1.0F);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            ring(ringBuffer, poseStack, frame, 1.5F, 1.0F, 0.5F, 1.0F);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void ring(VertexConsumer buffer, PoseStack poseStack, int frame, float size, float red, float green, float blue) {
        float v0 = (float) frame / RING_FRAMES;
        float v1 = (float) (frame + 1) / RING_FRAMES;
        float half = size / 2.0F;
        int r = (int) (red * 255.0F);
        int g = (int) (green * 255.0F);
        int b = (int) (blue * 255.0F);
        PoseStack.Pose pose = poseStack.last();
        vertex(buffer, pose, -half, half, 1.0F, v1, r, g, b);
        vertex(buffer, pose, half, half, 0.0F, v1, r, g, b);
        vertex(buffer, pose, half, -half, 0.0F, v0, r, g, b);
        vertex(buffer, pose, -half, -half, 1.0F, v0, r, g, b);
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
