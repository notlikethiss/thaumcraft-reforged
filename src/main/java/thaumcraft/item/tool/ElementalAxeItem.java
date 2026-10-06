package thaumcraft.item.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import thaumcraft.network.ModNetwork;
import thaumcraft.entity.FollowingItem;
import thaumcraft.network.BlockBoilPayload;
import thaumcraft.registry.ModSounds;

public class ElementalAxeItem extends Item {
    private static final int MAX_BLOCKS = 1024;
    private static boolean breaking;
    private static boolean alternate;

    public ElementalAxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (player == null || player.isShiftKeyDown() || !state.is(BlockTags.LOGS)) {
            return super.useOn(context);
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = context.getItemInHand();
            breakFurthest(serverLevel, serverPlayer, stack, pos, state);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.BUBBLE.value(), SoundSource.BLOCKS, 0.15F, 1.0F);
            stack.hurtAndBreak(alternate ? 1 : 2, player, context.getHand().asEquipmentSlot());
            alternate = !alternate;
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    public static boolean onBreakBlock(Level level, Player player, ItemStack stack, BlockPos pos, BlockState state) {
        if (breaking || player.isShiftKeyDown() || !state.is(BlockTags.LOGS)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            breakFurthest(serverLevel, serverPlayer, stack, pos, state);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.BUBBLE.value(), SoundSource.BLOCKS, 0.15F, 1.0F);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    private static void breakFurthest(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos origin, BlockState state) {
        Block block = state.getBlock();
        List<BlockPos> blocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        blocks.add(origin);
        visited.add(origin);
        BlockPos furthest = origin;
        for (int index = 0; index < blocks.size() && blocks.size() < MAX_BLOCKS; index++) {
            furthest = blocks.get(index);
            findBlocks(level, furthest, block, blocks, visited);
        }
        double furthestDistance = 0.0;
        for (BlockPos candidate : blocks) {
            double distance = candidate.distSqr(origin);
            if (distance > furthestDistance) {
                furthestDistance = distance;
                furthest = candidate;
            }
        }
        BlockState furthestState = level.getBlockState(furthest);
        if (!level.mayInteract(player, furthest)) {
            return;
        }
        breaking = true;
        try {
            if (CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, furthest, furthestState).isCanceled()) {
                return;
            }
        } finally {
            breaking = false;
        }
        ModNetwork.sendToNear(level, null, furthest.getX(), furthest.getY(), furthest.getZ(), 64.0, new BlockBoilPayload(furthest, 0.33F, 0.33F, 1.0F));
        ModNetwork.sendToNear(level, null, origin.getX(), origin.getY(), origin.getZ(), 64.0, new BlockBoilPayload(origin, 0.33F, 0.33F, 1.0F));
        BlockEntity blockEntity = furthestState.hasBlockEntity() ? level.getBlockEntity(furthest) : null;
        List<ItemStack> drops = Block.getDrops(furthestState, level, furthest, blockEntity, player, stack);
        level.destroyBlock(furthest, false, player);
        if (!player.isCreative()) {
            for (ItemStack drop : drops) {
                level.addFreshEntity(new FollowingItem(level, furthest.getX() + 0.5, furthest.getY() + 0.5, furthest.getZ() + 0.5, drop, player, 10));
            }
        }
    }

    private static void findBlocks(ServerLevel level, BlockPos center, Block block, List<BlockPos> blocks, Set<BlockPos> visited) {
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    BlockPos neighbor = center.offset(offsetX, offsetY, offsetZ);
                    if (level.getBlockState(neighbor).is(block) && visited.add(neighbor)) {
                        blocks.add(neighbor);
                    }
                }
            }
        }
    }
}
