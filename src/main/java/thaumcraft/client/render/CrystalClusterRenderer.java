package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.blockentity.CrystalClusterBlockEntity;

public class CrystalClusterRenderer implements BlockEntityRenderer<CrystalClusterBlockEntity, CrystalClusterRenderer.State> {
    public static class State extends BlockEntityRenderState {
        int type;
        Direction facing = Direction.UP;
        long seed;
    }

    private final CrystalRendering.Models models;

    public CrystalClusterRenderer(BlockEntityRendererProvider.Context context) {
        models = CrystalRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        CrystalClusterBlockEntity crystal,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(crystal, state, partialTicks, cameraPosition, breakProgress);
        state.type = crystal.getBlockState().getBlock() instanceof CrystalClusterBlock block ? block.getType() : 0;
        state.facing = crystal.getBlockState().getValue(DirectionalBlock.FACING);
        BlockPos pos = crystal.getBlockPos();
        state.seed = state.type + pos.getX() + (long) pos.getY() * pos.getZ();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        CrystalRendering.submitCluster(models, poseStack, collector, state.type, state.facing, state.seed);
    }
}
