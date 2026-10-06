package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.HoleBlockEntity;
import thaumcraft.registry.ModBlocks;

public class HoleRenderer implements BlockEntityRenderer<HoleBlockEntity> {
    public HoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HoleBlockEntity hole, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = hole.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = hole.getBlockPos();
        for (Direction direction : Direction.values()) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (neighbour.isSolidRender(level, pos.relative(direction)) && !neighbour.is(ModBlocks.HOLE.get())) {
                TunnelRendering.submitPlane(poseStack, bufferSource, direction, 0.001F, 0.0F);
            }
        }
    }
}
