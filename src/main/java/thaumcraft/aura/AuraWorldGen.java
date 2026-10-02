package thaumcraft.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import thaumcraft.registry.ModAttachments;

public final class AuraWorldGen {
    private AuraWorldGen() {
    }

    public static void registerNode(WorldGenLevel level, int value, NodeType type, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        AuraNode node = new AuraNode(-1, value, type, level.getLevel().dimension(), pos);
        chunk.getData(ModAttachments.AURA_CHUNK).addPending(node);
        chunk.markUnsaved();
    }

    public static boolean auraNearby(WorldGenLevel level, int x, int y, int z, int range) {
        if (AuraManager.auraNearby(level.getLevel().dimension(), x, y, z, range)) {
            return true;
        }
        return pendingNearby(level, x, y, z, range, null);
    }

    public static boolean specificAuraTypeNearby(WorldGenLevel level, int x, int y, int z, NodeType type, int range) {
        if (AuraManager.specificAuraTypeNearby(level.getLevel().dimension(), x, y, z, type, range)) {
            return true;
        }
        return pendingNearby(level, x, y, z, range, type);
    }

    private static boolean pendingNearby(WorldGenLevel level, int x, int y, int z, int range, NodeType type) {
        int cx = SectionPos.blockToSectionCoord(x);
        int cz = SectionPos.blockToSectionCoord(z);
        int radius = SectionPos.blockToSectionCoord(range) + 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (!level.hasChunk(cx + dx, cz + dz)) {
                    continue;
                }
                ChunkAccess chunk = level.getChunk(cx + dx, cz + dz, ChunkStatus.EMPTY, false);
                if (chunk == null || !chunk.hasData(ModAttachments.AURA_CHUNK)) {
                    continue;
                }
                for (AuraNode node : chunk.getData(ModAttachments.AURA_CHUNK).pendingView()) {
                    if (type != null && node.type != type) {
                        continue;
                    }
                    double ddx = (float) node.x - x + 0.5F;
                    double ddy = (float) node.y - y + 0.5F;
                    double ddz = (float) node.z - z + 0.5F;
                    if (ddx * ddx + ddy * ddy + ddz * ddz < range * range) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
