package thaumcraft.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import thaumcraft.Thaumcraft;

public final class TcFonts {
    private static final FontDescription GALACTIC = new FontDescription.Resource(Thaumcraft.id("galactic"));
    private static final Style GALACTIC_STYLE = Style.EMPTY.withFont(GALACTIC);
    private static final Style UNIFORM_STYLE = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("uniform")));
    private static final String[] CYRILLIC = {
        "a", "b", "v", "g", "d", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p",
        "r", "s", "t", "u", "f", "kh", "ts", "ch", "sh", "sch", "", "y", "", "e", "yu", "ya"
    };

    private TcFonts() {
    }

    public static MutableComponent galactic(String text) {
        return Component.literal(transliterate(text)).withStyle(GALACTIC_STYLE);
    }

    public static MutableComponent uniform(String text) {
        return Component.literal(text).withStyle(UNIFORM_STYLE);
    }

    public static MutableComponent text(String text, boolean galactic) {
        return galactic ? galactic(text) : Component.literal(text);
    }

    private static String transliterate(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char symbol = text.charAt(i);
            if (symbol >= 'а' && symbol <= 'я') {
                builder.append(CYRILLIC[symbol - 'а']);
            } else if (symbol >= 'А' && symbol <= 'Я') {
                String latin = CYRILLIC[symbol - 'А'];
                if (!latin.isEmpty()) {
                    builder.append(Character.toUpperCase(latin.charAt(0))).append(latin, 1, latin.length());
                }
            } else if (symbol == 'ё') {
                builder.append('e');
            } else if (symbol == 'Ё') {
                builder.append('E');
            } else {
                builder.append(symbol);
            }
        }
        return builder.toString();
    }
}
