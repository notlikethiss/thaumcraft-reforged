package thaumcraft.block.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ObsidianTotemBlock extends Block {
    public enum Part implements StringRepresentable {
        BASE,
        SHADED,
        CARVED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final IntegerProperty CARVING = IntegerProperty.create("carving", 0, 3);

    public ObsidianTotemBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, Part.BASE).setValue(CARVING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, CARVING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return shapeFor(context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        LevelReader level,
        ScheduledTickAccess ticks,
        BlockPos pos,
        Direction direction,
        BlockPos neighbourPos,
        BlockState neighbourState,
        RandomSource random
    ) {
        return direction.getAxis() == Direction.Axis.Y ? shapeFor(level, pos) : state;
    }

    private BlockState shapeFor(LevelReader level, BlockPos pos) {
        Part part;
        if (level.getBlockState(pos.above()).is(this)) {
            part = Part.SHADED;
        } else if (level.getBlockState(pos.below()).is(this)) {
            part = Part.CARVED;
        } else {
            part = Part.BASE;
        }
        int carving = Mth.positiveModulo(pos.getX() % 4 + pos.getZ() % 4 + pos.getY() % 4, 4);
        return defaultBlockState().setValue(PART, part).setValue(CARVING, carving);
    }
}
