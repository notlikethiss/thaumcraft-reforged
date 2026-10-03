package thaumcraft.world.gen;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class VillageTowers {
    private static final int WEIGHT = 2;
    private static final Map<String, String> BIOMES = Map.of(
        "plains", "mossify_10_percent",
        "taiga", "mossify_10_percent",
        "desert", "empty",
        "savanna", "empty",
        "snowy", "empty"
    );

    private VillageTowers() {
    }

    @SubscribeEvent
    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorLists = event.getServer().registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);
        BIOMES.forEach((biome, processorName) -> {
            StructureTemplatePool pool = pools.getValueOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("village/" + biome + "/houses")));
            String location = Thaumcraft.MODID + ":village/" + biome + "/wizard_tower";
            for (Pair<StructurePoolElement, Integer> entry : pool.rawTemplates) {
                if (entry.getFirst() instanceof LegacySinglePoolElement single && single.toString().contains(location)) {
                    return;
                }
            }
            Holder<StructureProcessorList> processors = processorLists.getOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.withDefaultNamespace(processorName)));
            StructurePoolElement element = StructurePoolElement.legacy(location, processors).apply(StructureTemplatePool.Projection.RIGID);
            List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>(pool.rawTemplates);
            raw.add(Pair.of(element, WEIGHT));
            pool.rawTemplates = raw;
            for (int index = 0; index < WEIGHT; index++) {
                pool.templates.add(element);
            }
        });
    }
}
