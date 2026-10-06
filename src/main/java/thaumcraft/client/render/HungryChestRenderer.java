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
import javax.annotation.Nullable;
import thaumcraft.blockentity.HungryChestBlockEntity;
import thaumcraft.client.render.model.HungryChestModel;
import thaumcraft.client.render.model.TcModelLayers;

public class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity, HungryChestRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.SOUTH;
        float openness;
    }

    private final HungryChestModel model;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
        model = new HungryChestModel(context.entityModelSet().bakeLayer(TcModelLayers.HUNGRY_CHEST));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        HungryChestBlockEntity chest,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(chest, state, partialTicks, cameraPosition, breakProgress);
        state.facing = chest.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        state.openness = chest.getOpenness(partialTicks);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        HungryChestRendering.submit(model, poseStack, collector, state.lightCoords, state.facing, state.openness);
    }
}
