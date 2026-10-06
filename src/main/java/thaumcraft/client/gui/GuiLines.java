package thaumcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class GuiLines {
    private static final Random RANDOM = new Random();

    private GuiLines() {
    }

    public static void unstable(GuiGraphics graphics, int x, int y, int x2, int y2, float instability, float opacity) {
        if (instability <= 0.01F) {
            line(graphics, x, y, x2, y2, opacity);
            return;
        }
        double distance = Mth.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2)) * instability;
        double dx = (x2 - x) / distance;
        double dy = (y2 - y) / distance;
        int xr = 0;
        int yr = 0;
        int steps = (int) Math.floor(distance - 1.0);
        int a;
        for (a = 0; a < steps; a++) {
            int xrn = RANDOM.nextInt(2) - RANDOM.nextInt(2);
            int yrn = RANDOM.nextInt(2) - RANDOM.nextInt(2);
            line(graphics, (int) (x + dx * a) + xr, (int) (y + dy * a) + yr, (int) (x + dx * (a + 1)) + xrn, (int) (y + dy * (a + 1)) + yrn, opacity);
            xr = xrn;
            yr = yrn;
        }
        line(graphics, (int) (x + dx * a) + xr, (int) (y + dy * a) + yr, x2, y2, opacity);
    }

    public static void line(GuiGraphics graphics, int x, int y, int x2, int y2, float opacity) {
        Minecraft minecraft = Minecraft.getInstance();
        int count = minecraft.player == null ? 0 : minecraft.player.tickCount;
        float red = Mth.sin((count + x2) / 10.0F) * 0.15F + 0.15F;
        float green = Mth.sin((count + x + y2) / 11.0F) * 0.15F + 0.15F;
        float blue = Mth.sin((count + y) / 12.0F) * 0.15F + 0.15F;
        int color = FastColor.ARGB32.colorFromFloat(opacity, red, green, blue);
        int dx = Math.abs(x2 - x);
        int dy = -Math.abs(y2 - y);
        int sx = x < x2 ? 1 : -1;
        int sy = y < y2 ? 1 : -1;
        int error = dx + dy;
        int cx = x;
        int cy = y;
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        while (true) {
            buffer.addVertex(matrix, cx, cy, 0.0F).setColor(color);
            buffer.addVertex(matrix, cx, cy + 1, 0.0F).setColor(color);
            buffer.addVertex(matrix, cx + 1, cy + 1, 0.0F).setColor(color);
            buffer.addVertex(matrix, cx + 1, cy, 0.0F).setColor(color);
            if (cx == x2 && cy == y2) {
                break;
            }
            int doubled = 2 * error;
            if (doubled >= dy) {
                error += dy;
                cx += sx;
            }
            if (doubled <= dx) {
                error += dx;
                cy += sy;
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.defaultBlendFunc();
    }
}
