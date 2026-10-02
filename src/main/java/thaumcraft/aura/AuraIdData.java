package thaumcraft.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import thaumcraft.Thaumcraft;

public final class AuraIdData extends SavedData {
    public static final Codec<AuraIdData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("last_id", -1).forGetter(data -> data.lastId)
    ).apply(instance, AuraIdData::new));
    public static final SavedDataType<AuraIdData> TYPE = new SavedDataType<>(Thaumcraft.id("node_ids"), AuraIdData::new, CODEC);

    private int lastId;

    public AuraIdData() {
        this(-1);
    }

    private AuraIdData(int lastId) {
        this.lastId = lastId;
    }

    public static AuraIdData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int nextId() {
        lastId++;
        setDirty();
        return lastId;
    }
}
