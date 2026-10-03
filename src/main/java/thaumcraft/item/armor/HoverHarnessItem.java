package thaumcraft.item.armor;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.menu.HoverHarnessMenu;

public class HoverHarnessItem extends Item {
    public HoverHarnessItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
            player.openMenu(
                new SimpleMenuProvider((id, inventory, opener) -> new HoverHarnessMenu(id, inventory, hand, slot), stack.getHoverName()),
                buffer -> buffer.writeVarInt(slot)
            );
        }
        return InteractionResult.SUCCESS;
    }
}
