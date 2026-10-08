package thaumcraft.world.gen;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
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
    private static final int MAX_PER_VILLAGE = 2;
    private static final Map<String, String> BIOMES = Map.of(
        "plains", "mossify_10_percent",
        "taiga", "mossify_10_percent",
        "desert", "empty",
        "savanna", "empty",
        "snowy", "empty"
    );

    private static volatile Set<StructurePoolElement> towers = Set.of();

    private VillageTowers() {
    }

    public static boolean isTower(StructurePoolElement element) {
        return towers.contains(element);
    }

    public static boolean limitReached(List<?> pieces) {
        int count = 0;
        for (Object piece : pieces) {
            if (piece instanceof PoolElementStructurePiece poolPiece && isTower(poolPiece.getElement()) && ++count >= MAX_PER_VILLAGE) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorLists = event.getServer().registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
        Set<StructurePoolElement> elements = Collections.newSetFromMap(new IdentityHashMap<>());
        BIOMES.forEach((biome, processorName) -> {
            StructureTemplatePool pool = pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.withDefaultNamespace("village/" + biome + "/houses")));
            String location = Thaumcraft.MODID + ":village/" + biome + "/wizard_tower";
            for (Pair<StructurePoolElement, Integer> entry : pool.rawTemplates) {
                if (entry.getFirst() instanceof LegacySinglePoolElement single && single.toString().contains(location)) {
                    elements.add(single);
                    return;
                }
            }
            Holder<StructureProcessorList> processors = processorLists.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace(processorName)));
            StructurePoolElement element = StructurePoolElement.legacy(location, processors).apply(StructureTemplatePool.Projection.RIGID);
            List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>(pool.rawTemplates);
            raw.add(Pair.of(element, WEIGHT));
            pool.rawTemplates = raw;
            elements.add(element);
            for (int index = 0; index < WEIGHT; index++) {
                pool.templates.add(element);
            }
        });
        towers = elements;
    }
}
