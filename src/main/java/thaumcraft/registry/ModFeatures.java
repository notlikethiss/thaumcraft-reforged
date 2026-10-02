package thaumcraft.registry;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.world.gen.ThaumcraftWorldGenFeature;

public final class ModFeatures {
    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURE_TYPES = DeferredRegister.create(Registries.FEATURE_TYPE, Thaumcraft.MODID);

    public static final Supplier<MapCodec<ThaumcraftWorldGenFeature>> WORLD_GENERATION = FEATURE_TYPES.register("world_generation", () -> ThaumcraftWorldGenFeature.CODEC);

    private ModFeatures() {
    }

    public static void register(IEventBus bus) {
        FEATURE_TYPES.register(bus);
    }
}
