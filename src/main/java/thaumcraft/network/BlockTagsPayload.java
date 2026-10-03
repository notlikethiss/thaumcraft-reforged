package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;

public record BlockTagsPayload(BlockPos pos, Direction side, AspectList aspects) implements CustomPacketPayload {
    public static final Type<BlockTagsPayload> TYPE = new Type<>(Thaumcraft.id("block_tags"));
    public static final StreamCodec<ByteBuf, BlockTagsPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlockTagsPayload::pos,
        Direction.STREAM_CODEC, BlockTagsPayload::side,
        AspectList.STREAM_CODEC, BlockTagsPayload::aspects,
        BlockTagsPayload::new
    );

    @Override
    public Type<BlockTagsPayload> type() {
        return TYPE;
    }
}
