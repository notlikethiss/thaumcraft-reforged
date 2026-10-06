package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.blockentity.CrystalCoreBlockEntity;

public class CrystalCoreRenderer implements BlockEntityRenderer<CrystalCoreBlockEntity, CrystalCoreRenderer.State> {
    public static class State extends BlockEntityRenderState {
        long seed;
        float speed;
        float spin;
    }

    private final CrystalRendering.Models models;

    public CrystalCoreRenderer(BlockEntityRendererProvider.Context context) {
        models = CrystalRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        CrystalCoreBlockEntity core,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(core, state, partialTicks, cameraPosition, breakProgress);
        BlockPos pos = core.getBlockPos();
        state.seed = pos.getX() + (long) pos.getY() * pos.getZ();
        state.speed = core.getSpeed();
        state.spin = (core.isActive() ? partialTicks * core.getSpeed() : 0.0F) + core.getRotation();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        CrystalRendering.submitCore(models, poseStack, collector, state.seed, state.speed, state.spin);
    }

    @Override
    public AABB getRenderBoundingBox(CrystalCoreBlockEntity core) {
        return new AABB(core.getBlockPos()).inflate(1.0, 2.0, 1.0);
    }
}
