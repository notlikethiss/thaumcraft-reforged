package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;

public class WorkbenchWandRenderer<T extends MagicWorkbenchBlockEntity> implements BlockEntityRenderer<T> {
    private final boolean floating;

    public WorkbenchWandRenderer(BlockEntityRendererProvider.Context context, boolean floating) {
        this.floating = floating;
    }

    @Override
    public void render(T workbench, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        ItemStack wand = workbench.getWand();
        if (wand.isEmpty()) {
            return;
        }
        float time = workbench.getLevel() == null ? 0.0F : workbench.getLevel().getGameTime() + partialTick;
        poseStack.pushPose();
        if (floating) {
            float bob = (float) Math.sin(time / 14.0F) * 0.03F + 0.03F;
            float weave = (float) Math.sin(time / 10.0F) * 0.5F + 0.5F;
            poseStack.translate(1.0F, 1.1F + bob, 1.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(85.0F + weave * 10.0F));
        } else {
            poseStack.translate(0.5F, 1.02F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }
        poseStack.scale(1.5F, 1.5F, 1.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
            wand, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, workbench.getLevel(), (int) workbench.getBlockPos().asLong()
        );
        poseStack.popPose();
    }
}
