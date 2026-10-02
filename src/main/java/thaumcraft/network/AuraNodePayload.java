package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraNode;
import thaumcraft.aura.NodeType;

public record AuraNodePayload(int key, float x, float y, float z, short level, short baseLevel, short flux, boolean locked, NodeType nodeType)
    implements CustomPacketPayload {
    public static final Type<AuraNodePayload> TYPE = new Type<>(Thaumcraft.id("aura_node"));
    public static final StreamCodec<ByteBuf, AuraNodePayload> STREAM_CODEC = StreamCodec.of(
        (buffer, payload) -> {
            FriendlyByteBuf out = new FriendlyByteBuf(buffer);
            out.writeInt(payload.key);
            out.writeFloat(payload.x);
            out.writeFloat(payload.y);
            out.writeFloat(payload.z);
            out.writeShort(payload.level);
            out.writeShort(payload.baseLevel);
            out.writeShort(payload.flux);
            out.writeBoolean(payload.locked);
            out.writeByte(payload.nodeType.ordinal());
        },
        buffer -> {
            FriendlyByteBuf in = new FriendlyByteBuf(buffer);
            return new AuraNodePayload(
                in.readInt(),
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readShort(),
                in.readShort(),
                in.readShort(),
                in.readBoolean(),
                NodeType.byId(in.readByte())
            );
        }
    );

    public static AuraNodePayload of(AuraNode node) {
        return new AuraNodePayload(
            node.key,
            (float) node.x,
            (float) node.y,
            (float) node.z,
            (short) node.level,
            (short) node.baseLevel,
            (short) node.fluxTotal(),
            node.locked,
            node.type
        );
    }

    @Override
    public Type<AuraNodePayload> type() {
        return TYPE;
    }
}
