package thaumcraft.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.Aspect;

public record ResearchNote(String key, List<Aspect> tags, List<Integer> progress, List<Integer> failed) {
    public static final Codec<ResearchNote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(ResearchNote::key),
        Aspect.CODEC.listOf().fieldOf("tags").forGetter(ResearchNote::tags),
        Codec.INT.listOf().fieldOf("progress").forGetter(ResearchNote::progress),
        Codec.INT.listOf().optionalFieldOf("failed", List.of()).forGetter(ResearchNote::failed)
    ).apply(instance, ResearchNote::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchNote> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ResearchNote::key,
        Aspect.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchNote::tags,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ResearchNote::progress,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ResearchNote::failed,
        ResearchNote::new
    );

    public ResearchNote {
        tags = List.copyOf(tags);
        progress = List.copyOf(progress);
        failed = List.copyOf(failed);
    }
}
