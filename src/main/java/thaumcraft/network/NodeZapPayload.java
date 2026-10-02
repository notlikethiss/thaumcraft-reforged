package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3fc;
import thaumcraft.Thaumcraft;

public record NodeZapPayload(Vector3fc from, int entityId) implements CustomPacketPayload {
    public static final Type<NodeZapPayload> TYPE = new Type<>(Thaumcraft.id("node_zap"));
    public static final StreamCodec<ByteBuf, NodeZapPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VECTOR3F, NodeZapPayload::from,
        ByteBufCodecs.VAR_INT, NodeZapPayload::entityId,
        NodeZapPayload::new
    );

    @Override
    public Type<NodeZapPayload> type() {
        return TYPE;
    }
}
