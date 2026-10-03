package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BlockSparklePayload(BlockPos pos, int color, int count) implements CustomPacketPayload {
    public static final Type<BlockSparklePayload> TYPE = new Type<>(Thaumcraft.id("block_sparkle"));
    public static final StreamCodec<ByteBuf, BlockSparklePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlockSparklePayload::pos,
        ByteBufCodecs.VAR_INT, BlockSparklePayload::color,
        ByteBufCodecs.VAR_INT, BlockSparklePayload::count,
        BlockSparklePayload::new
    );

    @Override
    public Type<BlockSparklePayload> type() {
        return TYPE;
    }
}
