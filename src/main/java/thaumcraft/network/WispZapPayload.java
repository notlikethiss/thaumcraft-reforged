package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record WispZapPayload(int sourceId, int targetId) implements CustomPacketPayload {
    public static final Type<WispZapPayload> TYPE = new Type<>(Thaumcraft.id("wisp_zap"));
    public static final StreamCodec<ByteBuf, WispZapPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WispZapPayload::sourceId,
        ByteBufCodecs.VAR_INT, WispZapPayload::targetId,
        WispZapPayload::new
    );

    @Override
    public Type<WispZapPayload> type() {
        return TYPE;
    }
}
