package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DirectionalBlock;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.blockentity.CrystalClusterBlockEntity;

public class CrystalClusterRenderer implements BlockEntityRenderer<CrystalClusterBlockEntity> {
    private final CrystalRendering.Models models;

    public CrystalClusterRenderer(BlockEntityRendererProvider.Context context) {
        models = CrystalRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(CrystalClusterBlockEntity crystal, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        int type = crystal.getBlockState().getBlock() instanceof CrystalClusterBlock block ? block.getType() : 0;
        BlockPos pos = crystal.getBlockPos();
        long seed = type + pos.getX() + (long) pos.getY() * pos.getZ();
        CrystalRendering.renderCluster(models, poseStack, buffers, type, crystal.getBlockState().getValue(DirectionalBlock.FACING), seed);
    }
}
