package thaumcraft.item.wand;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModSounds;

public class FireWandItem extends ElementalWandItem {
    private static final int USE_DURATION = 50;
    private static final int RANGE = 17;

    public FireWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
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
        if (!(entity instanceof Player player)) {
            return;
        }
        if (level.isClientSide()) {
            Fx.get().wandFire(level, player, RANGE);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int ticksUsed = USE_DURATION - ticksRemaining;
        if (ticksUsed % 10 == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FIRELOOP.value(), SoundSource.PLAYERS, 0.25F, 1.0F);
        }
        burnTargets(serverLevel, player, stack);
        if (ticksUsed + 1 > stack.getMaxDamage() - stack.getDamageValue()) {
            player.releaseUsingItem();
        }
    }

    private void burnTargets(ServerLevel level, Player player, ItemStack stack) {
        int potency = getPotency(level, stack);
        Vec3 start = player.position();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(RANGE));
        AABB area = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
        List<Entity> entities = level.getEntities(player, area, candidate -> !candidate.isSpectator());
        DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.IN_FIRE), player);
        for (Entity target : entities) {
            if (!target.isPickable()) {
                continue;
            }
            float border = Math.max(1.0F, target.getPickRadius());
            AABB box = target.getBoundingBox().inflate(border, border * 1.25F, border);
            if (box.clip(start, end).isEmpty() && !box.contains(start)) {
                continue;
            }
            if (player.hasLineOfSight(target) && !target.fireImmune() && (!(target instanceof Player) || level.isPvpAllowed())) {
                target.igniteForSeconds(4 + potency);
                target.hurt(source, 2 + potency);
            }
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
            if (target instanceof Player && !serverLevel.isPvpAllowed()) {
                return;
            }
            target.igniteForSeconds(5.0F);
            damageWand(stack, player, InteractionHand.MAIN_HAND, 1);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, serverLevel.getRandom().nextFloat() * 0.4F + 0.8F);
        }
    }
}
