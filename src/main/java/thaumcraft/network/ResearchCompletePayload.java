package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record ResearchCompletePayload(String key) implements CustomPacketPayload {
    public static final Type<ResearchCompletePayload> TYPE = new Type<>(Thaumcraft.id("research_complete"));
    public static final StreamCodec<ByteBuf, ResearchCompletePayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ResearchCompletePayload::new, ResearchCompletePayload::key);

    @Override
    public Type<ResearchCompletePayload> type() {
        return TYPE;
    }
}
