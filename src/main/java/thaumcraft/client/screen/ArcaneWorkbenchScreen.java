package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
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
