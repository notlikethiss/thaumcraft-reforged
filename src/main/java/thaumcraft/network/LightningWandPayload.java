package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static final StreamCodec<ByteBuf, LightningWandPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, LightningWandPayload::playerId,
        ByteBufCodecs.DOUBLE, LightningWandPayload::endX,
        ByteBufCodecs.DOUBLE, LightningWandPayload::endY,
        ByteBufCodecs.DOUBLE, LightningWandPayload::endZ,
        ByteBufCodecs.BOOL, LightningWandPayload::hasBlock,
        ByteBufCodecs.DOUBLE, LightningWandPayload::blockX,
        ByteBufCodecs.DOUBLE, LightningWandPayload::blockY,
        ByteBufCodecs.DOUBLE, LightningWandPayload::blockZ,
        ByteBufCodecs.BOOL, LightningWandPayload::hasEntity,
        LightningWandPayload::new
    );

    @Override
    public Type<LightningWandPayload> type() {
        return TYPE;
    }
}
