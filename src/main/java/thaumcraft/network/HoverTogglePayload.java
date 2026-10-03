package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.item.armor.Hover;

public record HoverTogglePayload(boolean hover) implements CustomPacketPayload {
    public static final Type<HoverTogglePayload> TYPE = new Type<>(Thaumcraft.id("hover_toggle"));
    public static final StreamCodec<ByteBuf, HoverTogglePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, HoverTogglePayload::hover,
        HoverTogglePayload::new
    );

    public static void handle(HoverTogglePayload payload, IPayloadContext context) {
        ItemStack armor = Hover.getHarness(context.player());
        if (!armor.isEmpty()) {
            Hover.setHovering(armor, payload.hover());
        }
    }

    @Override
    public Type<HoverTogglePayload> type() {
        return TYPE;
    }
}
