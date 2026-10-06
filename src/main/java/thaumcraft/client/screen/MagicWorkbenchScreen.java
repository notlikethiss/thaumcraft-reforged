package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
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
        super(menu, inventory, title, 176, imageHeight);
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    protected void drawVisInfo(GuiGraphicsExtractor graphics, int centerX, int warningY, @Nullable ItemStack result, int cost, boolean tooWeak) {
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
            graphics.fakeItem(result, leftPos + resultSlotX(), topPos + resultSlotY());
            graphics.itemDecorations(font, result, leftPos + resultSlotX(), topPos + resultSlotY());
            Component warning = Component.translatable(tooWeak ? "tc.thaumcraft.wand_too_weak" : "tc.thaumcraft.insufficient_charge");
            drawSmall(graphics, warning, centerX, warningY, 0, WARNING_COLOR, false);
        }
        if (cost > 0) {
            drawSmall(graphics, Component.translatable("tc.thaumcraft.vis_amount", cost), centerX, 81, -64, COST_COLOR, true);
        }
    }

    private void drawSmall(GuiGraphicsExtractor graphics, Component text, int centerX, int anchorY, int offsetY, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + centerX, topPos + anchorY);
        graphics.pose().scale(0.5F, 0.5F);
        graphics.text(font, text, -font.width(text) / 2, offsetY, color, shadow);
        graphics.pose().popMatrix();
    }

    protected abstract int resultSlotX();

    protected abstract int resultSlotY();
}
