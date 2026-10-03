package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record BlockBoilPayload(BlockPos pos, float red, float green, float blue) implements CustomPacketPayload {
    public static final Type<BlockBoilPayload> TYPE = new Type<>(Thaumcraft.id("block_boil"));
    public static final StreamCodec<ByteBuf, BlockBoilPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlockBoilPayload::pos,
        ByteBufCodecs.FLOAT, BlockBoilPayload::red,
        ByteBufCodecs.FLOAT, BlockBoilPayload::green,
        ByteBufCodecs.FLOAT, BlockBoilPayload::blue,
        BlockBoilPayload::new
    );

    @Override
    public Type<BlockBoilPayload> type() {
        return TYPE;
    }
}
