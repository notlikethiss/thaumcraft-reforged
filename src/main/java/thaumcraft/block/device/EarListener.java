package thaumcraft.block.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class EarListener {
    private static final Map<ResourceKey<Level>, List<HeardNote>> NOTES = new HashMap<>();

    private EarListener() {
    }

    public static List<HeardNote> heard(Level level) {
        return NOTES.getOrDefault(level.dimension(), List.of());
    }

    @SubscribeEvent
    public static void onNotePlay(NoteBlockEvent.Play event) {
        if (event.getLevel() instanceof ServerLevel level) {
            NOTES.computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                .add(new HeardNote(event.getPos().immutable(), event.getInstrument(), event.getVanillaNoteId()));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            NOTES.remove(level.dimension());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            NOTES.remove(level.dimension());
        }
    }

    public record HeardNote(BlockPos pos, NoteBlockInstrument instrument, int note) {
    }
}
