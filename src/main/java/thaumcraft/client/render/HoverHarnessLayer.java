package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.ObjModel;
import thaumcraft.item.armor.Hover;
import thaumcraft.registry.ModItems;

public class HoverHarnessLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final ResourceLocation BACK_TEXTURE = Thaumcraft.id("textures/model/hoverharness2.png");
    private static final ResourceLocation RING_TEXTURE = Thaumcraft.id("textures/item/lightningring.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int RING_FRAMES = 16;

    private static ObjModel back;

    public HoverHarnessLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        ItemStack chest = state.chestEquipment;
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
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(BACK_TEXTURE), (pose, buffer) -> back.render(pose, buffer, FULL_BRIGHT));
        poseStack.popPose();
        if (Hover.isHovering(chest)) {
            if (state.isCrouching) {
                poseStack.translate(0.0F, 0.075F, -0.05F);
            }
            poseStack.translate(0.0F, 0.2F, 0.55F);
            int frame = (int) state.ageInTicks % RING_FRAMES;
            RenderType ringType = TcRenderTypes.additive(RING_TEXTURE);
            ring(collector, poseStack, ringType, frame, 2.5F, 1.0F, 1.0F, 1.0F);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            ring(collector, poseStack, ringType, frame, 1.5F, 1.0F, 0.5F, 1.0F);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void ring(SubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, int frame, float size, float red, float green, float blue) {
        float v0 = (float) frame / RING_FRAMES;
        float v1 = (float) (frame + 1) / RING_FRAMES;
        float half = size / 2.0F;
        int r = (int) (red * 255.0F);
        int g = (int) (green * 255.0F);
        int b = (int) (blue * 255.0F);
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            buffer.addVertex(pose, -half, half, 0.0F).setUv(1.0F, v1).setColor(r, g, b, 255).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, half, half, 0.0F).setUv(0.0F, v1).setColor(r, g, b, 255).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, half, -half, 0.0F).setUv(0.0F, v0).setColor(r, g, b, 255).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, -half, -half, 0.0F).setUv(1.0F, v0).setColor(r, g, b, 255).setLight(FULL_BRIGHT);
        });
    }
}
