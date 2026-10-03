package thaumcraft.item.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ElementalToolEvents {
    private ElementalToolEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onBreakBlock(BreakBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        Level level = player.level();
        boolean handled = false;
        if (stack.getItem() instanceof ElementalAxeItem) {
            handled = ElementalAxeItem.onBreakBlock(level, player, stack, event.getPos(), event.getState());
        } else if (stack.getItem() instanceof ElementalPickaxeItem) {
            handled = ElementalPickaxeItem.onBreakBlock(level, player, stack, event.getPos(), event.getState());
        }
        if (handled) {
            event.setCanceled(true);
        }
    }
}
