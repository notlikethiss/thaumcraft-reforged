package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import thaumcraft.blockentity.CrystalCapacitorBlockEntity;

public class CrystalCapacitorRenderer implements BlockEntityRenderer<CrystalCapacitorBlockEntity> {
    private final CrystalRendering.Models models;

    public CrystalCapacitorRenderer(BlockEntityRendererProvider.Context context) {
        models = CrystalRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(CrystalCapacitorBlockEntity capacitor, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BlockPos pos = capacitor.getBlockPos();
        CrystalRendering.renderCapacitor(models, poseStack, buffers, pos.getX(), pos.getY(), pos.getZ(), capacitor.getStoredVis());
    }
}
