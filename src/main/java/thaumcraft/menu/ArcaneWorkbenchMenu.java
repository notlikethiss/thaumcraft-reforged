package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.item.wand.WandManager;
import thaumcraft.registry.ModMenus;

public class ArcaneWorkbenchMenu extends MagicWorkbenchMenu {
    public ArcaneWorkbenchMenu(int containerId, Inventory inventory, MagicWorkbenchBlockEntity workbench) {
        super(ModMenus.ARCANE_WORKBENCH.get(), containerId, inventory, workbench, 124, 29, 124, 61, 30, 17, 18, 94);
    }

    public static ArcaneWorkbenchMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        return new ArcaneWorkbenchMenu(containerId, inventory, MenuHelper.workbench(inventory, buffer));
    }

    @Override
    protected ItemStack computeResult(ServerLevel level) {
        ItemStack result = vanillaResult(level);
        if (result.isEmpty()) {
            WorkbenchRecipe recipe = findRecipe(WorkbenchRecipe.Kind.ARCANE);
            if (recipe != null && hasWandCharge(recipe.cost())) {
                result = recipe.assemble();
            }
        }
        return result;
    }

    @Override
    protected void onCrafted(Player player, ItemStack result) {
        WorkbenchRecipe recipe = findRecipe(WorkbenchRecipe.Kind.ARCANE);
        if (recipe != null && recipe.cost() > 0) {
            WandManager.spendCharge(workbench.getWand(), player, recipe.cost());
        }
    }
}
