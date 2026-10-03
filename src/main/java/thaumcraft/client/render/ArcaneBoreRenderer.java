package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;

public class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity, ArcaneBoreRenderer.State> {
    public static class State extends BlockEntityRenderState {
        BoreRendering.BoreState bore = new BoreRendering.BoreState(0.0F, 0.0F, false, false, 0.0F, 0.0F);
    }

    private final BoreRendering.Models models;

    public ArcaneBoreRenderer(BlockEntityRendererProvider.Context context) {
        models = BoreRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        ArcaneBoreBlockEntity bore,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(bore, state, partialTicks, cameraPosition, breakProgress);
        state.bore = new BoreRendering.BoreState(
            bore.rotX - bore.vRadX + partialTicks * bore.speedX,
            bore.rotZ - bore.vRadZ + partialTicks * bore.speedZ,
            bore.getBaseDirection() == Direction.UP,
            bore.hasWand(),
            bore.topRotation,
            CrystalRendering.ticks() % 45 + partialTicks
        );
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        BoreRendering.submitBore(models, poseStack, collector, state.lightCoords, state.bore);
    }
}
