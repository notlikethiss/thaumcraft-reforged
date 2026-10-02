package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;

final class MenuHelper {
    private MenuHelper() {
    }

    static MagicWorkbenchBlockEntity workbench(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof MagicWorkbenchBlockEntity workbench) {
            return workbench;
        }
        throw new IllegalStateException("Workbench block entity is missing");
    }
}
