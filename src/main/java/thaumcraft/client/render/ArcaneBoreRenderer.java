package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;

public class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity> {
    private final BoreRendering.Models models;

    public ArcaneBoreRenderer(BlockEntityRendererProvider.Context context) {
        models = BoreRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(ArcaneBoreBlockEntity bore, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BoreRendering.BoreState state = new BoreRendering.BoreState(
            bore.rotX - bore.vRadX + partialTick * bore.speedX,
            bore.rotZ - bore.vRadZ + partialTick * bore.speedZ,
            bore.getBaseDirection() == Direction.UP,
            bore.hasWand(),
            bore.topRotation,
            CrystalRendering.ticks() % 45 + partialTick
        );
        BoreRendering.renderBore(models, poseStack, buffers, light, state);
    }
}
