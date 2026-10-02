package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.CrucibleBlockEntity;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.State> {
    private static final Identifier WATER = Identifier.withDefaultNamespace("block/water_still");

    public static class State extends BlockEntityRenderState {
        boolean liquid;
        float height;
        int color;
    }

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        CrucibleBlockEntity crucible,
        State state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(crucible, state, partialTicks, cameraPosition, breakProgress);
        state.liquid = crucible.hasLiquid();
        state.height = crucible.getFluidHeight();
        float recolor = crucible.getTags().visSize() / (float) CrucibleBlockEntity.MAX_TAGS;
        if (recolor > 0.0F) {
            recolor = 0.5F + recolor / 2.0F;
        }
        int water = crucible.getLevel() instanceof BlockAndTintGetter tintGetter
            ? BiomeColors.getAverageWaterColor(tintGetter, crucible.getBlockPos())
            : 0x3F76E4;
        float red = Math.max(0.0F, 1.0F - recolor / 3.0F) * ARGB.red(water) / 255.0F;
        float green = Math.max(0.0F, 1.0F - recolor) * ARGB.green(water) / 255.0F;
        float blue = Math.max(0.0F, 1.0F - recolor / 2.0F) * ARGB.blue(water) / 255.0F;
        state.color = ARGB.colorFromFloat(1.0F, red, green, blue);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.liquid) {
            return;
        }
        TextureAtlasSprite sprite = TcRenderUtil.blockSprite(WATER);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            float y = state.height;
            TcRenderUtil.vertex(pose, buffer, 0.0F, y, 0.0F, sprite.getU0(), sprite.getV0(), state.color, state.lightCoords, 0, 1, 0);
            TcRenderUtil.vertex(pose, buffer, 0.0F, y, 1.0F, sprite.getU0(), sprite.getV1(), state.color, state.lightCoords, 0, 1, 0);
            TcRenderUtil.vertex(pose, buffer, 1.0F, y, 1.0F, sprite.getU1(), sprite.getV1(), state.color, state.lightCoords, 0, 1, 0);
            TcRenderUtil.vertex(pose, buffer, 1.0F, y, 0.0F, sprite.getU1(), sprite.getV0(), state.color, state.lightCoords, 0, 1, 0);
        });
    }
}
