package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.blockentity.InfusionWorkbenchBlockEntity;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.item.wand.WandManager;
import thaumcraft.registry.ModMenus;

public class InfusionWorkbenchMenu extends MagicWorkbenchMenu {
    public InfusionWorkbenchMenu(int containerId, Inventory inventory, MagicWorkbenchBlockEntity workbench) {
        super(ModMenus.INFUSION_WORKBENCH.get(), containerId, inventory, workbench, 132, 28, 132, 61, 36, 8, 20, 106);
    }

    public static InfusionWorkbenchMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        return new InfusionWorkbenchMenu(containerId, inventory, MenuHelper.workbench(inventory, buffer));
    }

    private InfusionWorkbenchBlockEntity infusion() {
        return (InfusionWorkbenchBlockEntity) workbench;
    }

    @Override
    protected ItemStack computeResult(ServerLevel level) {
        ItemStack result = vanillaResult(level);
        if (!result.isEmpty()) {
            infusion().clearParts();
            return result;
        }
        WorkbenchRecipe arcane = findRecipe(WorkbenchRecipe.Kind.ARCANE);
        if (arcane != null && hasWandCharge(arcane.cost())) {
            infusion().clearParts();
            return arcane.assemble();
        }
        WorkbenchRecipe recipe = findRecipe(WorkbenchRecipe.Kind.INFUSION);
        if (recipe == null) {
            infusion().clearParts();
            return ItemStack.EMPTY;
        }
        boolean sourcesMatch = infusion().doSourcesMatch(recipe.aspects());
        return hasWandCharge(recipe.cost()) && sourcesMatch ? recipe.assemble() : ItemStack.EMPTY;
    }

    @Override
    protected void onCrafted(Player player, ItemStack result) {
        int cost = 0;
        WorkbenchRecipe arcane = findRecipe(WorkbenchRecipe.Kind.ARCANE);
        if (arcane != null) {
            cost = arcane.cost();
        }
        if (cost == 0) {
            WorkbenchRecipe recipe = findRecipe(WorkbenchRecipe.Kind.INFUSION);
            if (recipe != null) {
                cost = recipe.cost();
                if (!player.level().isClientSide()) {
                    AspectList aspects = recipe.aspects();
                    for (Aspect aspect : aspects.getAspects()) {
                        AspectSource source = infusion().getLinkedSource(aspect);
                        if (source != null) {
                            source.takeFromSource(aspect, aspects.getAmount(aspect));
                        }
                    }
                }
            }
        }
        if (cost > 0) {
            WandManager.spendCharge(workbench.getWand(), player, cost);
        }
    }
}
