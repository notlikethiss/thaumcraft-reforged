package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;

public class WorkbenchWandRenderer<T extends MagicWorkbenchBlockEntity> implements BlockEntityRenderer<T, WorkbenchWandRenderer.State> {
    private final ItemModelResolver itemModelResolver;
    private final boolean floating;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState wand = new ItemStackRenderState();
        float time;
    }

    public WorkbenchWandRenderer(BlockEntityRendererProvider.Context context, boolean floating) {
        this.itemModelResolver = context.itemModelResolver();
        this.floating = floating;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T workbench, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(workbench, state, partialTicks, cameraPosition, breakProgress);
        itemModelResolver.updateForTopItem(state.wand, workbench.getWand(), ItemDisplayContext.FIXED, workbench.getLevel(), null, (int) workbench.getBlockPos().asLong());
        state.time = workbench.getLevel() == null ? 0.0F : workbench.getLevel().getGameTime() + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.wand.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (floating) {
            float bob = (float) Math.sin(state.time / 14.0F) * 0.03F + 0.03F;
            float weave = (float) Math.sin(state.time / 10.0F) * 0.5F + 0.5F;
            poseStack.translate(1.0F, 1.1F + bob, 1.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(85.0F + weave * 10.0F));
        } else {
            poseStack.translate(0.5F, 1.02F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }
        poseStack.scale(1.5F, 1.5F, 1.5F);
        state.wand.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
