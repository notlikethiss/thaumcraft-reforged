package thaumcraft.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class TcFonts {
    private static final Style GALACTIC_STYLE = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));
    private static final Style UNIFORM_STYLE = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("uniform"));

    private TcFonts() {
    }

    public static MutableComponent galactic(String text) {
        return Component.literal(text).withStyle(GALACTIC_STYLE);
    }

    public static MutableComponent uniform(String text) {
        return Component.literal(text).withStyle(UNIFORM_STYLE);
    }

    public static MutableComponent text(String text, boolean galactic) {
        return galactic ? galactic(text) : Component.literal(text);
    }
}
