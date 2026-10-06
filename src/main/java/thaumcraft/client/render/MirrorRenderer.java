package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.block.mirror.MirrorBlock;
import thaumcraft.blockentity.MirrorBlockEntity;

public class MirrorRenderer implements BlockEntityRenderer<MirrorBlockEntity, MirrorRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        boolean linked;
    }

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        MirrorBlockEntity mirror,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(mirror, state, partialTicks, cameraPosition, breakProgress);
        state.facing = mirror.getBlockState().getValue(DirectionalBlock.FACING);
        state.linked = mirror.getBlockState().getValue(MirrorBlock.LINKED);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.linked) {
            TunnelRendering.submitPlane(poseStack, collector, state.facing.getOpposite(), 0.01F, 0.1875F);
        }
    }
}
