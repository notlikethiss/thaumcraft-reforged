package thaumcraft.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import javax.annotation.Nullable;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.crafting.ThaumcraftRecipes;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.item.wand.WandManager;

public abstract class MagicWorkbenchMenu extends AbstractContainerMenu {
    protected final MagicWorkbenchBlockEntity workbench;
    protected final Player player;
    private final int playerSlotsStart;

    protected MagicWorkbenchMenu(
        @Nullable MenuType<?> type,
        int containerId,
        Inventory inventory,
        MagicWorkbenchBlockEntity workbench,
        int resultX,
        int resultY,
        int wandX,
        int wandY,
        int gridX,
        int gridY,
        int gridStep,
        int inventoryY
    ) {
        super(type, containerId);
        this.workbench = workbench;
        this.player = inventory.player;
        addSlot(new WorkbenchResultSlot(this, workbench, MagicWorkbenchBlockEntity.RESULT_SLOT, resultX, resultY));
        addSlot(new WandSlot(workbench, MagicWorkbenchBlockEntity.WAND_SLOT, wandX, wandY));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(workbench, column + row * 3, gridX + column * gridStep, gridY + row * gridStep));
            }
        }
        playerSlotsStart = slots.size();
        InventorySlots.add(this::addSlot, inventory, 8, inventoryY);
        workbench.addListener(this);
        slotsChanged(workbench);
    }

    public MagicWorkbenchBlockEntity getWorkbench() {
        return workbench;
    }

    @Override
    public void slotsChanged(Container container) {
        if (player.level() instanceof ServerLevel level) {
            workbench.setItemSoftly(MagicWorkbenchBlockEntity.RESULT_SLOT, computeResult(level));
            broadcastChanges();
        }
    }

    protected abstract ItemStack computeResult(ServerLevel level);

    protected ItemStack vanillaResult(ServerLevel level) {
        CraftingInput input = craftingInput();
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        return recipe.map(holder -> holder.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    protected @Nullable WorkbenchRecipe findRecipe(WorkbenchRecipe.Kind kind) {
        return ThaumcraftRecipes.findMatching(kind, workbench::getGridItem, player);
    }

    protected boolean hasWandCharge(int cost) {
        ItemStack wand = workbench.getWand();
        return wand.getItem() instanceof CastingWandItem && WandManager.hasCharge(wand, player, cost);
    }

    protected CraftingInput craftingInput() {
        List<ItemStack> grid = new ArrayList<>();
        for (int slot = 0; slot < MagicWorkbenchBlockEntity.GRID_SIZE; slot++) {
            grid.add(workbench.getItem(slot));
        }
        return CraftingInput.of(3, 3, grid);
    }

    protected abstract void onCrafted(Player player, ItemStack result);

    void handleTake(Player taker, ItemStack result) {
        result.onCraftedBy(taker.level(), taker, result.getCount());
        onCrafted(taker, result);
        for (int slot = 0; slot < MagicWorkbenchBlockEntity.GRID_SIZE; slot++) {
            ItemStack stack = workbench.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack replacement = stack.getCraftingRemainingItem();
            workbench.removeItem(slot, 1);
            if (!replacement.isEmpty()) {
                if (workbench.getItem(slot).isEmpty()) {
                    workbench.setItem(slot, replacement);
                } else if (!taker.getInventory().add(replacement)) {
                    taker.drop(replacement, false);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerEnd = slots.size();
        int hotbarStart = playerEnd - 9;
        if (index == 0) {
            if (!moveItemStackTo(stack, playerSlotsStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index >= playerSlotsStart && index < hotbarStart) {
            if (stack.getItem() instanceof CastingWandItem) {
                if (!moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, hotbarStart, playerEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= hotbarStart) {
            if (stack.getItem() instanceof CastingWandItem && moveItemStackTo(stack, 1, 2, false)) {
                slot.setChanged();
            } else if (!moveItemStackTo(stack, playerSlotsStart, hotbarStart, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, playerSlotsStart, playerEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return workbench.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        workbench.removeListener(this);
    }
}
