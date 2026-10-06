package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.gui.GuiDraw;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;
import thaumcraft.menu.ArcaneBoreMenu;

public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 141;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiDraw.blit(graphics, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        ArcaneBoreBlockEntity bore = menu.getBore();
        drawWarning(graphics, bore.getItem(0), 26);
        drawWarning(graphics, bore.getItem(1), 74);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 112, topPos + 8, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, Component.translatable("tc.thaumcraft.bore.width", 1 + (bore.getArea() + ArcaneBoreBlockEntity.MAX_RADIUS) * 2), 0, 0, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.translatable("tc.thaumcraft.bore.speed", bore.getSpeed()), 0, 10, 0xFFFFFFFF, true);
        graphics.drawString(font, Component.translatable("tc.thaumcraft.bore.properties"), 0, 24, 0xFFFFFFFF, true);
        int line = 0;
        if (bore.hasElementalPickaxe()) {
            graphics.drawString(font, Component.translatable("tc.thaumcraft.bore.clusters"), 4, 34 + line, 0xFFC0C0C0, true);
            line += 9;
        }
        if (bore.getFortune() > 0) {
            graphics.drawString(font, Component.translatable("tc.thaumcraft.bore.fortune", bore.getFortune()), 4, 34 + line, 0xFFEEC64A, true);
            line += 9;
        }
        if (bore.hasSilkTouch()) {
            graphics.drawString(font, Component.translatable("tc.thaumcraft.bore.silk_touch"), 4, 34 + line, 0xFF8080FF, true);
        }
        graphics.pose().popPose();
    }

    private void drawWarning(GuiGraphics graphics, ItemStack stack, int x) {
        if (!stack.isEmpty() && stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
            GuiDraw.blit(graphics, TEXTURE, leftPos + x, topPos + 18, 184.0F, 0.0F, 16, 16, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
