package thaumcraft.client.aura;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import thaumcraft.aura.NodeType;
import thaumcraft.network.AuraNodePayload;

public final class AuraClientData {
    public record ClientNode(int key, float x, float y, float z, ResourceKey<Level> dimension, short level, short baseLevel, short flux, boolean locked, NodeType type) {
    }

    public record History(short level, short flux) {
    }

    public static final Map<Integer, ClientNode> NODES = new HashMap<>();
    public static final Map<Integer, History> HISTORY = new HashMap<>();
    public static final Map<Integer, float[]> RENDER_POSITIONS = new HashMap<>();

    private AuraClientData() {
    }

    public static void update(AuraNodePayload payload, ResourceKey<Level> dimension) {
        ClientNode previous = NODES.get(payload.key());
        if (previous != null && HISTORY.containsKey(payload.key())) {
            HISTORY.put(payload.key(), new History(previous.level(), previous.flux()));
        }
        NODES.put(payload.key(), new ClientNode(
            payload.key(),
            payload.x(),
            payload.y(),
            payload.z(),
            dimension,
            payload.level(),
            payload.baseLevel(),
            payload.flux(),
            payload.locked(),
            payload.nodeType()
        ));
        HISTORY.putIfAbsent(payload.key(), new History(payload.level(), payload.flux()));
    }

    public static void remove(int key) {
        NODES.remove(key);
        HISTORY.remove(key);
        RENDER_POSITIONS.remove(key);
    }

    public static void clear() {
        NODES.clear();
        HISTORY.clear();
        RENDER_POSITIONS.clear();
    }
}
