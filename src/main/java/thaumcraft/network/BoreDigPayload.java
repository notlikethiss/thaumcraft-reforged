package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BoreDigPayload(BlockPos pos, BlockPos target) implements CustomPacketPayload {
    public static final Type<BoreDigPayload> TYPE = new Type<>(Thaumcraft.id("bore_dig"));
    public static final StreamCodec<ByteBuf, BoreDigPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BoreDigPayload::pos,
        BlockPos.STREAM_CODEC, BoreDigPayload::target,
        BoreDigPayload::new
    );

    @Override
    public Type<BoreDigPayload> type() {
        return TYPE;
    }
}
