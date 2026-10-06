package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.blockentity.BellowsBlockEntity;

public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity, BellowsRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float inflation;
    }

    private final BellowsRendering.Models models;

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
        models = BellowsRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        BellowsBlockEntity bellows,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(bellows, state, partialTicks, cameraPosition, breakProgress);
        state.facing = bellows.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        state.inflation = Mth.lerp(partialTicks, bellows.prevInflation, bellows.inflation);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        BellowsRendering.submit(models, poseStack, collector, state.lightCoords, state.facing, state.inflation);
    }
}
