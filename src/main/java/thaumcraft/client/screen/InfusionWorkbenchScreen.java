package thaumcraft.client.screen;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.InfusionWorkbenchBlockEntity;
import thaumcraft.client.gui.AspectRenderer;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.menu.InfusionWorkbenchMenu;

public class InfusionWorkbenchScreen extends MagicWorkbenchScreen<InfusionWorkbenchMenu> {
    public InfusionWorkbenchScreen(InfusionWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Thaumcraft.id("textures/gui/gui_infusionworkbench.png"), 187);
    }

    private @Nullable WorkbenchRecipe currentRecipe() {
        WorkbenchRecipe recipe = findRecipe(WorkbenchRecipe.Kind.ARCANE);
        return recipe != null ? recipe : findRecipe(WorkbenchRecipe.Kind.INFUSION);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        WorkbenchRecipe recipe = currentRecipe();
        if (recipe == null || !(workbench().getWand().getItem() instanceof CastingWandItem castingWand)) {
            drawVisInfo(graphics, 140, 85, null, 0, false);
            return;
        }
        if (recipe.kind() == WorkbenchRecipe.Kind.INFUSION) {
            drawAspects(graphics, recipe.aspects());
        }
        int cost = discounted(recipe.cost());
        drawVisInfo(graphics, 140, 85, recipe.assemble(), cost, cost > castingWand.getMaxVis());
    }

    private void drawAspects(GuiGraphicsExtractor graphics, AspectList required) {
        AspectList found = ((InfusionWorkbenchBlockEntity) workbench()).getFoundTags();
        List<Aspect> aspects = required.getAspects();
        int ticks = minecraft.player == null ? 0 : minecraft.player.tickCount;
        for (int i = 0; i < aspects.size(); i++) {
            Aspect aspect = aspects.get(i);
            int amount = required.getAmount(aspect);
            float alpha = 1.0F;
            if (found.getAmount(aspect) < amount) {
                alpha = (float) Math.sin((ticks - i * 10) / 8.0F) * 0.125F + 0.25F;
            }
            AspectRenderer.drawTag(graphics, leftPos + aspectX(i, aspects.size()), topPos + 72, aspect, amount, 0, false, false, alpha);
        }
    }

    private static int aspectX(int index, int count) {
        return 24 + 16 * index + (5 - count) * 8;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        WorkbenchRecipe recipe = currentRecipe();
        if (recipe == null || recipe.kind() != WorkbenchRecipe.Kind.INFUSION) {
            return;
        }
        List<Aspect> aspects = recipe.aspects().getAspects();
        for (int i = 0; i < aspects.size(); i++) {
            if (isHovering(aspectX(i, aspects.size()), 72, 16, 16, mouseX, mouseY)) {
                Aspect aspect = aspects.get(i);
                graphics.setTooltipForNextFrame(font, List.of(aspect.getDisplayName(), aspect.getMeaning()), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected int resultSlotX() {
        return 132;
    }

    @Override
    protected int resultSlotY() {
        return 28;
    }
}
