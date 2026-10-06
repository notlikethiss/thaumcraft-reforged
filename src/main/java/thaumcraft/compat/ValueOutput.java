package thaumcraft.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public final class ValueOutput {
    private final CompoundTag tag;
    private final HolderLookup.Provider provider;

    private ValueOutput(CompoundTag tag, HolderLookup.Provider provider) {
        this.tag = tag;
        this.provider = provider;
    }

    public static ValueOutput of(CompoundTag tag, HolderLookup.Provider provider) {
        return new ValueOutput(tag, provider);
    }

    private DynamicOps<Tag> ops() {
        return provider.createSerializationContext(NbtOps.INSTANCE);
    }

    public CompoundTag tag() {
        return tag;
    }

    public <T> void store(String name, Codec<T> codec, T value) {
        codec.encodeStart(ops(), value).result().ifPresent(encoded -> tag.put(name, encoded));
    }

    public <T> void storeNullable(String name, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            store(name, codec, value);
        }
    }

    public <T> void store(MapCodec<T> codec, T value) {
        codec.codec().encodeStart(ops(), value).result().ifPresent(encoded -> {
            if (encoded instanceof CompoundTag compound) {
                tag.merge(compound);
            }
        });
    }

    public void store(CompoundTag value) {
        tag.merge(value);
    }

    public void putBoolean(String name, boolean value) {
        tag.putBoolean(name, value);
    }

    public void putByte(String name, byte value) {
        tag.putByte(name, value);
    }

    public void putShort(String name, short value) {
        tag.putShort(name, value);
    }

    public void putInt(String name, int value) {
        tag.putInt(name, value);
    }

    public void putLong(String name, long value) {
        tag.putLong(name, value);
    }

    public void putFloat(String name, float value) {
        tag.putFloat(name, value);
    }

    public void putDouble(String name, double value) {
        tag.putDouble(name, value);
    }

    public void putString(String name, String value) {
        tag.putString(name, value);
    }

    public void putIntArray(String name, int[] value) {
        tag.putIntArray(name, value);
    }

    public ValueOutput child(String name) {
        CompoundTag child = new CompoundTag();
        tag.put(name, child);
        return new ValueOutput(child, provider);
    }

    public ValueOutputList childrenList(String name) {
        ListTag list = new ListTag();
        tag.put(name, list);
        return new ValueOutputList(list, provider);
    }

    public <T> TypedOutputList<T> list(String name, Codec<T> codec) {
        ListTag list = new ListTag();
        tag.put(name, list);
        return new TypedOutputList<>(list, codec, this);
    }

    public void discard(String name) {
        tag.remove(name);
    }

    public boolean isEmpty() {
        return tag.isEmpty();
    }

    public static final class TypedOutputList<T> {
        private final ListTag list;
        private final Codec<T> codec;
        private final ValueOutput owner;

        private TypedOutputList(ListTag list, Codec<T> codec, ValueOutput owner) {
            this.list = list;
            this.codec = codec;
            this.owner = owner;
        }

        public void add(T value) {
            codec.encodeStart(owner.ops(), value).result().ifPresent(list::add);
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }
    }

    public static final class ValueOutputList {
        private final ListTag list;
        private final HolderLookup.Provider provider;

        private ValueOutputList(ListTag list, HolderLookup.Provider provider) {
            this.list = list;
            this.provider = provider;
        }

        public ValueOutput addChild() {
            CompoundTag child = new CompoundTag();
            list.add(child);
            return new ValueOutput(child, provider);
        }

        public void discardLast() {
            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
            }
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }
    }
}
