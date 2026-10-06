package thaumcraft.menu;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class InventorySlots {
    private InventorySlots() {
    }

    public static void add(Consumer<Slot> adder, Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                adder.accept(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            adder.accept(new Slot(inventory, column, x + column * 18, y + 58));
        }
    }
}
