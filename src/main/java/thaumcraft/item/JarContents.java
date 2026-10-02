package thaumcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.Aspect;

public record JarContents(Aspect aspect, int amount) {
    public static final Codec<JarContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Aspect.CODEC.fieldOf("aspect").forGetter(JarContents::aspect),
        Codec.INT.fieldOf("amount").forGetter(JarContents::amount)
    ).apply(instance, JarContents::new));
    public static final StreamCodec<ByteBuf, JarContents> STREAM_CODEC = StreamCodec.composite(
        Aspect.STREAM_CODEC,
        JarContents::aspect,
        ByteBufCodecs.VAR_INT,
        JarContents::amount,
        JarContents::new
    );
}
