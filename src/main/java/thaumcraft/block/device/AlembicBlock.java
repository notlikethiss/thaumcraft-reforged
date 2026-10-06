package thaumcraft.block.device;

import com.mojang.serialization.MapCodec;
import thaumcraft.blockentity.TcBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.block.WandTarget;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;

public class AlembicBlock extends HorizontalDirectionalBlock implements EntityBlock, WandTarget {
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0);
    private static final Direction[] SEARCH_ORDER = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public AlembicBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(AlembicBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace().getOpposite();
        if (clicked.getAxis().isHorizontal() && isCrucible(context.getLevel(), context.getClickedPos().relative(clicked))) {
            return defaultBlockState().setValue(FACING, clicked);
        }
        for (Direction direction : SEARCH_ORDER) {
            if (isCrucible(context.getLevel(), context.getClickedPos().relative(direction))) {
                return defaultBlockState().setValue(FACING, direction);
            }
        }
        return null;
    }

    private static boolean isCrucible(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.CRUCIBLE.get());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && alembic.getAmount() > 0) {
                playBubble(level, pos);
                level.blockEvent(pos, this, 0, alembic.getAspect().color);
                alembic.spillRemnants();
            }
            return InteractionResult.SUCCESS;
        }
        if (alembic.getAmount() <= 0
            || !(level.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof CrucibleBlockEntity crucible)
            || !crucible.isBoiling()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            crucible.addToSource(alembic.getAspect(), alembic.getAmount());
            alembic.takeFromSource(alembic.getAspect(), alembic.getAmount());
            playBubble(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static void playBubble(Level level, BlockPos pos) {
        level.playSound(null, pos, ModSounds.BUBBLE.value(), SoundSource.BLOCKS, 0.2F, 1.0F + level.getRandom().nextFloat() * 0.4F);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int param) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(type, param);
    }
}
