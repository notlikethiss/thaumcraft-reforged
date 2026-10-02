package thaumcraft.world.gen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import thaumcraft.Thaumcraft;

public final class MoundGenerator {
    public static final ResourceKey<LootTable> MOUND_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Thaumcraft.id("chests/mound"));

    private record Entry(int x, int y, int z, BlockState state) {
    }

    private static List<Entry> template;

    private MoundGenerator() {
    }

    private static synchronized List<Entry> template() {
        if (template != null) {
            return template;
        }
        List<Entry> entries = new ArrayList<>();
        try (InputStream stream = MoundGenerator.class.getResourceAsStream("/thaumcraft/structures/mound.txt")) {
            if (stream == null) {
                throw new IllegalStateException("Missing mound template");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(" ");
                entries.add(new Entry(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    LegacyBlocks.of(parts[3], Integer.parseInt(parts[4]))
                ));
            }
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        template = entries;
        return entries;
    }

    public static boolean generate(LevelAccessor level, Random random, int i, int j, int k) {
        if (!LegacyGen.locationIsValidSpawn(level, i + 9, j + 9, k + 9)
            || !LegacyGen.locationIsValidSpawn(level, i, j + 9, k)
            || !LegacyGen.locationIsValidSpawn(level, i + 18, j + 9, k)
            || !LegacyGen.locationIsValidSpawn(level, i + 18, j + 9, k + 18)
            || !LegacyGen.locationIsValidSpawn(level, i, j + 9, k + 18)) {
            return false;
        }
        for (Entry entry : template()) {
            LegacyGen.set(level, i + entry.x(), j + entry.y(), k + entry.z(), entry.state());
        }
        loot(level, new BlockPos(i + 9, j + 1, k + 7), BuiltInLootTables.JUNGLE_TEMPLE, random);
        loot(level, new BlockPos(i + 9, j + 1, k + 11), BuiltInLootTables.STRONGHOLD_LIBRARY, random);
        loot(level, new BlockPos(i + 10, j + 1, k + 9), MOUND_LOOT, random);
        spawner(level, new BlockPos(i + 4, j + 5, k + 4), EntityTypes.SKELETON);
        spawner(level, new BlockPos(i + 4, j + 5, k + 14), EntityTypes.ZOMBIE);
        return true;
    }

    static void loot(LevelAccessor level, BlockPos pos, ResourceKey<LootTable> table, Random random) {
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(table, random.nextLong());
        }
    }

    static void spawner(LevelAccessor level, BlockPos pos, EntityType<?> type) {
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, level.getRandom());
        }
    }
}
