package thaumcraft.block.bore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.block.WandTarget;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class ArcaneBoreBlock extends Block implements EntityBlock, WandTarget {
    public static final BooleanProperty BASE_BELOW = BooleanProperty.create("base_below");

    public ArcaneBoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BASE_BELOW, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BASE_BELOW);
    }

    public static Direction baseDirection(BlockState state) {
        return state.getValue(BASE_BELOW) ? Direction.DOWN : Direction.UP;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (side.getAxis().isHorizontal()) {
            return null;
        }
        BlockState state = defaultBlockState().setValue(BASE_BELOW, side == Direction.UP);
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(baseDirection(state))).is(ModBlocks.ARCANE_BORE_BASE.get());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (directionToNeighbour == baseDirection(state) && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        if (by != null && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            bore.setOrientation(Direction.orderedByNearest(by)[0].getOpposite(), true);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            player.openMenu(bore, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int b0, int b1) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(b0, b1);
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore)
            || !level.isEmptyBlock(pos.relative(side))
            || side == baseDirection(state)) {
            return InteractionResult.PASS;
        }
        bore.rotateTo(side);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ARCANE_BORE.get()) {
            return null;
        }
        BlockEntityTicker<ArcaneBoreBlockEntity> ticker = ArcaneBoreBlockEntity::tick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }
}
