package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import thaumcraft.blockentity.HungryChestBlockEntity;
import thaumcraft.client.render.model.HungryChestModel;
import thaumcraft.client.render.model.TcModelLayers;

public class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity> {
    private final HungryChestModel model;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
        model = new HungryChestModel(context.bakeLayer(TcModelLayers.HUNGRY_CHEST));
    }

    @Override
    public void render(HungryChestBlockEntity chest, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        HungryChestRendering.render(model, poseStack, buffers, light, chest.getBlockState().getValue(HorizontalDirectionalBlock.FACING), chest.getOpenness(partialTick));
    }
}
