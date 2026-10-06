package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import thaumcraft.blockentity.BellowsBlockEntity;

public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity> {
    private final BellowsRendering.Models models;

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
        models = BellowsRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(BellowsBlockEntity bellows, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BellowsRendering.render(
            models,
            poseStack,
            buffers,
            light,
            bellows.getBlockState().getValue(HorizontalDirectionalBlock.FACING),
            Mth.lerp(partialTick, bellows.prevInflation, bellows.inflation)
        );
    }
}
