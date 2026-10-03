package thaumcraft.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraManager;
import thaumcraft.lib.MiningUtils;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModTags;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class EquipmentEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private EquipmentEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCreative()) {
            return;
        }
        for (int slot = 0; slot < 9; slot++) {
            repair(player, player.getInventory().getItem(slot));
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            repair(player, player.getItemBySlot(slot));
        }
    }

    private static void repair(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || stack.getDamageValue() <= 0 || !stack.is(ModTags.VIS_REPAIRABLE)) {
            return;
        }
        int repair = MiningUtils.enchantmentLevel(player.level(), stack, ModEnchantments.REPAIR);
        if (repair <= 0) {
            return;
        }
        int modifier = 0;
        if (repair > 2) {
            modifier = Math.min(15, (repair - 2) * 2);
            repair = 2;
        }
        if (player.tickCount % (60 - repair * 20 - modifier) == 0
            && AuraManager.decreaseClosestAura(player.level(), player.getX(), player.getY(), player.getZ(), 1)) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        }
    }
}
