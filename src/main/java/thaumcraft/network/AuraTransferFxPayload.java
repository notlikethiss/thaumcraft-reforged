package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3fc;
import thaumcraft.Thaumcraft;

public record AuraTransferFxPayload(Vector3fc from, Vector3fc to) implements CustomPacketPayload {
    public static final Type<AuraTransferFxPayload> TYPE = new Type<>(Thaumcraft.id("aura_transfer_fx"));
    public static final StreamCodec<ByteBuf, AuraTransferFxPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VECTOR3F, AuraTransferFxPayload::from,
        ByteBufCodecs.VECTOR3F, AuraTransferFxPayload::to,
        AuraTransferFxPayload::new
    );

    @Override
    public Type<AuraTransferFxPayload> type() {
        return TYPE;
    }
}
