package thaumcraft.world.gen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlocks;

public final class HilltopStonesGenerator {
    public static final ResourceKey<LootTable> HILLTOP_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Thaumcraft.id("chests/hilltop_stones"));

    private HilltopStonesGenerator() {
    }

    private static boolean valid(LevelAccessor level, int x, int y, int z) {
        return y >= 85 && LegacyGen.locationIsValidSpawn(level, x, y, z);
    }

    public static boolean generate(WorldGenLevel level, Random rand, int i, int j, int k) {
        if (!valid(level, i - 2, j, k - 2) || !valid(level, i, j, k) || !valid(level, i + 2, j, k) || !valid(level, i + 2, j, k + 2) || !valid(level, i, j, k + 2)) {
            return false;
        }
        Biome biome = level.getBiome(new BlockPos(i, j, k)).value();
        BlockState replace = level.getBlockState(new BlockPos(i, j, k));
        if (!LegacyGen.isValidSpawnBase(replace)) {
            replace = Blocks.GRASS_BLOCK.defaultBlockState();
        }
        boolean genVines = biome.warmEnoughToRain(new BlockPos(i, j, k), level.getSeaLevel());
        BlockState tile = ModBlocks.OBSIDIAN_TILE.get().defaultBlockState();
        BlockState totem = ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState();
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) {
                    continue;
                }
                LegacyGen.set(level, x, j, z, rand.nextBoolean() ? tile : Blocks.OBSIDIAN.defaultBlockState());
                boolean stop = false;
                for (int y = 1; y < 5; y++) {
                    if (j - y < level.getMinY()) {
                        continue;
                    }
                    BlockState below = LegacyGen.get(level, x, j - y, z);
                    if (below.is(Blocks.SNOW) || below.is(Blocks.POPPY) || below.is(Blocks.DANDELION) || below.is(Blocks.SHORT_GRASS) || below.isAir()) {
                        LegacyGen.set(level, x, j - y, z, replace);
                    }
                    if (x == i && z == k && y == 1) {
                        LegacyGen.set(level, x, j + y, z, tile);
                        BlockPos chestPos = new BlockPos(x, j + y + 1, z);
                        LegacyGen.set(level, x, j + y + 1, z, Blocks.CHEST.defaultBlockState());
                        MoundGenerator.loot(level, chestPos, HILLTOP_LOOT, rand);
                        LegacyGen.set(level, x, j + y - 1, z, Blocks.SPAWNER.defaultBlockState());
                        EntityType<?> wisp = BuiltInRegistries.ENTITY_TYPE.getOptional(Thaumcraft.id("wisp")).orElse(null);
                        if (wisp != null) {
                            MoundGenerator.spawner(level, new BlockPos(x, j + y - 1, z), wisp);
                        }
                    }
                    boolean edge = (x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1
                        || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1;
                    if (!stop && edge) {
                        LegacyGen.set(level, x, j + y, z, totem);
                        if (y >= 2 && rand.nextBoolean()) {
                            stop = true;
                            if (genVines) {
                                vinesAround(level, rand, x, j + y, z);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void vinesAround(LevelAccessor level, Random rand, int x, int y, int z) {
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x - 1, y, z)) {
            growVines(level, x - 1, y, z, VineBlock.EAST);
        }
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x + 1, y, z)) {
            growVines(level, x + 1, y, z, VineBlock.WEST);
        }
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x, y, z - 1)) {
            growVines(level, x, y, z - 1, VineBlock.SOUTH);
        }
        if (rand.nextInt(3) == 0 && LegacyGen.isAir(level, x, y, z + 1)) {
            growVines(level, x, y, z + 1, VineBlock.NORTH);
        }
    }

    private static void growVines(LevelAccessor level, int x, int y, int z, net.minecraft.world.level.block.state.properties.BooleanProperty side) {
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(side, true);
        LegacyGen.set(level, x, y, z, vine);
        for (int count = 4; LegacyGen.isAir(level, x, --y, z) && count > 0; count--) {
            LegacyGen.set(level, x, y, z, vine);
        }
    }
}
