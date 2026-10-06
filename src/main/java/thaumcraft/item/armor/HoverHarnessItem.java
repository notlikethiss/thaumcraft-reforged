package thaumcraft.item.armor;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.item.ModMaterials;
import thaumcraft.menu.HoverHarnessMenu;

public class HoverHarnessItem extends ArmorItem {
    public HoverHarnessItem(Properties properties) {
        super(ModMaterials.HARNESS_ARMOR, ArmorItem.Type.CHESTPLATE, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;
            player.openMenu(
                new SimpleMenuProvider((id, inventory, opener) -> new HoverHarnessMenu(id, inventory, hand, slot), stack.getHoverName()),
                buffer -> buffer.writeVarInt(slot)
            );
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
