package thaumcraft.registry;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.JarContents;
import thaumcraft.item.KeyLink;
import thaumcraft.research.ResearchNote;

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
    public static final Supplier<DataComponentType<Integer>> HELLROD_CHARGES = COMPONENTS.registerComponentType(
        "hellrod_charges",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );
    public static final Supplier<DataComponentType<BlockState>> TRADE_BLOCK = COMPONENTS.registerComponentType(
        "trade_block",
        builder -> builder.persistent(BlockState.CODEC).networkSynchronized(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY).cast())
    );
    public static final Supplier<DataComponentType<Integer>> VIS_DISCOUNT = COMPONENTS.registerComponentType(
        "vis_discount",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );

    public static final Supplier<DataComponentType<Boolean>> HOVER = COMPONENTS.registerComponentType(
        "hover",
        builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL.cast())
    );
    public static final Supplier<DataComponentType<Integer>> HOVER_CHARGE = COMPONENTS.registerComponentType(
        "hover_charge",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );
    public static final Supplier<DataComponentType<ItemStack>> HARNESS_JAR = COMPONENTS.registerComponentType(
        "harness_jar",
        builder -> builder.persistent(ItemStack.OPTIONAL_CODEC).networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<JarContents>> JAR_CONTENTS = COMPONENTS.registerComponentType(
        "jar_contents",
        builder -> builder.persistent(JarContents.CODEC).networkSynchronized(JarContents.STREAM_CODEC.cast())
    );

    public static final Supplier<DataComponentType<Integer>> GOLEM_CORE = COMPONENTS.registerComponentType(
        "golem_core",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );
    public static final Supplier<DataComponentType<String>> GOLEM_DECORATION = COMPONENTS.registerComponentType(
        "golem_decoration",
        builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8.cast())
    );

    public static final Supplier<DataComponentType<ResearchNote>> RESEARCH_NOTE = COMPONENTS.registerComponentType(
        "research_note",
        builder -> builder.persistent(ResearchNote.CODEC).networkSynchronized(ResearchNote.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<KeyLink>> KEY_LINK = COMPONENTS.registerComponentType(
        "key_link",
        builder -> builder.persistent(KeyLink.CODEC).networkSynchronized(KeyLink.STREAM_CODEC.cast())
    );

    public static final Supplier<DataComponentType<Integer>> STORED_VIS = COMPONENTS.registerComponentType(
        "stored_vis",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT.cast())
    );

    public static final Supplier<DataComponentType<GlobalPos>> MIRROR_LINK = COMPONENTS.registerComponentType(
        "mirror_link",
        builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC)
    );

    private ModDataComponents() {
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
