package thaumcraft.item.wand;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.network.ModNetwork;
import thaumcraft.lib.Utils;
import thaumcraft.network.LightningWandPayload;
import thaumcraft.registry.ModSounds;

public class LightningWandItem extends ElementalWandItem {
    private static final int USE_DURATION = 50;

    public LightningWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }
        int ticksUsed = USE_DURATION - ticksRemaining;
        Entity pointedEntity = Utils.getPointedEntity(level, player, 20.0, 1.1F);
        int potency = getPotency(level, stack);
        if (ticksUsed % 2 == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SHOCK.value(), SoundSource.PLAYERS, 0.25F, 1.0F);
            BlockHitResult blockHit = Utils.getTargetBlock(level, player, false);
            Vec3 look = player.getViewVector(1.0F);
            Vec3 end = player.position().add(look.scale(10.0));
            boolean hasBlock = blockHit.getType() != HitResult.Type.MISS;
            if (hasBlock) {
                end = blockHit.getLocation();
            }
            boolean hasEntity = pointedEntity != null;
            if (hasEntity) {
                end = new Vec3(pointedEntity.getX(), pointedEntity.getY() + pointedEntity.getBbHeight() / 2.0F, pointedEntity.getZ());
            }
            Vec3 blockPoint = hasBlock ? blockHit.getLocation() : end;
            ModNetwork.sendToNear(serverLevel, null, player.getX(), player.getY(), player.getZ(), 64.0, new LightningWandPayload(
                player.getId(), end.x, end.y, end.z, hasBlock, blockPoint.x, blockPoint.y, blockPoint.z, hasEntity
            ));
        }
        if (pointedEntity != null) {
            pointedEntity.hurtServer(serverLevel, player.damageSources().playerAttack(player), 3 + potency);
            if (serverLevel.getRandom().nextInt(16 - Math.min(15, potency * 2)) == 0) {
                LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
                if (bolt != null) {
                    bolt.setVisualOnly(true);
                    bolt.snapTo(pointedEntity.getX(), pointedEntity.getY(), pointedEntity.getZ());
                    pointedEntity.thunderHit(serverLevel, bolt);
                }
            }
        }
        if (ticksUsed + 1 > stack.getMaxDamage() - stack.getDamageValue()) {
            player.releaseUsingItem();
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return false;
        }
        int charges = Math.min(USE_DURATION - remainingTime, stack.getMaxDamage() - stack.getDamageValue() + 1);
        damageWand(stack, player, player.getUsedItemHand(), charges);
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            int charges = Math.min(USE_DURATION, stack.getMaxDamage() - stack.getDamageValue() + 1);
            damageWand(stack, player, player.getUsedItemHand(), charges);
        }
        return stack;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel serverLevel && attacker instanceof Player player) {
            target.hurtServer(serverLevel, player.damageSources().playerAttack(player), 4.0F);
            damageWand(stack, player, InteractionHand.MAIN_HAND, 1);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.SHOCK.value(), SoundSource.PLAYERS, 0.25F, 1.0F);
        }
    }
}
