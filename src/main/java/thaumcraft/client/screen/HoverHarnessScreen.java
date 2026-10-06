package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.client.gui.GuiDraw;
import thaumcraft.menu.HoverHarnessMenu;

public class HoverHarnessScreen extends AbstractContainerScreen<HoverHarnessMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/guihoverharness.png");

    public HoverHarnessScreen(HoverHarnessMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiDraw.blit(graphics, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        if (menu.getHotbarSlot() >= 0) {
            GuiDraw.blit(graphics, TEXTURE, leftPos + 8 + menu.getHotbarSlot() * 18, topPos + 142, 240.0F, 0.0F, 16, 16, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
