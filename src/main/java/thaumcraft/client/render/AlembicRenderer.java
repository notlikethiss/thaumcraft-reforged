package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.blockentity.AlembicBlockEntity;

public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity, AlembicRenderer.State> {
    public static class State extends BlockEntityRenderState {
        float level;
        int color;
    }

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        AlembicBlockEntity alembic,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(alembic, state, partialTicks, cameraPosition, breakProgress);
        state.level = alembic.getAmount() / (float) AlembicBlockEntity.MAX_AMOUNT * 0.5625F;
        state.color = ARGB.opaque(alembic.getAspect().color);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.level <= 0.0F) {
            return;
        }
        TextureAtlasSprite sprite = TcRenderUtil.blockSprite(Thaumcraft.id("block/animatedglow"));
        int light = Math.max(state.lightCoords & 0xFFFF, 0xC0) | (state.lightCoords & 0xFFFF0000);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
            TcRenderUtil.box(pose, buffer, sprite, 0.275F, 0.25F, 0.275F, 0.725F, 0.25F + state.level, 0.725F, state.color, light)
        );
    }
}
