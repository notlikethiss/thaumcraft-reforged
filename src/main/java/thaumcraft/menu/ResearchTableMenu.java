package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModMenus;

public class ResearchTableMenu extends AbstractContainerMenu {
    public static final int BUTTON_RESEARCH = 0;
    public static final int BUTTON_TOGGLE_SAFE = 1;
    private static final int TABLE_SLOTS = 7;

    private final ResearchTableBlockEntity table;
    private final ContainerData data;

    public ResearchTableMenu(int containerId, Inventory inventory, ResearchTableBlockEntity table, ContainerData data) {
        super(ModMenus.RESEARCH_TABLE.get(), containerId);
        this.table = table;
        this.data = data;
        for (int a = 0; a < ResearchTableBlockEntity.INPUT_SLOTS; a++) {
            addSlot(new Slot(table, a, 15, 16 + 24 * a));
        }
        addSlot(new Slot(table, ResearchTableBlockEntity.NOTE_SLOT, 75, 65) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.RESEARCH_NOTES.get());
            }
        });
        addSlot(new Slot(table, ResearchTableBlockEntity.PAPER_SLOT, 75, 97) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.PAPER);
            }
        });
        addStandardInventorySlots(inventory, 40, 160);
        addDataSlots(data);
    }

    public static ResearchTableMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof ResearchTableBlockEntity table) {
            return new ResearchTableMenu(containerId, inventory, table, new SimpleContainerData(ResearchTableBlockEntity.DATA_COUNT));
        }
        throw new IllegalStateException("Research table block entity is missing");
    }

    public ResearchTableBlockEntity getTable() {
        return table;
    }

    public ContainerData getData() {
        return data;
    }

    public boolean canResearch() {
        return ResearchTableBlockEntity.canResearch(table, data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_RESEARCH) {
            if (canResearch()) {
                table.startResearch(player);
            }
            return true;
        }
        if (id == BUTTON_TOGGLE_SAFE) {
            table.toggleSafe();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < TABLE_SLOTS) {
            if (!moveItemStackTo(stack, TABLE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, TABLE_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return table.stillValid(player);
    }
}
