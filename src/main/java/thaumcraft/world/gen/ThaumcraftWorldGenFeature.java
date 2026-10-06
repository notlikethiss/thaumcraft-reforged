package thaumcraft.world.gen;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import thaumcraft.Config;
import thaumcraft.aspect.Aspect;
import thaumcraft.aura.AuraWorldGen;
import thaumcraft.aura.NodeType;
import thaumcraft.block.world.InfusedStoneBlock;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.BiomeHandler;

public class ThaumcraftWorldGenFeature extends Feature<NoneFeatureConfiguration> {
    private static final Set<ResourceKey<Structure>> SCATTERED = Set.of(
        BuiltinStructures.DESERT_PYRAMID,
        BuiltinStructures.JUNGLE_TEMPLE,
        BuiltinStructures.SWAMP_HUT
    );

    public ThaumcraftWorldGenFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        BlockPos origin = context.origin();
        Random random = new Random(context.random().nextLong());
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        ResourceKey<Level> dimension = level.getLevel().dimension();
        if (dimension == Level.NETHER) {
            generateAura(level, random, chunkX, chunkZ, false);
        } else if (dimension != Level.END) {
            generateSurface(level, chunkGenerator, random, chunkX, chunkZ);
        }
        return true;
    }

    private static int height(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    private static int firstUncoveredY(WorldGenLevel level, int x, int z) {
        int y = 5;
        while (!level.isEmptyBlock(new BlockPos(x, y + 1, z)) && y < level.getMaxBuildHeight() - 1) {
            y++;
        }
        return y;
    }

    private static boolean generateAura(WorldGenLevel level, Random random, int chunkX, int chunkZ, boolean auraGen) {
        if (random.nextInt(Config.NODE_RARITY.getAsInt()) != 0 || auraGen) {
            return false;
        }
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int q = firstUncoveredY(level, x, z);
        if (level.isEmptyBlock(new BlockPos(x, q + 1, z))) {
            q++;
        }
        int p = random.nextInt(6);
        if (level.isEmptyBlock(new BlockPos(x, q + p, z))) {
            q += p;
            if (p == 5) {
                p = random.nextInt(5);
            }
            if (level.isEmptyBlock(new BlockPos(x, q + p, z))) {
                q += p;
            }
        }
        int y = q;
        if (AuraWorldGen.auraNearby(level, x, y, z, 64)) {
            return false;
        }
        Holder<Biome> biome = level.getBiome(new BlockPos(x, y, z));
        int aura = BiomeHandler.getBiomeAura(biome);
        int value = random.nextInt(Math.max(1, aura / 2)) + aura / 2;
        NodeType type = NodeType.NORMAL;
        if (random.nextInt(Config.SPECIAL_NODE_RARITY.getAsInt()) == 0) {
            type = switch (random.nextInt(3)) {
                case 0 -> NodeType.PURE;
                case 1 -> NodeType.DARK;
                default -> NodeType.UNSTABLE;
            };
        }
        boolean nether = level.getLevel().dimension() == Level.NETHER;
        if (random.nextInt(type != NodeType.NORMAL ? 2 : (nether ? 2 : 6)) == 0) {
            buildTotem(level, random, x, z, nether);
        }
        AuraWorldGen.registerNode(level, value, type, new BlockPos(x, y, z));
        return true;
    }

    private static void buildTotem(WorldGenLevel level, Random random, int x, int z, boolean nether) {
        int topY = nether ? firstUncoveredY(level, x, z) - 1 : height(level, x, z) - 1;
        if (LegacyGen.get(level, x, topY, z).is(BlockTags.LEAVES)) {
            do {
                topY--;
            } while (!LegacyGen.get(level, x, topY, z).is(Blocks.GRASS_BLOCK) && topY > 40);
        }
        BlockState top = LegacyGen.get(level, x, topY, z);
        if (top.is(Blocks.SNOW) || top.is(Blocks.SHORT_GRASS)) {
            topY--;
            top = LegacyGen.get(level, x, topY, z);
        }
        if (!(top.is(Blocks.GRASS_BLOCK) || top.is(Blocks.SAND) || top.is(Blocks.DIRT) || top.is(Blocks.STONE) || top.is(Blocks.NETHERRACK))) {
            return;
        }
        int count = 1;
        while (LegacyGen.isAir(level, x, topY + count, z) && count < 3) {
            count++;
        }
        if (count < 2) {
            return;
        }
        LegacyGen.set(level, x, topY, z, ModBlocks.OBSIDIAN_TILE.get().defaultBlockState());
        for (int i = 1; LegacyGen.isAir(level, x, topY + i, z) && i < 5; i++) {
            LegacyGen.set(level, x, topY + i, z, ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState());
            if (i > 1 && random.nextInt(4) == 0) {
                i = 5;
            }
        }
    }

    private static void generateSurface(WorldGenLevel level, ChunkGenerator generator, Random random, int chunkX, int chunkZ) {
        boolean auraGen = false;
        boolean flat = generator instanceof FlatLevelSource;
        if (!flat) {
            if (random.nextInt(100) == 42) {
                generateSilverwood(level, random, chunkX, chunkZ);
            } else if (random.nextInt(20) == 7) {
                generateGreatwood(level, random, chunkX, chunkZ);
            }
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = height(level, x, z);
            Holder<Biome> biome = level.getBiome(new BlockPos(x, y, z));
            if (biome.value().getBaseTemperature() > 1.0F && LegacyGen.get(level, x, y - 1, z).is(Blocks.SAND) && random.nextInt(30) == 0) {
                generateFlowers(level, random, x, y, z, ModBlocks.CINDERPEARL.get().defaultBlockState());
            }

            x = chunkX * 16 + random.nextInt(16) - 8;
            z = chunkZ * 16 + random.nextInt(16) - 8;
            y = height(level, x, z) - 9;
            long chunkSeed = level.getSeed() + (long) chunkX * chunkX * 4987142L + chunkX * 5947611L + (long) chunkZ * chunkZ * 4392871L + chunkZ * 389711L ^ 957234911L;
            if (new Random(chunkSeed).nextInt(100) != 0 || AuraWorldGen.specificAuraTypeNearby(level, x + 9, y + 8, z + 9, NodeType.DARK, 250)) {
                y += 9;
                if (random.nextInt(3) == 0
                    && !AuraWorldGen.specificAuraTypeNearby(level, x, y, z, NodeType.UNSTABLE, 250)
                    && HilltopStonesGenerator.generate(level, random, x, y, z)) {
                    auraGen = true;
                    AuraWorldGen.registerNode(level, random.nextInt(200) + 400, NodeType.UNSTABLE, new BlockPos(x, y + 5, z));
                }
            } else if (MoundGenerator.generate(level, random, x, y, z)) {
                auraGen = true;
                AuraWorldGen.registerNode(level, random.nextInt(200) + 400, NodeType.DARK, new BlockPos(x + 9, y + 8, z + 9));
            }
        }

        BlockPos temple = findScatteredStart(level, chunkX, chunkZ);
        if (temple != null) {
            auraGen = true;
            AuraWorldGen.registerNode(level, random.nextInt(200) + 800, NodeType.NORMAL, new BlockPos(temple.getX(), height(level, temple.getX(), temple.getZ()) + 3, temple.getZ()));
        }
        generateAura(level, random, chunkX, chunkZ, auraGen);

        for (int i = 0; i < 15; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int y = random.nextInt(256 / 5);
            int z = chunkZ * 16 + random.nextInt(16);
            if (LegacyGen.get(level, x, y, z).is(Blocks.STONE)) {
                LegacyGen.set(level, x, y, z, ModBlocks.CINNABAR_ORE.get().defaultBlockState());
            }
        }
        for (int i = 0; i < 15; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = height(level, x, z) - 5 - random.nextInt(25);
            if (LegacyGen.get(level, x, y, z).is(Blocks.STONE)) {
                LegacyGen.set(level, x, y, z, ModBlocks.AMBER_ORE.get().defaultBlockState());
            }
        }
        for (int i = 0; i < 8; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = random.nextInt(Math.max(5, height(level, x, z) - 5));
            int type = random.nextInt(5) + 1;
            if (random.nextInt(3) == 0) {
                Holder<Biome> biome = level.getBiome(new BlockPos(x, y, z));
                Aspect aspect = BiomeHandler.getRandomBiomeTag(biome, level.getRandom());
                type = aspect == Aspect.UNKNOWN ? 1 + random.nextInt(5) : BiomeHandler.getRandomBiomeTag(biome, level.getRandom()).element;
            }
            LegacyMinable.generate(level, random, x, y, z, 6, InfusedStoneBlock.byType(type).get().defaultBlockState());
        }
    }

    private static BlockPos findScatteredStart(WorldGenLevel level, int chunkX, int chunkZ) {
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : chunk.getAllStarts().entrySet()) {
            if (!entry.getValue().isValid()) {
                continue;
            }
            var key = registry.getResourceKey(entry.getKey());
            if (key.isPresent() && SCATTERED.contains(key.get())) {
                return entry.getValue().getBoundingBox().getCenter();
            }
        }
        return null;
    }

    private static void generateFlowers(WorldGenLevel level, Random random, int x, int y, int z, BlockState flower) {
        for (int i = 0; i < 18; i++) {
            int fx = x + random.nextInt(8) - random.nextInt(8);
            int fy = y + random.nextInt(4) - random.nextInt(4);
            int fz = z + random.nextInt(8) - random.nextInt(8);
            BlockState below = LegacyGen.get(level, fx, fy - 1, fz);
            if (LegacyGen.isAir(level, fx, fy, fz) && (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SAND))) {
                LegacyGen.set(level, fx, fy, fz, flower);
            }
        }
    }

    private static boolean generateSilverwood(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = height(level, x, z);
        Holder<Biome> biome = level.getBiome(new BlockPos(x, y, z));
        if (BiomeHandler.getBiomeSupportsSilverwood(biome) && new SilverwoodTreeGenerator(false).generate(level, random, new BlockPos(x, y, z))) {
            AuraWorldGen.registerNode(level, random.nextInt(200) + 200, NodeType.PURE, new BlockPos(x, y + 1, z));
            generateFlowers(level, random, x, y, z, ModBlocks.SHIMMERLEAF.get().defaultBlockState());
            return true;
        }
        return false;
    }

    private static boolean generateGreatwood(WorldGenLevel level, Random random, int chunkX, int chunkZ) {
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = height(level, x, z);
        Holder<Biome> biome = level.getBiome(new BlockPos(x, y, z));
        return BiomeHandler.getBiomeSupportsGreatwood(biome)
            && new GreatwoodTreeGenerator(false).generate(level, random, new BlockPos(x, y, z), random.nextInt(8) == 0);
    }
}
