package thaumcraft.item.wand;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import thaumcraft.aura.AuraManager;
import thaumcraft.lib.MiningUtils;
import thaumcraft.registry.ModEnchantments;

public class ElementalWandItem extends Item {
    private final int chargeAmount;

    public ElementalWandItem(Properties properties) {
        this(5, properties);
    }

    public ElementalWandItem(int chargeAmount, Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE).setNoRepair());
        this.chargeAmount = chargeAmount;
    }

    public void damageWand(ItemStack stack, Player player, InteractionHand hand, int amount) {
        int frugal = MiningUtils.enchantmentLevel(player.level(), stack, ModEnchantments.FRUGAL);
        for (int index = 0; index < amount; index++) {
            if (player.getRandom().nextFloat() < 1.0F - frugal / 5.0F) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
        }
    }

    public void damageWand(ItemStack stack, ServerPlayer player, int amount) {
        int frugal = MiningUtils.enchantmentLevel(player.level(), stack, ModEnchantments.FRUGAL);
        for (int index = 0; index < amount; index++) {
            if (player.getRandom().nextFloat() < 1.0F - frugal / 5.0F) {
                stack.hurtAndBreak(1, player, stack == player.getOffhandItem() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
            }
        }
    }

    public int getPotency(Level level, ItemStack stack) {
        return MiningUtils.enchantmentLevel(level, stack, ModEnchantments.POTENCY);
    }

    public int getTreasure(Level level, ItemStack stack) {
        return MiningUtils.enchantmentLevel(level, stack, ModEnchantments.TREASURE);
    }

    public boolean canCharge(Level level, ItemStack stack) {
        return MiningUtils.enchantmentLevel(level, stack, ModEnchantments.CHARGING) > 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity owner, int slotId, boolean isSelected) {
        if (!level.isClientSide()
            && canCharge(level, stack)
            && owner.tickCount % 50 == 0
            && stack.getDamageValue() > 0
            && AuraManager.decreaseClosestAura(level, owner.getX(), owner.getY(), owner.getZ(), 1)) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - chargeAmount));
        }
    }
}
