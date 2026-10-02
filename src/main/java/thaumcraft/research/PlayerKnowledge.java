package thaumcraft.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class PlayerKnowledge {
    public static final MapCodec<PlayerKnowledge> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.listOf().optionalFieldOf("research", List.of()).forGetter(data -> List.copyOf(data.research)),
        Codec.STRING.listOf().optionalFieldOf("objects", List.of()).forGetter(data -> List.copyOf(data.objects)),
        Codec.STRING.listOf().optionalFieldOf("entities", List.of()).forGetter(data -> List.copyOf(data.entities)),
        Codec.STRING.listOf().optionalFieldOf("phenomena", List.of()).forGetter(data -> List.copyOf(data.phenomena))
    ).apply(instance, PlayerKnowledge::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), data -> List.copyOf(data.research),
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), data -> List.copyOf(data.objects),
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), data -> List.copyOf(data.entities),
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), data -> List.copyOf(data.phenomena),
        PlayerKnowledge::new
    );

    private final Set<String> research = new LinkedHashSet<>();
    private final Set<String> objects = new LinkedHashSet<>();
    private final Set<String> entities = new LinkedHashSet<>();
    private final Set<String> phenomena = new LinkedHashSet<>();

    public PlayerKnowledge() {
    }

    private PlayerKnowledge(List<String> research, List<String> objects, List<String> entities, List<String> phenomena) {
        this.research.addAll(research);
        this.objects.addAll(objects);
        this.entities.addAll(entities);
        this.phenomena.addAll(phenomena);
    }

    public boolean isComplete(String key) {
        return research.contains(key);
    }

    public boolean complete(String key) {
        return research.add(key);
    }

    public Set<String> research() {
        return research;
    }

    public Set<String> objects() {
        return objects;
    }

    public Set<String> entities() {
        return entities;
    }

    public Set<String> phenomena() {
        return phenomena;
    }
}
