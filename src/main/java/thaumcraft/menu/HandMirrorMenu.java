package thaumcraft.menu;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.HandMirrorItem;
import thaumcraft.registry.ModMenus;

public class HandMirrorMenu extends AbstractContainerMenu {
    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer input = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            HandMirrorMenu.this.slotsChanged(this);
        }
    };

    public HandMirrorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, InteractionHand.MAIN_HAND);
    }

    public HandMirrorMenu(int containerId, Inventory inventory, InteractionHand hand) {
        super(ModMenus.HAND_MIRROR.get(), containerId);
        this.player = inventory.player;
        this.hand = hand;
        addSlot(new Slot(input, 0, 80, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !(stack.getItem() instanceof HandMirrorItem);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        ItemStack stack = input.getItem(0);
        if (!player.level().isClientSide() && !stack.isEmpty() && HandMirrorItem.transport(player.getItemInHand(hand), stack, player, player.level())) {
            input.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || slot.getItem().getItem() instanceof HandMirrorItem) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, input);
    }
}
