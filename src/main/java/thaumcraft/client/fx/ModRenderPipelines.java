package thaumcraft.client.fx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

public final class ModRenderPipelines {
    private ModRenderPipelines() {
    }

    public static void beginGuiAdditive(GuiGraphics graphics) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }

    public static void endGuiAdditive(GuiGraphics graphics) {
        graphics.flush();
        RenderSystem.defaultBlendFunc();
    }
}
