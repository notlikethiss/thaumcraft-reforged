package thaumcraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public final class AspectRenderer {
    public static final ResourceLocation TAGS = Thaumcraft.id("textures/misc/ss_tags_1.png");
    public static final ResourceLocation TAGS_BW = Thaumcraft.id("textures/misc/ss_tags_2.png");
    public static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");

    private AspectRenderer() {
    }

    public static void drawTag(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect, int amount, boolean background, boolean blackAndWhite) {
        drawTag(graphics, x, y, aspect, amount, 0, background, blackAndWhite, 1.0F);
    }

    public static void drawTag(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        Aspect aspect,
        int amount,
        int bonus,
        boolean background,
        boolean blackAndWhite,
        float opacity
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5F, 0.5F);
        int sx = x * 2;
        int sy = y * 2;
        if (background) {
            graphics.fill(sx - 2, sy - 2, sx + 34, sy + 34, 0xAA000000);
        }
        int u = aspect.id % 8 * 32;
        int v = aspect.id / 8 * 32;
        float alpha = blackAndWhite ? Math.min(opacity, 0.8F) : opacity;
        int color = FastColor.ARGB32.color(Math.round(alpha * 255), aspect.color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, blackAndWhite ? TAGS_BW : TAGS, sx, sy, u, v, 32, 32, 256, 256, color);
        if (amount > 1) {
            drawOutlined(graphics, font, String.valueOf(amount), 33 - font.width(String.valueOf(amount)) + sx, 33 - font.lineHeight + sy);
        }
        if (bonus > 0 && minecraft.player != null) {
            int age = minecraft.player.tickCount;
            int pu = 32 * (age % 16);
            int pv = 32 * (age % 32 / 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, PARTICLES, sx - 8, sy - 8, pu, 96 + pv, 32, 32, 256, 256);
            String text = String.valueOf(bonus);
            drawOutlined(graphics, font, text, 8 - font.width(text) / 2 + sx, 15 - font.lineHeight + sy);
        }
        graphics.pose().popMatrix();
    }

    private static void drawOutlined(GuiGraphicsExtractor graphics, Font font, String text, int x, int y) {
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
                    graphics.text(font, text, x + a, y + b, 0xFF000000, false);
                }
            }
        }
        graphics.text(font, text, x, y, 0xFFFFFFFF, false);
    }
}
