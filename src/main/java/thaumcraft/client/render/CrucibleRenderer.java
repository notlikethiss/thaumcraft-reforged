package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import thaumcraft.blockentity.CrucibleBlockEntity;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("block/water_still");

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrucibleBlockEntity crucible, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (!crucible.hasLiquid()) {
            return;
        }
        float y = crucible.getFluidHeight();
        float recolor = crucible.getTags().visSize() / (float) CrucibleBlockEntity.MAX_TAGS;
        if (recolor > 0.0F) {
            recolor = 0.5F + recolor / 2.0F;
        }
        int water = crucible.getLevel() instanceof BlockAndTintGetter tintGetter
            ? BiomeColors.getAverageWaterColor(tintGetter, crucible.getBlockPos())
            : 0x3F76E4;
        float red = Math.max(0.0F, 1.0F - recolor / 3.0F) * FastColor.ARGB32.red(water) / 255.0F;
        float green = Math.max(0.0F, 1.0F - recolor) * FastColor.ARGB32.green(water) / 255.0F;
        float blue = Math.max(0.0F, 1.0F - recolor / 2.0F) * FastColor.ARGB32.blue(water) / 255.0F;
        int color = FastColor.ARGB32.color(255, (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
        TextureAtlasSprite sprite = TcRenderUtil.blockSprite(WATER);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        TcRenderUtil.vertex(pose, buffer, 0.0F, y, 0.0F, sprite.getU0(), sprite.getV0(), color, light, 0, 1, 0);
        TcRenderUtil.vertex(pose, buffer, 0.0F, y, 1.0F, sprite.getU0(), sprite.getV1(), color, light, 0, 1, 0);
        TcRenderUtil.vertex(pose, buffer, 1.0F, y, 1.0F, sprite.getU1(), sprite.getV1(), color, light, 0, 1, 0);
        TcRenderUtil.vertex(pose, buffer, 1.0F, y, 0.0F, sprite.getU1(), sprite.getV0(), color, light, 0, 1, 0);
    }
}
