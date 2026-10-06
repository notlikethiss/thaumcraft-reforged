package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public final class TcRenderUtil {
    private TcRenderUtil() {
    }

    public static TextureAtlasSprite blockSprite(ResourceLocation id) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(id);
    }

    public static void vertex(
        PoseStack.Pose pose,
        VertexConsumer buffer,
        float x,
        float y,
        float z,
        float u,
        float v,
        int color,
        int light,
        float nx,
        float ny,
        float nz
    ) {
        buffer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
    }

    public static void box(
        PoseStack.Pose pose,
        VertexConsumer buffer,
        TextureAtlasSprite sprite,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        int color,
        int light
    ) {
        float u1 = sprite.getU(x1);
        float u2 = sprite.getU(x2);
        float v1 = sprite.getV(1.0F - y2);
        float v2 = sprite.getV(1.0F - y1);
        float zu1 = sprite.getU(z1);
        float zu2 = sprite.getU(z2);
        float tv1 = sprite.getV(z1);
        float tv2 = sprite.getV(z2);
        vertex(pose, buffer, x1, y2, z1, u1, tv1, color, light, 0, 1, 0);
        vertex(pose, buffer, x1, y2, z2, u1, tv2, color, light, 0, 1, 0);
        vertex(pose, buffer, x2, y2, z2, u2, tv2, color, light, 0, 1, 0);
        vertex(pose, buffer, x2, y2, z1, u2, tv1, color, light, 0, 1, 0);
        vertex(pose, buffer, x1, y1, z1, u1, tv1, color, light, 0, -1, 0);
        vertex(pose, buffer, x2, y1, z1, u2, tv1, color, light, 0, -1, 0);
        vertex(pose, buffer, x2, y1, z2, u2, tv2, color, light, 0, -1, 0);
        vertex(pose, buffer, x1, y1, z2, u1, tv2, color, light, 0, -1, 0);
        vertex(pose, buffer, x1, y1, z1, u1, v2, color, light, 0, 0, -1);
        vertex(pose, buffer, x1, y2, z1, u1, v1, color, light, 0, 0, -1);
        vertex(pose, buffer, x2, y2, z1, u2, v1, color, light, 0, 0, -1);
        vertex(pose, buffer, x2, y1, z1, u2, v2, color, light, 0, 0, -1);
        vertex(pose, buffer, x2, y1, z2, u2, v2, color, light, 0, 0, 1);
        vertex(pose, buffer, x2, y2, z2, u2, v1, color, light, 0, 0, 1);
        vertex(pose, buffer, x1, y2, z2, u1, v1, color, light, 0, 0, 1);
        vertex(pose, buffer, x1, y1, z2, u1, v2, color, light, 0, 0, 1);
        vertex(pose, buffer, x1, y1, z2, zu2, v2, color, light, -1, 0, 0);
        vertex(pose, buffer, x1, y2, z2, zu2, v1, color, light, -1, 0, 0);
        vertex(pose, buffer, x1, y2, z1, zu1, v1, color, light, -1, 0, 0);
        vertex(pose, buffer, x1, y1, z1, zu1, v2, color, light, -1, 0, 0);
        vertex(pose, buffer, x2, y1, z1, zu1, v2, color, light, 1, 0, 0);
        vertex(pose, buffer, x2, y2, z1, zu1, v1, color, light, 1, 0, 0);
        vertex(pose, buffer, x2, y2, z2, zu2, v1, color, light, 1, 0, 0);
        vertex(pose, buffer, x2, y1, z2, zu2, v2, color, light, 1, 0, 0);
    }
}
