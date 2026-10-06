package thaumcraft.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LegacyGen {
    private LegacyGen() {
    }

    public static void set(LevelAccessor level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    public static BlockState get(LevelAccessor level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z));
    }

    public static boolean isAir(LevelAccessor level, int x, int y, int z) {
        return level.isEmptyBlock(new BlockPos(x, y, z));
    }

    public static boolean isSnowOrGrass(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    public static boolean isValidSpawnBase(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT);
    }

    public static boolean locationIsValidSpawn(LevelAccessor level, int x, int y, int z) {
        int distanceToAir = 0;
        while (!isAir(level, x, y + distanceToAir, z)) {
            distanceToAir++;
            if (y + distanceToAir > level.getMaxBuildHeight() - 1) {
                return false;
            }
        }
        if (distanceToAir > 2) {
            return false;
        }
        y += distanceToAir - 1;
        BlockState state = get(level, x, y, z);
        if (!isAir(level, x, y + 1, z)) {
            return false;
        }
        if (isValidSpawnBase(state)) {
            return true;
        }
        return isSnowOrGrass(state) && isValidSpawnBase(get(level, x, y - 1, z));
    }
}
