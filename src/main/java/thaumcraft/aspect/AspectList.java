package thaumcraft.aspect;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class AspectList {
    public static final Codec<AspectList> CODEC = Codec.unboundedMap(Aspect.CODEC, Codec.INT)
        .xmap(AspectList::new, list -> list.aspects);
    public static final StreamCodec<ByteBuf, AspectList> STREAM_CODEC = ByteBufCodecs.<ByteBuf, Aspect, Integer, Map<Aspect, Integer>>map(
            LinkedHashMap::new, Aspect.STREAM_CODEC, ByteBufCodecs.VAR_INT
        )
        .map(AspectList::new, list -> list.aspects);
    public static final AspectList EMPTY = new AspectList();

    private final Map<Aspect, Integer> aspects;

    public AspectList() {
        this.aspects = new LinkedHashMap<>();
    }

    private AspectList(Map<Aspect, Integer> aspects) {
        this.aspects = new LinkedHashMap<>(aspects);
    }

    public AspectList copy() {
        return new AspectList(aspects);
    }

    public int size() {
        return aspects.size();
    }

    public boolean isEmpty() {
        return aspects.isEmpty();
    }

    public List<Aspect> getAspects() {
        return new ArrayList<>(aspects.keySet());
    }

    public List<Aspect> getAspectsSorted() {
        List<Aspect> sorted = getAspects();
        sorted.sort(Comparator.comparing(aspect -> aspect.latinName));
        return sorted;
    }

    public List<Aspect> getAspectsSortedAmount() {
        List<Aspect> sorted = getAspects();
        sorted.sort(Comparator.comparingInt(this::getAmount).reversed());
        return sorted;
    }

    public int getAmount(Aspect aspect) {
        Integer amount = aspects.get(aspect);
        return amount == null ? 0 : amount;
    }

    public int visSize() {
        int total = 0;
        for (int amount : aspects.values()) {
            total += amount;
        }
        return total;
    }

    public boolean reduceAmount(Aspect aspect, int amount) {
        if (getAmount(aspect) < amount) {
            return false;
        }
        int remaining = getAmount(aspect) - amount;
        if (remaining <= 0) {
            aspects.remove(aspect);
        } else {
            aspects.put(aspect, remaining);
        }
        return true;
    }

    public AspectList remove(Aspect aspect, int amount) {
        int current = getAmount(aspect);
        if (current >= amount) {
            int remaining = current - amount;
            if (remaining <= 0) {
                aspects.remove(aspect);
            } else {
                aspects.put(aspect, remaining);
            }
        } else if (current == 0) {
            aspects.put(aspect, -amount);
        }
        return this;
    }

    public AspectList removeAll(Aspect aspect) {
        aspects.remove(aspect);
        return this;
    }

    public AspectList add(Aspect aspect, int amount) {
        aspects.merge(aspect, amount, Integer::sum);
        return this;
    }

    public AspectList add(AspectList other) {
        for (Map.Entry<Aspect, Integer> entry : other.aspects.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public AspectList merge(Aspect aspect, int amount) {
        aspects.merge(aspect, amount, Math::max);
        return this;
    }

    public AspectList cull(int limit) {
        while (aspects.size() > limit) {
            Aspect lowest = null;
            int low = Integer.MAX_VALUE;
            for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
                if (entry.getValue() < low) {
                    low = entry.getValue();
                    lowest = entry.getKey();
                }
            }
            aspects.remove(lowest);
        }
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AspectList list && list.aspects.equals(aspects);
    }

    @Override
    public int hashCode() {
        return aspects.hashCode();
    }

    @Override
    public String toString() {
        return aspects.toString();
    }
}
