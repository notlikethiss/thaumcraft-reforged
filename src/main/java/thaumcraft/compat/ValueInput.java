package thaumcraft.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public final class ValueInput {
    private final CompoundTag tag;
    private final HolderLookup.Provider provider;

    private ValueInput(CompoundTag tag, HolderLookup.Provider provider) {
        this.tag = tag;
        this.provider = provider;
    }

    public static ValueInput of(CompoundTag tag, HolderLookup.Provider provider) {
        return new ValueInput(tag, provider);
    }

    private DynamicOps<Tag> ops() {
        return provider.createSerializationContext(NbtOps.INSTANCE);
    }

    public CompoundTag tag() {
        return tag;
    }

    public <T> Optional<T> read(String name, Codec<T> codec) {
        Tag value = tag.get(name);
        if (value == null) {
            return Optional.empty();
        }
        return codec.parse(ops(), value).result();
    }

    public <T> Optional<T> read(MapCodec<T> codec) {
        return codec.codec().parse(ops(), tag).result();
    }

    public Optional<ValueInput> child(String name) {
        if (tag.contains(name, Tag.TAG_COMPOUND)) {
            return Optional.of(new ValueInput(tag.getCompound(name), provider));
        }
        return Optional.empty();
    }

    public ValueInput childOrEmpty(String name) {
        return child(name).orElseGet(() -> new ValueInput(new CompoundTag(), provider));
    }

    public ValueInput rawChildOrEmpty(String name) {
        return childOrEmpty(name);
    }

    public Optional<ValueInputList> childrenList(String name) {
        if (!tag.contains(name, Tag.TAG_LIST)) {
            return Optional.empty();
        }
        ListTag list = (ListTag) tag.get(name);
        List<ValueInput> children = new ArrayList<>();
        for (Tag element : list) {
            if (element instanceof CompoundTag compound) {
                children.add(new ValueInput(compound, provider));
            }
        }
        return Optional.of(new ValueInputList(children));
    }

    public ValueInputList childrenListOrEmpty(String name) {
        return childrenList(name).orElseGet(() -> new ValueInputList(List.of()));
    }

    public <T> Optional<TypedInputList<T>> list(String name, Codec<T> codec) {
        if (!tag.contains(name, Tag.TAG_LIST)) {
            return Optional.empty();
        }
        ListTag list = (ListTag) tag.get(name);
        List<T> values = new ArrayList<>();
        DynamicOps<Tag> ops = ops();
        for (Tag element : list) {
            codec.parse(ops, element).result().ifPresent(values::add);
        }
        return Optional.of(new TypedInputList<>(values));
    }

    public <T> TypedInputList<T> listOrEmpty(String name, Codec<T> codec) {
        return list(name, codec).orElseGet(() -> new TypedInputList<>(List.of()));
    }

    public boolean getBooleanOr(String name, boolean defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(name) : defaultValue;
    }

    public byte getByteOr(String name, byte defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getByte(name) : defaultValue;
    }

    public int getShortOr(String name, short defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getShort(name) : defaultValue;
    }

    public Optional<Integer> getInt(String name) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? Optional.of(tag.getInt(name)) : Optional.empty();
    }

    public int getIntOr(String name, int defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getInt(name) : defaultValue;
    }

    public long getLongOr(String name, long defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getLong(name) : defaultValue;
    }

    public Optional<Long> getLong(String name) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? Optional.of(tag.getLong(name)) : Optional.empty();
    }

    public float getFloatOr(String name, float defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getFloat(name) : defaultValue;
    }

    public double getDoubleOr(String name, double defaultValue) {
        return tag.contains(name, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(name) : defaultValue;
    }

    public Optional<String> getString(String name) {
        return tag.contains(name, Tag.TAG_STRING) ? Optional.of(tag.getString(name)) : Optional.empty();
    }

    public String getStringOr(String name, String defaultValue) {
        return tag.contains(name, Tag.TAG_STRING) ? tag.getString(name) : defaultValue;
    }

    public Optional<int[]> getIntArray(String name) {
        return tag.contains(name, Tag.TAG_INT_ARRAY) ? Optional.of(tag.getIntArray(name)) : Optional.empty();
    }

    public Set<String> keySet() {
        return tag.getAllKeys();
    }

    public HolderLookup.Provider lookup() {
        return provider;
    }

    public static final class TypedInputList<T> implements Iterable<T> {
        private final List<T> values;

        private TypedInputList(List<T> values) {
            this.values = values;
        }

        public boolean isEmpty() {
            return values.isEmpty();
        }

        public Stream<T> stream() {
            return values.stream();
        }

        @Override
        public Iterator<T> iterator() {
            return values.iterator();
        }
    }

    public static final class ValueInputList implements Iterable<ValueInput> {
        private final List<ValueInput> values;

        private ValueInputList(List<ValueInput> values) {
            this.values = values;
        }

        public boolean isEmpty() {
            return values.isEmpty();
        }

        public Stream<ValueInput> stream() {
            return values.stream();
        }

        @Override
        public Iterator<ValueInput> iterator() {
            return values.iterator();
        }
    }
}
