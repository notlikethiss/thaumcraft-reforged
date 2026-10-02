package thaumcraft.world.gen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import thaumcraft.registry.ModBlocks;

public class SilverwoodTreeGenerator extends MagicalTreeGenerator {
    public SilverwoodTreeGenerator(boolean notify) {
        super(
            notify,
            ModBlocks.SILVERWOOD_LOG.get().defaultBlockState(),
            ModBlocks.SILVERWOOD_LEAVES.get().defaultBlockState(),
            -0.3,
            1.0,
            1,
            5
        );
    }

    public boolean generate(LevelAccessor level, Random random, BlockPos pos) {
        if (!prepare(level, random, pos)) {
            return false;
        }
        generateSegment(pos.getX(), pos.getY(), pos.getZ());
        generateSegment(pos.getX(), pos.getY() + height, pos.getZ());
        generateSegment(pos.getX(), pos.getY() + height * 2, pos.getZ());
        return true;
    }
}
