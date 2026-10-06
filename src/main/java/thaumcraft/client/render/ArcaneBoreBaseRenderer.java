package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;

public class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity> {
    private final BoreRendering.Models models;

    public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context context) {
        models = BoreRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(ArcaneBoreBaseBlockEntity base, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BoreRendering.renderBase(models, poseStack, buffers, light, base.getBlockState().getValue(HorizontalDirectionalBlock.FACING));
    }
}
