package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.research.PlayerKnowledge;

public record KnowledgeSyncPayload(PlayerKnowledge knowledge) implements CustomPacketPayload {
    public static final Type<KnowledgeSyncPayload> TYPE = new Type<>(Thaumcraft.id("knowledge_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KnowledgeSyncPayload> STREAM_CODEC = StreamCodec.composite(
        PlayerKnowledge.STREAM_CODEC, KnowledgeSyncPayload::knowledge,
        KnowledgeSyncPayload::new
    );

    @Override
    public Type<KnowledgeSyncPayload> type() {
        return TYPE;
    }
}
