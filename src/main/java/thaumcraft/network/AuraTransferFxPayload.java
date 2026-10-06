package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import thaumcraft.Thaumcraft;

public record AuraTransferFxPayload(Vector3fc from, Vector3fc to) implements CustomPacketPayload {
    public static final Type<AuraTransferFxPayload> TYPE = new Type<>(Thaumcraft.id("aura_transfer_fx"));
    private static final StreamCodec<ByteBuf, Vector3fc> VECTOR = ByteBufCodecs.VECTOR3F.map(vector -> (Vector3fc) vector, Vector3f::new);
    public static final StreamCodec<ByteBuf, AuraTransferFxPayload> STREAM_CODEC = StreamCodec.composite(
        VECTOR, AuraTransferFxPayload::from,
        VECTOR, AuraTransferFxPayload::to,
        AuraTransferFxPayload::new
    );

    @Override
    public Type<AuraTransferFxPayload> type() {
        return TYPE;
    }
}
