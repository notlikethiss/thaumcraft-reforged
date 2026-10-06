package thaumcraft.block.device;

import thaumcraft.blockentity.TcBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.Config;
import thaumcraft.block.WandTarget;
import thaumcraft.block.ward.WardHelper;
import thaumcraft.blockentity.OwnedBlockEntity;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

public class ArcaneDoorBlock extends DoorBlock implements EntityBlock, WandTarget {
    public ArcaneDoorBlock(Properties properties) {
        super(BlockSetType.IRON, properties);
    }

    public static BlockPos lowerHalf(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    private static BlockPos otherHalf(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        WardHelper.setOwner(level, pos, by);
        WardHelper.setOwner(level, pos.above(), by);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof OwnedBlockEntity owned && owned.canUse(player)) {
            BlockState toggled = state.cycle(OPEN);
            level.setBlock(pos, toggled, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            playDoorSound(level, pos);
            level.gameEvent(player, toggled.getValue(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        } else {
            player.sendSystemMessage(Component.translatable("tc.thaumcraft.door_refuses"));
            level.playSound(null, pos, ModSounds.DOORFAIL.get(), SoundSource.BLOCKS, 0.66F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private static void playDoorSound(Level level, BlockPos pos) {
        level.playSound(
            null,
            pos,
            level.getRandom().nextBoolean() ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
            SoundSource.BLOCKS,
            1.0F,
            level.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if (!(block instanceof ArcanePressurePlateBlock)) {
            return;
        }
        BlockPos lower = lowerHalf(state, pos);
        if (!(level.getBlockEntity(lower) instanceof OwnedBlockEntity door)) {
            return;
        }
        int open = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos platePos = lower.relative(direction);
            BlockState plate = level.getBlockState(platePos);
            if (plate.getBlock() instanceof ArcanePressurePlateBlock
                && level.getBlockEntity(platePos) instanceof OwnedBlockEntity owned
                && door.sharesUsersWith(owned)) {
                if (plate.getValue(ArcanePressurePlateBlock.POWERED)) {
                    open = 1;
                    break;
                }
                open = -1;
            }
        }
        BlockState lowerState = level.getBlockState(lower);
        if (open != 0 && lowerState.is(this) && lowerState.getValue(OPEN) != (open == 1)) {
            level.setBlock(lower, lowerState.setValue(OPEN, open == 1), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            playDoorSound(level, lower);
            level.gameEvent(null, open == 1 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, lower);
        }
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return Config.wardedStone() ? 0.0F : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        WardHelper.markCreativeRemoval(level, pos, player);
        WardHelper.markCreativeRemoval(level, otherHalf(state, pos), player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Config.wardedStone() ? List.of() : super.getDrops(state, params);
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        if (!level.isClientSide()
            && level.getBlockEntity(pos) instanceof OwnedBlockEntity owned
            && owned.canRemoveWard(player)
            && level.getBlockEntity(otherHalf(state, pos)) instanceof OwnedBlockEntity other) {
            other.markSafeToRemove();
        }
        return WardHelper.removeWithWand(level, pos, state, player, new ItemStack(ModItems.ARCANE_DOOR.get()));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwnedBlockEntity(pos, state);
    }
}
