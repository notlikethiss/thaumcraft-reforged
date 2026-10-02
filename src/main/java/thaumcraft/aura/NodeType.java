package thaumcraft.aura;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum NodeType implements StringRepresentable {
    NORMAL,
    PURE,
    DARK,
    UNSTABLE;

    public static final Codec<NodeType> CODEC = StringRepresentable.fromEnum(NodeType::values);
    public static final StreamCodec<ByteBuf, NodeType> STREAM_CODEC = ByteBufCodecs.idMapper(NodeType::byId, NodeType::ordinal);

    public static NodeType byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : NORMAL;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
