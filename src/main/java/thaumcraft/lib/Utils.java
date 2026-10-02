package thaumcraft.lib;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public final class Utils {
    private Utils() {
    }

    public static boolean isChunkLoaded(Level level, double x, double z) {
        return level.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    public static int getFirstUncoveredBlockHeight(Level level, int x, int z) {
        int y = Math.max(10, level.getMinY());
        while (y < level.getMaxY() && !level.isEmptyBlock(new BlockPos(x, y + 1, z))) {
            y++;
        }
        return y;
    }
}
