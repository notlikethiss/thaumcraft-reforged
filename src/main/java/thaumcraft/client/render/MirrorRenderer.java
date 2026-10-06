package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import thaumcraft.block.mirror.MirrorBlock;
import thaumcraft.blockentity.MirrorBlockEntity;

public class MirrorRenderer implements BlockEntityRenderer<MirrorBlockEntity> {
    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MirrorBlockEntity mirror, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (!mirror.getBlockState().getValue(MirrorBlock.LINKED)) {
            return;
        }
        Direction side = mirror.getBlockState().getValue(DirectionalBlock.FACING).getOpposite();
        TunnelRendering.submitPlane(poseStack, buffers, side, 0.01F, 0.1875F);
    }
}
