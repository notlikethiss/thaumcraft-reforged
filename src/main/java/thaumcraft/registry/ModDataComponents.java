package thaumcraft.registry;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, Thaumcraft.MODID);

    public static final Supplier<DataComponentType<Aspect>> ESSENCE_ASPECT = COMPONENTS.registerComponentType(
        "essence_aspect",
        builder -> builder.persistent(Aspect.CODEC).networkSynchronized(Aspect.STREAM_CODEC.cast())
    );
    public static final Supplier<DataComponentType<Integer>> WAND_VIS = COMPONENTS.registerComponentType(
        "wand_vis",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );

    public static final Supplier<DataComponentType<Integer>> GOLEM_CORE = COMPONENTS.registerComponentType(
        "golem_core",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );

    private ModDataComponents() {
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
