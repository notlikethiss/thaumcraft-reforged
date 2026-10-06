package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.blockentity.AlembicBlockEntity;

public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlembicBlockEntity alembic, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int lightCoords, int overlay) {
        float level = alembic.getAmount() / (float) AlembicBlockEntity.MAX_AMOUNT * 0.5625F;
        if (level <= 0.0F) {
            return;
        }
        int color = FastColor.ARGB32.opaque(alembic.getAspect().color);
        TextureAtlasSprite sprite = TcRenderUtil.blockSprite(Thaumcraft.id("block/animatedglow"));
        int light = Math.max(lightCoords & 0xFFFF, 0xC0) | (lightCoords & 0xFFFF0000);
        TcRenderUtil.box(poseStack.last(), buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), sprite,
            0.275F, 0.25F, 0.275F, 0.725F, 0.25F + level, 0.725F, color, light);
    }
}
