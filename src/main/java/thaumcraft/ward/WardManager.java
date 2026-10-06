package thaumcraft.ward;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import thaumcraft.Thaumcraft;
import thaumcraft.blockentity.OwnedBlockEntity;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class WardManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PEARL_RANGE = 5;
    private static final Map<ResourceKey<Level>, Queue<RestorableBlock>> RESTORE = new HashMap<>();

    private WardManager() {
    }

    public static void addBlockToRestore(ServerLevel level, BlockPos pos, BlockState state, CompoundTag tag) {
        RESTORE.computeIfAbsent(level.dimension(), key -> new ArrayDeque<>()).add(new RestorableBlock(pos.immutable(), state, tag));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Queue<RestorableBlock> queue = RESTORE.get(level.dimension());
        if (queue == null) {
            return;
        }
        RestorableBlock restorable;
        while ((restorable = queue.poll()) != null) {
            restore(level, restorable);
        }
    }

    private static void restore(ServerLevel level, RestorableBlock restorable) {
        level.setBlock(restorable.pos(), restorable.state(), Block.UPDATE_ALL);
        BlockEntity blockEntity = level.getBlockEntity(restorable.pos());
        if (blockEntity == null) {
            return;
        }
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
            blockEntity.loadWithComponents(TagValueInput.create(reporter, level.registryAccess(), restorable.tag()));
        }
        blockEntity.setChanged();
        level.sendBlockUpdated(restorable.pos(), restorable.state(), restorable.state(), Block.UPDATE_ALL);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            RESTORE.remove(level.dimension());
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof ThrownEnderpearl pearl)) {
            return;
        }
        if (!isWardedNearby(event.getLevel(), pearl.blockPosition())) {
            return;
        }
        event.setCanceled(true);
        if (pearl.getOwner() instanceof Player player) {
            player.sendSystemMessage(Component.translatable("tc.thaumcraft.warded_pearl"));
        }
    }

    private static boolean isWardedNearby(Level level, BlockPos center) {
        int minX = SectionPos.blockToSectionCoord(center.getX() - PEARL_RANGE);
        int maxX = SectionPos.blockToSectionCoord(center.getX() + PEARL_RANGE);
        int minZ = SectionPos.blockToSectionCoord(center.getZ() - PEARL_RANGE);
        int maxZ = SectionPos.blockToSectionCoord(center.getZ() + PEARL_RANGE);
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                if (!(level.getChunk(chunkX, chunkZ) instanceof LevelChunk chunk)) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (blockEntity instanceof OwnedBlockEntity
                        && Math.abs(pos.getX() - center.getX()) <= PEARL_RANGE
                        && Math.abs(pos.getY() - center.getY()) <= PEARL_RANGE
                        && Math.abs(pos.getZ() - center.getZ()) <= PEARL_RANGE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private record RestorableBlock(BlockPos pos, BlockState state, CompoundTag tag) {
    }
}
