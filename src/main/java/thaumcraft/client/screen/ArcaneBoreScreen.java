package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;
import thaumcraft.menu.ArcaneBoreMenu;

public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 141);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        ArcaneBoreBlockEntity bore = menu.getBore();
        drawWarning(graphics, bore.getItem(0), 26);
        drawWarning(graphics, bore.getItem(1), 74);
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + 112, topPos + 8);
        graphics.pose().scale(0.5F, 0.5F);
        graphics.text(font, Component.translatable("tc.thaumcraft.bore.width", 1 + (bore.getArea() + ArcaneBoreBlockEntity.MAX_RADIUS) * 2), 0, 0, 0xFFFFFFFF, true);
        graphics.text(font, Component.translatable("tc.thaumcraft.bore.speed", bore.getSpeed()), 0, 10, 0xFFFFFFFF, true);
        graphics.text(font, Component.translatable("tc.thaumcraft.bore.properties"), 0, 24, 0xFFFFFFFF, true);
        int line = 0;
        if (bore.hasElementalPickaxe()) {
            graphics.text(font, Component.translatable("tc.thaumcraft.bore.clusters"), 4, 34 + line, 0xFFC0C0C0, true);
            line += 9;
        }
        if (bore.getFortune() > 0) {
            graphics.text(font, Component.translatable("tc.thaumcraft.bore.fortune", bore.getFortune()), 4, 34 + line, 0xFFEEC64A, true);
            line += 9;
        }
        if (bore.hasSilkTouch()) {
            graphics.text(font, Component.translatable("tc.thaumcraft.bore.silk_touch"), 4, 34 + line, 0xFF8080FF, true);
        }
        graphics.pose().popMatrix();
    }

    private void drawWarning(GuiGraphicsExtractor graphics, ItemStack stack, int x) {
        if (!stack.isEmpty() && stack.getDamageValue() + 1 >= stack.getMaxDamage()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + x, topPos + 18, 184.0F, 0.0F, 16, 16, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }
}
