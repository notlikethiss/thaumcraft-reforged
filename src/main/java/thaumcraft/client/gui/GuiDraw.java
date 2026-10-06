package thaumcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;

public final class GuiDraw {
    private GuiDraw() {
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int argb) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        tint(argb);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blitAdditive(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blitAdditive(graphics, texture, x, y, u, v, width, height, textureWidth, textureHeight, 0xFFFFFFFF);
    }

    public static void blitAdditive(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int argb) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        tint(argb);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    public static int whiteAlpha(float alpha) {
        return FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
    }

    public static void wrapped(GuiGraphics graphics, Font font, Component text, int x, int y, int width, int color, boolean shadow) {
        List<FormattedCharSequence> lines = font.split(text, width);
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(font, lines.get(index), x, y + index * font.lineHeight, color, shadow);
        }
    }

    private static void tint(int argb) {
        RenderSystem.setShaderColor(
            FastColor.ARGB32.red(argb) / 255.0F,
            FastColor.ARGB32.green(argb) / 255.0F,
            FastColor.ARGB32.blue(argb) / 255.0F,
            FastColor.ARGB32.alpha(argb) / 255.0F
        );
    }
}
