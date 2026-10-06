package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.blockentity.CrystalCapacitorBlockEntity;

public class CrystalCapacitorRenderer implements BlockEntityRenderer<CrystalCapacitorBlockEntity, CrystalCapacitorRenderer.State> {
    public static class State extends BlockEntityRenderState {
        BlockPos pos = BlockPos.ZERO;
        int storedVis;
    }

    private final CrystalRendering.Models models;

    public CrystalCapacitorRenderer(BlockEntityRendererProvider.Context context) {
        models = CrystalRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        CrystalCapacitorBlockEntity capacitor,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(capacitor, state, partialTicks, cameraPosition, breakProgress);
        state.pos = capacitor.getBlockPos();
        state.storedVis = capacitor.getStoredVis();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        CrystalRendering.submitCapacitor(models, poseStack, collector, state.pos.getX(), state.pos.getY(), state.pos.getZ(), state.storedVis);
    }
}
