package thaumcraft.event;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraManager;
import thaumcraft.item.armor.Hover;
import thaumcraft.lib.MiningUtils;
import thaumcraft.lib.MovementModifiers;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModTags;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class EquipmentEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private static final Identifier SPEED_ID = Thaumcraft.id("boots_speed");
    private static final Identifier STEP_ID = Thaumcraft.id("boots_step");

    private EquipmentEvents() {
    }

    private static boolean wearsTravellerBoots(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.BOOTS_TRAVELLER.get());
    }

    @SubscribeEvent
    static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && wearsTravellerBoots(player)) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y + 0.275F, motion.z);
        }
    }

    private static void applyBootsMovement(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty() || player.getAbilities().flying) {
            MovementModifiers.setAdditive(player, Attributes.MOVEMENT_SPEED, SPEED_ID, 0.0);
            MovementModifiers.setAdditive(player, Attributes.STEP_HEIGHT, STEP_ID, 0.0);
            return;
        }
        int haste = MiningUtils.enchantmentLevel(player.level(), boots, ModEnchantments.HASTE);
        if (boots.is(ModItems.BOOTS_TRAVELLER.get())) {
            float bonus = haste * 0.007F;
            MovementModifiers.setAdditive(player, Attributes.MOVEMENT_SPEED, SPEED_ID, 0.155F + bonus - 0.1F);
            MovementModifiers.setAdditive(player, Attributes.STEP_HEIGHT, STEP_ID, 1.0 - 0.6);
            MovementModifiers.addAirSpeed(player, 0.033F + bonus * 0.66F);
            MovementModifiers.multiplyWaterSpeed(player, 1.133F);
            MovementModifiers.reduceFallDistance(player, 0.25);
        } else {
            float bonus = haste * 0.015F;
            MovementModifiers.setAdditive(player, Attributes.MOVEMENT_SPEED, SPEED_ID, bonus);
            MovementModifiers.setAdditive(player, Attributes.STEP_HEIGHT, STEP_ID, 0.0);
            if (haste > 0) {
                MovementModifiers.addAirSpeed(player, 0.02F + bonus * 0.66F);
            }
        }
    }

    @SubscribeEvent
    static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.getAbilities().flying) {
            ItemStack harness = Hover.getHarness(player);
            if (!harness.isEmpty()) {
                Hover.handleServer(player, harness);
            }
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        applyBootsMovement(event.getEntity());
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
