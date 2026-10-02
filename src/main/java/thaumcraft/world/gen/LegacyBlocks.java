package thaumcraft.world.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public final class LegacyBlocks {
    private LegacyBlocks() {
    }

    public static Direction horizontal(int meta) {
        return switch (meta) {
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public static BlockState stairs(BlockState base, int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return base.setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    public static BlockState of(String name, int meta) {
        return switch (name) {
            case "air" -> Blocks.AIR.defaultBlockState();
            case "dirt" -> Blocks.DIRT.defaultBlockState();
            case "grass" -> Blocks.GRASS_BLOCK.defaultBlockState();
            case "cobblestone" -> Blocks.COBBLESTONE.defaultBlockState();
            case "cobblestoneMossy" -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case "stairsCobblestone" -> stairs(Blocks.COBBLESTONE_STAIRS.defaultBlockState(), meta);
            case "fenceIron" -> Blocks.IRON_BARS.defaultBlockState();
            case "ladder" -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, horizontal(meta));
            case "mobSpawner" -> Blocks.SPAWNER.defaultBlockState();
            case "chest" -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, horizontal(meta));
            case "stoneBrick" -> switch (meta) {
                case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                case 2 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                case 3 -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                default -> Blocks.STONE_BRICKS.defaultBlockState();
            };
            case "tallGrass" -> meta == 2 ? Blocks.FERN.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState();
            default -> throw new IllegalArgumentException("Unknown legacy block " + name);
        };
    }
}
