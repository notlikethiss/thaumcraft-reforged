package thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record KeyLink(BlockPos pos, int type) {
    public static final int DOOR = 0;
    public static final int PLATE = 1;

    public static final Codec<KeyLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(KeyLink::pos),
        Codec.INT.fieldOf("type").forGetter(KeyLink::type)
    ).apply(instance, KeyLink::new));
    public static final StreamCodec<ByteBuf, KeyLink> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        KeyLink::pos,
        ByteBufCodecs.VAR_INT,
        KeyLink::type,
        KeyLink::new
    );
}
