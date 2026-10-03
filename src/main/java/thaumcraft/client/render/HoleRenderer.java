package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.HoleBlockEntity;
import thaumcraft.registry.ModBlocks;

public class HoleRenderer implements BlockEntityRenderer<HoleBlockEntity, HoleRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final Set<Direction> walls = EnumSet.noneOf(Direction.class);
    }

    public HoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        HoleBlockEntity hole,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(hole, state, partialTicks, cameraPosition, breakProgress);
        state.walls.clear();
        Level level = hole.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = hole.getBlockPos();
        for (Direction direction : Direction.values()) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (neighbour.isSolidRender() && !neighbour.is(ModBlocks.HOLE.get())) {
                state.walls.add(direction);
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Direction wall : state.walls) {
            TunnelRendering.submitPlane(poseStack, collector, wall, 0.001F, 0.0F);
        }
    }
}
