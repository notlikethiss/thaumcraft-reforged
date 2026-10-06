package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record LightningWandPayload(
    int playerId,
    double endX,
    double endY,
    double endZ,
    boolean hasBlock,
    double blockX,
    double blockY,
    double blockZ,
    boolean hasEntity
) implements CustomPacketPayload {
    public static final Type<LightningWandPayload> TYPE = new Type<>(Thaumcraft.id("lightning_wand"));
    public static final StreamCodec<ByteBuf, LightningWandPayload> STREAM_CODEC = StreamCodec.of(
        LightningWandPayload::write,
        LightningWandPayload::read
    );

    private static void write(ByteBuf buffer, LightningWandPayload payload) {
        VarInt.write(buffer, payload.playerId);
        buffer.writeDouble(payload.endX);
        buffer.writeDouble(payload.endY);
        buffer.writeDouble(payload.endZ);
        buffer.writeBoolean(payload.hasBlock);
        buffer.writeDouble(payload.blockX);
        buffer.writeDouble(payload.blockY);
        buffer.writeDouble(payload.blockZ);
        buffer.writeBoolean(payload.hasEntity);
    }

    private static LightningWandPayload read(ByteBuf buffer) {
        return new LightningWandPayload(
            VarInt.read(buffer),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean()
        );
    }

    @Override
    public Type<LightningWandPayload> type() {
        return TYPE;
    }
}
