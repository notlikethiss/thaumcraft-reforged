package thaumcraft.aura;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class AuraIdData extends SavedData {
    public static final String NAME = "thaumcraft_node_ids";
    public static final SavedData.Factory<AuraIdData> FACTORY = new SavedData.Factory<>(AuraIdData::new, (tag, provider) -> load(tag), null);

    private int lastId;

    public AuraIdData() {
        this(-1);
    }

    private AuraIdData(int lastId) {
        this.lastId = lastId;
    }

    private static AuraIdData load(CompoundTag tag) {
        return new AuraIdData(tag.contains("last_id") ? tag.getInt("last_id") : -1);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("last_id", lastId);
        return tag;
    }

    public static AuraIdData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public int nextId() {
        lastId++;
        setDirty();
        return lastId;
    }
}
