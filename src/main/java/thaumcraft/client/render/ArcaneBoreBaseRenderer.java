package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;

public class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity, ArcaneBoreBaseRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
    }

    private final BoreRendering.Models models;

    public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context context) {
        models = BoreRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        ArcaneBoreBaseBlockEntity base,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(base, state, partialTicks, cameraPosition, breakProgress);
        state.facing = base.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        BoreRendering.submitBase(models, poseStack, collector, state.lightCoords, state.facing);
    }
}
