package thaumcraft.item.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import thaumcraft.fx.Fx;
import thaumcraft.lib.Utils;
import thaumcraft.registry.ModAttachments;
import thaumcraft.registry.ModSounds;

public class ExcavationWandItem extends ElementalWandItem {
    private static final int USE_DURATION = 50;
    private static final double RANGE = 10.0;
    private static final long SOUND_DELAY_TICKS = 24L;

    public ExcavationWandItem(Properties properties) {
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
        if (!(entity instanceof Player player)) {
            return;
        }
        BlockHitResult hit = Utils.getTargetBlock(level, player, false, RANGE);
        boolean hasBlock = hit.getType() == HitResult.Type.BLOCK;
        Vec3 end = hasBlock ? hit.getLocation() : player.getEyePosition().add(player.getViewVector(1.0F).scale(RANGE));
        if (level.isClientSide()) {
            Fx.get().wandBeam(level, player, end.x, end.y, end.z, 2, 0x00FF66, false, hasBlock ? 2.0F : 0.0F, hasBlock ? 5 : 0);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ExcavationState state = state(player);
        if (hasBlock) {
            if (state.soundDelayUntil <= serverLevel.getGameTime()) {
                serverLevel.playSound(null, end.x, end.y, end.z, ModSounds.RUMBLE.value(), SoundSource.PLAYERS, 0.3F, 1.0F);
                state.soundDelayUntil = serverLevel.getGameTime() + SOUND_DELAY_TICKS;
            }
            excavate(serverLevel, serverPlayer, stack, state, hit.getBlockPos());
        } else {
            state.soundDelayUntil = 0L;
            clearProgress(serverLevel, player, state);
        }
        if (state.mined > stack.getMaxDamage() - stack.getDamageValue()) {
            player.releaseUsingItem();
        }
    }

    public static ExcavationState state(Player player) {
        return player.getData(ModAttachments.EXCAVATION);
    }

    private void excavate(ServerLevel level, ServerPlayer player, ItemStack stack, ExcavationState state, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        float destroySpeed = blockState.getDestroySpeed(level, pos);
        if (blockState.isAir() || destroySpeed < 0.0F) {
            clearProgress(level, player, state);
            return;
        }
        if (!pos.equals(state.target)) {
            level.destroyBlockProgress(player.getId(), pos, -1);
            if (state.target != null) {
                level.destroyBlockProgress(player.getId(), state.target, -1);
            }
            state.target = pos.immutable();
            state.breakCount = 0.0F;
            return;
        }
        float progress = blockState.getDestroyProgress(player, level, pos);
        float hardness = destroySpeed == 0.0F ? 0.0F : destroySpeed / progress / 100.0F;
        int potency = getPotency(level, stack);
        float speed = 0.1F + potency * 0.1F;
        if (blockState.is(BlockTags.MINEABLE_WITH_PICKAXE) || blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            speed = 1.0F + potency * 0.25F;
        }
        if (blockState.is(Blocks.OBSIDIAN)) {
            speed = 50.0F + potency * 5;
        }
        if (state.breakCount >= hardness) {
            if (breakBlock(level, player, stack, pos, blockState)) {
                state.mined++;
            }
            level.destroyBlockProgress(player.getId(), pos, -1);
            state.resetTarget();
        } else {
            if (state.breakCount > 0.0F) {
                level.destroyBlockProgress(player.getId(), pos, Math.min(9, (int) (state.breakCount / hardness * 9.0F)));
            }
            state.breakCount += speed;
        }
    }

    private boolean breakBlock(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos pos, BlockState blockState) {
        if (!level.mayInteract(player, pos)) {
            return false;
        }
        if (CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, blockState).isCanceled()) {
            return false;
        }
        int fortune = getTreasure(level, stack);
        ItemStack tool = stack.copy();
        if (fortune > 0) {
            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE)
                .ifPresent(holder -> EnchantmentHelper.updateEnchantments(tool, enchantments -> enchantments.set(holder, fortune)));
        }
        BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        level.levelEvent(2001, pos, Block.getId(blockState));
        Block.dropResources(blockState, level, pos, blockEntity, player, tool);
        level.destroyBlock(pos, false, player);
        return true;
    }

    private void clearProgress(ServerLevel level, Player player, ExcavationState state) {
        if (state.target != null) {
            level.destroyBlockProgress(player.getId(), state.target, -1);
        }
        state.resetTarget();
    }

    private void finishExcavation(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide() || !(entity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ExcavationState state = state(player);
        clearProgress(serverLevel, player, state);
        state.soundDelayUntil = 0L;
        int charges = Math.min(state.mined, stack.getMaxDamage() - stack.getDamageValue() + 1);
        state.mined = 0;
        damageWand(stack, player, player.getUsedItemHand(), charges);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        finishExcavation(stack, level, entity);
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        finishExcavation(stack, level, entity);
        return stack;
    }
}
