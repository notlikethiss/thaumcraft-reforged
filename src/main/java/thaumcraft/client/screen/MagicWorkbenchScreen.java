package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.client.gui.GuiDraw;
import thaumcraft.crafting.ThaumcraftRecipes;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.item.wand.WandManager;
import thaumcraft.menu.MagicWorkbenchMenu;

public abstract class MagicWorkbenchScreen<T extends MagicWorkbenchMenu> extends AbstractContainerScreen<T> {
    private static final int WARNING_COLOR = 0xFFEE6E6E;
    private static final int COST_COLOR = 0xFFEEEEEE;
    private static final int CHARGE_COLOR = 0xFFFFFFFF;

    private final ResourceLocation texture;

    protected MagicWorkbenchScreen(T menu, Inventory inventory, Component title, ResourceLocation texture, int imageHeight) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = imageHeight;
        this.texture = texture;
    }

    protected MagicWorkbenchBlockEntity workbench() {
        return menu.getWorkbench();
    }

    protected @Nullable WorkbenchRecipe findRecipe(WorkbenchRecipe.Kind kind) {
        return ThaumcraftRecipes.findMatching(kind, workbench()::getGridItem, minecraft.player);
    }

    protected int discounted(int cost) {
        return WandManager.getDiscountedCost(minecraft.player, cost);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiDraw.blit(graphics, texture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    protected void drawVisInfo(GuiGraphics graphics, int centerX, int warningY, @Nullable ItemStack result, int cost, boolean tooWeak) {
        ItemStack wand = workbench().getWand();
        if (!(wand.getItem() instanceof CastingWandItem)) {
            return;
        }
        int charge = WandManager.getCharge(wand);
        if (charge > 0) {
            drawSmall(graphics, Component.translatable("tc.thaumcraft.vis_amount", charge), centerX, 85, -16, CHARGE_COLOR, true);
        }
        if (result == null) {
            return;
        }
        if (charge < cost) {
            graphics.renderFakeItem(result, leftPos + resultSlotX(), topPos + resultSlotY());
            graphics.renderItemDecorations(font, result, leftPos + resultSlotX(), topPos + resultSlotY());
            Component warning = Component.translatable(tooWeak ? "tc.thaumcraft.wand_too_weak" : "tc.thaumcraft.insufficient_charge");
            drawSmall(graphics, warning, centerX, warningY, 0, WARNING_COLOR, false);
        }
        if (cost > 0) {
            drawSmall(graphics, Component.translatable("tc.thaumcraft.vis_amount", cost), centerX, 81, -64, COST_COLOR, true);
        }
    }

    private void drawSmall(GuiGraphics graphics, Component text, int centerX, int anchorY, int offsetY, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + centerX, topPos + anchorY, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, text, -font.width(text) / 2, offsetY, color, shadow);
        graphics.pose().popPose();
    }

    protected abstract int resultSlotX();

    protected abstract int resultSlotY();
}
