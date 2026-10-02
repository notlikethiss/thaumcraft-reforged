package thaumcraft.world.gen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LegacyMinable {
    private LegacyMinable() {
    }

    public static void generate(LevelAccessor level, Random random, int x, int y, int z, int size, BlockState state) {
        float angle = random.nextFloat() * (float) Math.PI;
        double x0 = x + 8 + Mth.sin(angle) * size / 8.0F;
        double x1 = x + 8 - Mth.sin(angle) * size / 8.0F;
        double z0 = z + 8 + Mth.cos(angle) * size / 8.0F;
        double z1 = z + 8 - Mth.cos(angle) * size / 8.0F;
        double y0 = y + random.nextInt(3) - 2;
        double y1 = y + random.nextInt(3) - 2;
        for (int step = 0; step <= size; step++) {
            double cx = x0 + (x1 - x0) * step / size;
            double cy = y0 + (y1 - y0) * step / size;
            double cz = z0 + (z1 - z0) * step / size;
            double scale = random.nextDouble() * size / 16.0;
            double width = (Mth.sin(step * (float) Math.PI / size) + 1.0F) * scale + 1.0;
            double height = (Mth.sin(step * (float) Math.PI / size) + 1.0F) * scale + 1.0;
            int minX = Mth.floor(cx - width / 2.0);
            int minY = Mth.floor(cy - height / 2.0);
            int minZ = Mth.floor(cz - width / 2.0);
            int maxX = Mth.floor(cx + width / 2.0);
            int maxY = Mth.floor(cy + height / 2.0);
            int maxZ = Mth.floor(cz + width / 2.0);
            for (int bx = minX; bx <= maxX; bx++) {
                double dx = (bx + 0.5 - cx) / (width / 2.0);
                if (dx * dx >= 1.0) {
                    continue;
                }
                for (int by = minY; by <= maxY; by++) {
                    double dy = (by + 0.5 - cy) / (height / 2.0);
                    if (dx * dx + dy * dy >= 1.0) {
                        continue;
                    }
                    for (int bz = minZ; bz <= maxZ; bz++) {
                        double dz = (bz + 0.5 - cz) / (width / 2.0);
                        if (dx * dx + dy * dy + dz * dz < 1.0 && LegacyGen.get(level, bx, by, bz).is(Blocks.STONE)) {
                            LegacyGen.set(level, bx, by, bz, state);
                        }
                    }
                }
            }
        }
    }
}
