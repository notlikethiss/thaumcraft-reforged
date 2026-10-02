package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.LevitatorBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class LevitatorBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LevitatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
            updateStack(level, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            updateStack(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        updateStack(level, pos);
    }

    private void updateStack(Level level, BlockPos pos) {
        for (int count = 1; level.getBlockState(pos.below(count)).is(this); count++) {
            if (level.getBlockEntity(pos.below(count)) instanceof LevitatorBlockEntity levitator) {
                levitator.requireUpdate();
            }
        }
        for (int count = 1; level.getBlockState(pos.above(count)).is(this); count++) {
            if (level.getBlockEntity(pos.above(count)) instanceof LevitatorBlockEntity levitator) {
                levitator.requireUpdate();
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) || !(level.getBlockEntity(pos) instanceof LevitatorBlockEntity levitator)) {
            return;
        }
        if (levitator.getRangeAbove() > 0) {
            Fx.get().sparkle(pos.getX() + 0.2F + random.nextFloat() * 0.6F, pos.getY() + 1, pos.getZ() + 0.2F + random.nextFloat() * 0.6F, 1.0F, 3, -0.3F);
        }
        if (levitator.getRangeBelow() > 0) {
            Fx.get().sparkle(pos.getX() + 0.2F + random.nextFloat() * 0.6F, pos.getY(), pos.getZ() + 0.2F + random.nextFloat() * 0.6F, 1.0F, 1, 0.3F);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LevitatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.LEVITATOR.get()) {
            return null;
        }
        BlockEntityTicker<LevitatorBlockEntity> ticker = LevitatorBlockEntity::tick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }
}
