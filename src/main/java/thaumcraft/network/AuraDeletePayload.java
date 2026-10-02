package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record AuraDeletePayload(int key) implements CustomPacketPayload {
    public static final Type<AuraDeletePayload> TYPE = new Type<>(Thaumcraft.id("aura_delete"));
    public static final StreamCodec<ByteBuf, AuraDeletePayload> STREAM_CODEC = ByteBufCodecs.INT.map(AuraDeletePayload::new, AuraDeletePayload::key);

    @Override
    public Type<AuraDeletePayload> type() {
        return TYPE;
    }
}
