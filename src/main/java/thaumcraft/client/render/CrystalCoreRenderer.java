package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import thaumcraft.blockentity.CrystalCoreBlockEntity;

public class CrystalCoreRenderer implements BlockEntityRenderer<CrystalCoreBlockEntity> {
    private final CrystalRendering.Models models;

    public CrystalCoreRenderer(BlockEntityRendererProvider.Context context) {
        models = CrystalRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(CrystalCoreBlockEntity core, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BlockPos pos = core.getBlockPos();
        long seed = pos.getX() + (long) pos.getY() * pos.getZ();
        float spin = (core.isActive() ? partialTick * core.getSpeed() : 0.0F) + core.getRotation();
        CrystalRendering.renderCore(models, poseStack, buffers, seed, core.getSpeed(), spin);
    }

    @Override
    public AABB getRenderBoundingBox(CrystalCoreBlockEntity core) {
        return new AABB(core.getBlockPos()).inflate(1.0, 2.0, 1.0);
    }
}
