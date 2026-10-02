package thaumcraft.network;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;

public record AspectTagsPayload(Map<Item, AspectList> tags) implements CustomPacketPayload {
    public static final Type<AspectTagsPayload> TYPE = new Type<>(Thaumcraft.id("aspect_tags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectTagsPayload> STREAM_CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, Item, AspectList, Map<Item, AspectList>>map(
            LinkedHashMap::new,
            ByteBufCodecs.registry(Registries.ITEM),
            AspectList.STREAM_CODEC.cast()
        )
        .map(AspectTagsPayload::new, AspectTagsPayload::tags);

    @Override
    public Type<AspectTagsPayload> type() {
        return TYPE;
    }
}
