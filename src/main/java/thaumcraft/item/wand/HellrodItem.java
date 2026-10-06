package thaumcraft.item.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import thaumcraft.aura.AuraManager;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.lib.Utils;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;

public class HellrodItem extends ElementalWandItem {
    public static final int MAX_CHARGES = 9;

    public HellrodItem(Properties properties) {
        super(properties);
    }

    public static int getCharges(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.HELLROD_CHARGES.get(), 0);
    }

    public static boolean hasCharges(ItemStack stack) {
        return stack.has(ModDataComponents.HELLROD_CHARGES.get());
    }

    public static void setCharges(ItemStack stack, int charges) {
        stack.set(ModDataComponents.HELLROD_CHARGES.get(), charges);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity owner, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }
        int interval = canCharge(level, stack) ? 25 : 50;
        if (!hasCharges(stack)) {
            setCharges(stack, 0);
        } else if (owner.tickCount % interval == 0) {
            int charges = getCharges(stack);
            if (charges < MAX_CHARGES && AuraManager.decreaseClosestAura(level, owner.getX(), owner.getY(), owner.getZ(), 6)) {
                setCharges(stack, charges + 1);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int charges = getCharges(stack);
        if (charges <= 0) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        Entity pointed = Utils.getPointedEntity(level, player, 32.0, FireBat.class);
        if (!(pointed instanceof LivingEntity target)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (level instanceof ServerLevel serverLevel) {
            if (target instanceof Player && !serverLevel.getServer().isPvpAllowed()) {
                return InteractionResultHolder.pass(player.getItemInHand(hand));
            }
            float yaw = player.getYRot();
            double px = player.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
            double py = player.getY() + player.getBbHeight() / 2.0F + 0.25 - 0.05F;
            double pz = player.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
            Vec3 look = player.getViewVector(1.0F);
            px += look.x * 0.5;
            py += look.y * 0.5;
            pz += look.z * 0.5;
            FireBat bat = new FireBat(ModEntities.FIRE_BAT.get(), serverLevel);
            bat.moveTo(px, py + bat.getBbHeight(), pz, yaw, 0.0F);
            bat.setTarget(target);
            bat.setSummoned(true);
            bat.setHanging(false);
            bat.setDamBonus(getPotency(level, stack));
            bat.setSummoner(player.getUUID());
            if (serverLevel.addFreshEntity(bat)) {
                serverLevel.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, BlockPos.containing(px, py, pz), 0);
                setCharges(stack, charges - 1);
            }
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WANDFAIL.value(), SoundSource.PLAYERS, 0.4F, 0.9F + serverLevel.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
