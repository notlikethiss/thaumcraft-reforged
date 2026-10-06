package thaumcraft.entity.golem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

final class ClayLogic {
    private ClayLogic() {
    }

    static @Nullable List<ItemStack> missingItems(GolemWorker golem, boolean toggle) {
        GolemInventory inventory = golem.getInventory();
        if (toggle) {
            List<ItemStack> result = new ArrayList<>();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (!inventory.getItem(slot).isEmpty()) {
                    result.add(inventory.getItem(slot).copy());
                }
            }
            return result;
        }
        if (!golem.hasHomeInventory()) {
            return null;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack filter = inventory.getItem(slot);
            if (filter.isEmpty()) {
                continue;
            }
            int found = golem.homeCount(filter);
            int needed = inventory.getAmountNeeded(filter);
            if (found < needed) {
                int missing = filter.getCount() - found;
                return List.of(filter.copyWithCount(missing > 0 ? missing : needed - found));
            }
        }
        return null;
    }
}
