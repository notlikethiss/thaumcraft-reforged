package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.armor.Hover;
import thaumcraft.registry.ModMenus;

public class HoverHarnessMenu extends AbstractContainerMenu {
    private final Player player;
    private final InteractionHand hand;
    private final ItemStack armor;
    private final int hotbarSlot;
    private final int blockSlot;
    private final SimpleContainer input = new SimpleContainer(1);

    public HoverHarnessMenu(int containerId, Inventory inventory, InteractionHand hand, int hotbarSlot) {
        super(ModMenus.HOVER_HARNESS.get(), containerId);
        this.player = inventory.player;
        this.hand = hand;
        this.hotbarSlot = hotbarSlot;
        this.blockSlot = hotbarSlot < 0 ? -1 : hotbarSlot + 28;
        this.armor = player.getItemInHand(hand);
        addSlot(new Slot(input, 0, 80, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Hover.isFuel(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            int index = column;
            addSlot(new Slot(inventory, column, 8 + column * 18, 142) {
                @Override
                public boolean mayPickup(Player picker) {
                    return index != hotbarSlot && super.mayPickup(picker);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return index != hotbarSlot && super.mayPlace(stack);
                }
            });
        }
        if (!player.level().isClientSide()) {
            input.setItem(0, Hover.getJar(armor));
        }
    }

    public static HoverHarnessMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        int slot = buffer.readVarInt();
        return new HoverHarnessMenu(containerId, inventory, slot < 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, slot);
    }

    public int getHotbarSlot() {
        return hotbarSlot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == blockSlot) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!Hover.isFuel(stack) || !moveItemStackTo(stack, 0, 1, false)) {
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
        return player.level().isClientSide() || player.getItemInHand(hand) == armor;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            ItemStack jar = input.removeItemNoUpdate(0);
            if (player.getItemInHand(hand) == armor) {
                Hover.setJar(armor, jar);
            } else if (!jar.isEmpty()) {
                player.getInventory().placeItemBackInInventory(jar, Prediction.SERVER_ONLY);
            }
        }
    }
}
