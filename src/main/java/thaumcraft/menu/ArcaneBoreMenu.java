package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;
import thaumcraft.item.wand.ExcavationWandItem;
import thaumcraft.registry.ModMenus;

public class ArcaneBoreMenu extends AbstractContainerMenu {
    private final ArcaneBoreBlockEntity bore;

    public ArcaneBoreMenu(int containerId, Inventory inventory, ArcaneBoreBlockEntity bore) {
        super(ModMenus.ARCANE_BORE.get(), containerId);
        this.bore = bore;
        addSlot(new Slot(bore, 0, 26, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ExcavationWandItem;
            }
        });
        addSlot(new Slot(bore, 1, 74, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemTags.PICKAXES);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 59 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 117));
        }
    }

    public static ArcaneBoreMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof ArcaneBoreBlockEntity bore) {
            return new ArcaneBoreMenu(containerId, inventory, bore);
        }
        throw new IllegalStateException("Arcane bore block entity is missing");
    }

    public ArcaneBoreBlockEntity getBore() {
        return bore;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index <= 1) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ExcavationWandItem) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ItemTags.PICKAXES)) {
            if (!moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return bore.stillValid(player);
    }
}
