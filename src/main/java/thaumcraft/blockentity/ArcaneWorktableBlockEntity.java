package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneWorktableBlockEntity extends MagicWorkbenchBlockEntity implements MenuProvider {
    public ArcaneWorktableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_WORKTABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.arcane_worktable");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArcaneWorkbenchMenu(containerId, inventory, this);
    }
}
