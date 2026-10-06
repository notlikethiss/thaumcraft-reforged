package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.menu.ArcaneWorkbenchMenu;

public class ArcaneWorkbenchScreen extends MagicWorkbenchScreen<ArcaneWorkbenchMenu> {
    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Thaumcraft.id("textures/gui/gui_arcaneworkbench.png"), 176);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        WorkbenchRecipe recipe = findRecipe(WorkbenchRecipe.Kind.ARCANE);
        ItemStack wand = workbench().getWand();
        if (recipe == null || !(wand.getItem() instanceof CastingWandItem castingWand)) {
            drawVisInfo(graphics, 132, 82, null, 0, false);
            return;
        }
        int cost = discounted(recipe.cost());
        drawVisInfo(graphics, 132, 82, recipe.assemble(), cost, cost > castingWand.getMaxVis());
    }

    @Override
    protected int resultSlotX() {
        return 124;
    }

    @Override
    protected int resultSlotY() {
        return 28;
    }
}
