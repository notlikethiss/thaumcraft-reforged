package thaumcraft.item.tool;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import thaumcraft.item.ModMaterials;
import thaumcraft.network.ModNetwork;
import thaumcraft.entity.FollowingItem;
import thaumcraft.lib.Utils;
import thaumcraft.network.BlockSparklePayload;

public class ElementalShovelItem extends ShovelItem {
    public ElementalShovelItem(Properties properties) {
        super(ModMaterials.ELEMENTAL_TOOL, properties.attributes(DiggerItem.createAttributes(ModMaterials.ELEMENTAL_TOOL, 0.0F, -3.0F)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState clicked = level.getBlockState(pos);
        if (level.getBlockEntity(pos) != null) {
            return InteractionResult.PASS;
        }
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();
        Item layerItem = clicked.getBlock().asItem();
        boolean placed = false;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                if (stack.isEmpty()) {
                    return placed ? InteractionResult.SUCCESS : InteractionResult.PASS;
                }
                BlockPos target = pos.offset(offsetX, 0, offsetZ).relative(face);
                if (!level.getBlockState(target).canBeReplaced() || !level.mayInteract(player, target)) {
                    continue;
                }
                BlockState state = clicked.getBlock().defaultBlockState();
                if (layerItem != Items.AIR && state.canSurvive(level, target) && Utils.consumeInventoryItem(player, layerItem)) {
                    place(level, player, stack, context, target, state);
                    placed = true;
                } else if (clicked.is(Blocks.GRASS_BLOCK) && Utils.consumeInventoryItem(player, Items.DIRT)) {
                    place(level, player, stack, context, target, Blocks.DIRT.defaultBlockState());
                    placed = true;
                }
            }
        }
        return placed ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static void place(ServerLevel level, ServerPlayer player, ItemStack stack, UseOnContext context, BlockPos target, BlockState state) {
        level.playSound(null, target, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.setBlock(target, state, Block.UPDATE_ALL);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        ModNetwork.sendToNear(level, null, target.getX(), target.getY(), target.getZ(), 64.0, new BlockSparklePayload(target, 3, 4));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (owner.isShiftKeyDown()) {
            return super.mineBlock(stack, level, state, pos, owner);
        }
        if (!(level instanceof ServerLevel serverLevel) || !(owner instanceof ServerPlayer player) || !isEffective(stack, state)) {
            return true;
        }
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                if ((offsetX == 0 && offsetZ == 0) || stack.isEmpty()) {
                    continue;
                }
                BlockPos neighbor = pos.offset(offsetX, 0, offsetZ);
                BlockState neighborState = level.getBlockState(neighbor);
                if (!isEffective(stack, neighborState) || neighborState.getDestroySpeed(level, neighbor) < 0.0F) {
                    continue;
                }
                if (!serverLevel.mayInteract(player, neighbor)
                    || CommonHooks.fireBlockBreak(serverLevel, player.gameMode.getGameModeForPlayer(), player, neighbor, neighborState).isCanceled()) {
                    continue;
                }
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                BlockEntity blockEntity = neighborState.hasBlockEntity() ? level.getBlockEntity(neighbor) : null;
                List<ItemStack> drops = Block.getDrops(neighborState, serverLevel, neighbor, blockEntity, player, stack);
                serverLevel.destroyBlock(neighbor, false, player);
                if (!player.isCreative()) {
                    for (ItemStack drop : drops) {
                        serverLevel.addFreshEntity(new FollowingItem(serverLevel, neighbor.getX() + 0.5, neighbor.getY() + 0.5, neighbor.getZ() + 0.5, drop, player, 3));
                    }
                }
            }
        }
        return true;
    }

    private static boolean isEffective(ItemStack stack, BlockState state) {
        return stack.isCorrectToolForDrops(state) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }
}
