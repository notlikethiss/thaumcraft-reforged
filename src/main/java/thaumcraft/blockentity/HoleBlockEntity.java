package thaumcraft.blockentity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import thaumcraft.Config;
import thaumcraft.fx.Fx;
import thaumcraft.network.BlockSparklePayload;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModTags;

public class HoleBlockEntity extends TcBlockEntity {
    private static final int DURATION = 120;
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private BlockState oldState = Blocks.AIR.defaultBlockState();
    private int countdown;
    private int count;
    private @Nullable Direction direction;

    public HoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLE.get(), pos, state);
    }

    private static boolean isBlacklisted(BlockState state) {
        if (state.is(ModTags.PORTABLE_HOLE_BLACKLIST)) {
            return true;
        }
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        List<? extends String> blacklist = Config.PORTABLE_HOLE_BLACKLIST.get();
        return blacklist.contains(id);
    }

    public static boolean canPassThrough(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir()
            && !isBlacklisted(state)
            && !state.is(Blocks.BEDROCK)
            && !state.is(ModBlocks.HOLE.get())
            && state.getDestroySpeed(level, pos) != -1.0F;
    }

    public static boolean createHole(Level level, BlockPos pos, @Nullable Direction direction, int count) {
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) != null
            || state.isAir()
            || isBlacklisted(state)
            || state.is(Blocks.BEDROCK)
            || state.is(ModBlocks.HOLE.get())
            || state.canBeReplaced()
            || state.getDestroySpeed(level, pos) == -1.0F) {
            return false;
        }
        level.setBlock(pos, ModBlocks.HOLE.get().defaultBlockState(), PLACE_FLAGS);
        if (level.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
            hole.oldState = state;
            hole.count = count;
            hole.direction = direction;
            hole.sync();
        }
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 64.0, new BlockSparklePayload(pos, 5, 1));
        }
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HoleBlockEntity hole) {
        if (level.isClientSide()) {
            hole.surroundWithSparkles(level);
        } else if (hole.countdown == 0 && hole.count > 1 && hole.direction != null) {
            hole.spread(level);
        }
        hole.countdown++;
        if (hole.countdown >= DURATION && !level.isClientSide()) {
            level.setBlock(pos, hole.oldState, PLACE_FLAGS);
        }
    }

    private void spread(Level level) {
        Direction.Axis axis = direction.getAxis();
        for (int index = 0; index < 9; index++) {
            if (index / 3 == 1 && index % 3 == 1) {
                continue;
            }
            int a = index / 3 - 1;
            int b = index % 3 - 1;
            BlockPos ring = switch (axis) {
                case Y -> worldPosition.offset(a, 0, b);
                case Z -> worldPosition.offset(a, b, 0);
                case X -> worldPosition.offset(0, a, b);
            };
            createHole(level, ring, null, 1);
        }
        if (!createHole(level, worldPosition.relative(direction), direction, count - 1)) {
            count = 0;
        }
    }

    private void surroundWithSparkles(Level level) {
        for (Edge edge : EDGES) {
            BlockState open = level.getBlockState(worldPosition.relative(edge.open()));
            if (!open.isSolidRender() && level.getBlockState(worldPosition.relative(edge.wall())).isSolidRender() && !open.is(ModBlocks.HOLE.get())) {
                Fx.get().sparkle(
                    worldPosition.getX() + edge.coordinate(edge.x(), level),
                    worldPosition.getY() + edge.coordinate(edge.y(), level),
                    worldPosition.getZ() + edge.coordinate(edge.z(), level),
                    2
                );
            }
        }
    }

    private record Edge(Direction open, Direction wall, int x, int y, int z) {
        float coordinate(int value, Level level) {
            return value < 0 ? level.getRandom().nextFloat() : value;
        }
    }

    private static final Edge[] EDGES = {
        new Edge(Direction.EAST, Direction.UP, 1, 1, -1),
        new Edge(Direction.WEST, Direction.UP, 0, 1, -1),
        new Edge(Direction.SOUTH, Direction.UP, -1, 1, 1),
        new Edge(Direction.NORTH, Direction.UP, -1, 1, 0),
        new Edge(Direction.EAST, Direction.DOWN, 1, 0, -1),
        new Edge(Direction.WEST, Direction.DOWN, 0, 0, -1),
        new Edge(Direction.SOUTH, Direction.DOWN, -1, 0, 1),
        new Edge(Direction.NORTH, Direction.DOWN, -1, 0, 0),
        new Edge(Direction.UP, Direction.EAST, 1, 1, -1),
        new Edge(Direction.DOWN, Direction.EAST, 1, 0, -1),
        new Edge(Direction.SOUTH, Direction.EAST, 1, -1, 1),
        new Edge(Direction.NORTH, Direction.EAST, 1, -1, 0),
        new Edge(Direction.UP, Direction.WEST, 0, 1, -1),
        new Edge(Direction.DOWN, Direction.WEST, 0, 0, -1),
        new Edge(Direction.SOUTH, Direction.WEST, 0, -1, 1),
        new Edge(Direction.NORTH, Direction.WEST, 0, -1, 0),
        new Edge(Direction.EAST, Direction.SOUTH, 1, -1, 1),
        new Edge(Direction.WEST, Direction.SOUTH, 0, -1, 1),
        new Edge(Direction.UP, Direction.SOUTH, -1, 1, 1),
        new Edge(Direction.DOWN, Direction.SOUTH, -1, 0, 1),
        new Edge(Direction.EAST, Direction.NORTH, 1, -1, 0),
        new Edge(Direction.WEST, Direction.NORTH, 0, -1, 0),
        new Edge(Direction.UP, Direction.NORTH, -1, 1, 0),
        new Edge(Direction.DOWN, Direction.NORTH, -1, 0, 0)
    };

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        oldState = input.read("old_state", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        countdown = input.getIntOr("countdown", 0);
        count = input.getIntOr("count", 0);
        direction = input.read("direction", Direction.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("old_state", BlockState.CODEC, oldState);
        output.putInt("countdown", countdown);
        output.putInt("count", count);
        if (direction != null) {
            output.store("direction", Direction.CODEC, direction);
        }
    }
}
