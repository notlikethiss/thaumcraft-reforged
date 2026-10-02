package thaumcraft.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.aspect.Aspect;

public final class BiomeHandler {
    private record BiomeInfo(TagKey<Biome> tag, int visLevel, Aspect aspect, boolean greatwood, boolean silverwood) {
    }

    private static final List<BiomeInfo> INFO = List.of(
        new BiomeInfo(Tags.Biomes.IS_BEACH, 500, Aspect.WATER, false, false),
        new BiomeInfo(Tags.Biomes.IS_DESERT, 300, Aspect.FIRE, false, false),
        new BiomeInfo(Tags.Biomes.IS_END, 500, Aspect.ELDRITCH, false, false),
        new BiomeInfo(Tags.Biomes.IS_FOREST, 700, Aspect.WOOD, true, true),
        new BiomeInfo(Tags.Biomes.IS_SNOWY, 400, Aspect.COLD, false, false),
        new BiomeInfo(Tags.Biomes.IS_HILL, 600, Aspect.EARTH, false, true),
        new BiomeInfo(Tags.Biomes.IS_JUNGLE, 800, Aspect.PLANT, false, true),
        new BiomeInfo(Tags.Biomes.IS_MAGICAL, 900, Aspect.MAGIC, true, true),
        new BiomeInfo(Tags.Biomes.IS_MOUNTAIN, 500, Aspect.ROCK, false, false),
        new BiomeInfo(Tags.Biomes.IS_MUSHROOM, 500, Aspect.FUNGUS, false, false),
        new BiomeInfo(Tags.Biomes.IS_NETHER, 400, Aspect.EVIL, false, false),
        new BiomeInfo(Tags.Biomes.IS_PLAINS, 600, Aspect.WIND, true, false),
        new BiomeInfo(Tags.Biomes.IS_SWAMP, 700, Aspect.POISON, false, false),
        new BiomeInfo(Tags.Biomes.IS_WASTELAND, 300, Aspect.DEATH, false, false),
        new BiomeInfo(Tags.Biomes.IS_OCEAN, 500, Aspect.WATER, false, false),
        new BiomeInfo(Tags.Biomes.IS_RIVER, 500, Aspect.WATER, false, false)
    );

    private BiomeHandler() {
    }

    private static List<BiomeInfo> typesFor(Holder<Biome> biome) {
        List<BiomeInfo> types = new ArrayList<>();
        for (BiomeInfo info : INFO) {
            if (biome.is(info.tag())) {
                types.add(info);
            }
        }
        return types;
    }

    public static int getBiomeAura(Holder<Biome> biome) {
        List<BiomeInfo> types = typesFor(biome);
        if (types.isEmpty()) {
            return 300;
        }
        int total = 0;
        for (BiomeInfo info : types) {
            total += info.visLevel();
        }
        return total / types.size();
    }

    public static Aspect getRandomBiomeTag(Holder<Biome> biome, RandomSource random) {
        List<BiomeInfo> types = typesFor(biome);
        if (types.isEmpty()) {
            return Aspect.UNKNOWN;
        }
        return types.get(random.nextInt(types.size())).aspect();
    }

    public static boolean getBiomeSupportsGreatwood(Holder<Biome> biome) {
        return typesFor(biome).stream().anyMatch(BiomeInfo::greatwood);
    }

    public static boolean getBiomeSupportsSilverwood(Holder<Biome> biome) {
        return typesFor(biome).stream().anyMatch(BiomeInfo::silverwood);
    }
}
