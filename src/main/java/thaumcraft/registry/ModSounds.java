package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Thaumcraft.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BRAIN = register("brain");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = register("bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL = register("crystal");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOORFAIL = register("doorfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> DW = register("dw");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRELOOP = register("fireloop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEMIRON = register("golemiron");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEMIRONSHOOT = register("golemironshoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEMSTONE = register("golemstone");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOLEMWOOD = register("golemwood");
    public static final DeferredHolder<SoundEvent, SoundEvent> HHOFF = register("hhoff");
    public static final DeferredHolder<SoundEvent, SoundEvent> HHON = register("hhon");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE = register("ice");
    public static final DeferredHolder<SoundEvent, SoundEvent> JACOBS = register("jacobs");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAR = register("jar");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEY = register("key");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEARN = register("learn");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAGE = register("page");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUMBLE = register("rumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK = register("shock");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPILL = register("spill");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWING = register("swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOOL = register("tool");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND = register("wand");
    public static final DeferredHolder<SoundEvent, SoundEvent> WANDFAIL = register("wandfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIND = register("wind");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISPDEAD = register("wispdead");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISPLIVE = register("wisplive");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP = register("zap");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Thaumcraft.id(name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
