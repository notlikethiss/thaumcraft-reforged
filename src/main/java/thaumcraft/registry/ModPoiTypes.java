package thaumcraft.registry;

import com.google.common.collect.ImmutableSet;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModPoiTypes {
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Thaumcraft.MODID);

    public static final DeferredHolder<PoiType, PoiType> MARKER = POI_TYPES.register("marker", () -> {
        ImmutableSet.Builder<BlockState> states = ImmutableSet.builder();
        ModBlocks.MARKERS.values().forEach(block -> states.addAll(block.get().getStateDefinition().getPossibleStates()));
        return new PoiType(states.build(), 0, 1);
    });

    private ModPoiTypes() {
    }

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
    }
}
