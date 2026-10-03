package thaumcraft.registry;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModVillagers {
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Thaumcraft.MODID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Thaumcraft.MODID);

    public static final DeferredHolder<PoiType, PoiType> ARCANE_WORKTABLE = POI_TYPES.register(
        "arcane_worktable",
        () -> new PoiType(ImmutableSet.copyOf(ModBlocks.ARCANE_WORKTABLE.get().getStateDefinition().getPossibleStates()), 1, 1)
    );

    public static final DeferredHolder<VillagerProfession, VillagerProfession> WIZARD = PROFESSIONS.register("wizard", () -> {
        Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            tradeSets.put(level, ResourceKey.create(Registries.TRADE_SET, Thaumcraft.id("wizard/level_" + level)));
        }
        return new VillagerProfession(
            Component.translatable("entity.thaumcraft.villager.wizard"),
            holder -> holder.is(ARCANE_WORKTABLE.getKey()),
            holder -> holder.is(ARCANE_WORKTABLE.getKey()),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN,
            tradeSets
        );
    });

    private ModVillagers() {
    }

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        PROFESSIONS.register(bus);
    }
}
