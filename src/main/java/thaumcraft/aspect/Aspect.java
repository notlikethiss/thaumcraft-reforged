package thaumcraft.aspect;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum Aspect implements StringRepresentable {
    UNKNOWN(63, "Obscurus", 1, false, 0x282828),
    WIND(0, "Aura", 1, false, 0xC0C0D7),
    FIRE(6, "Ignis", 2, true, 0xFF5A01),
    ROCK(11, "Saxum", 4, false, 0x5C4842),
    WATER(21, "Aqua", 3, false, 0x3CD4FC),
    PLANT(36, "Herba", 4, false, 0x01AC00),
    BEAST(53, "Bestia", 3, true, 0x9F6409),
    FLESH(54, "Corpus", 3, false, 0xEE478D),
    METAL(12, "Metallum", 4, false, 0xD8D8D8),
    CRYSTAL(20, "Vitreus", 1, false, 0x80FFFF),
    SOUND(24, "Sonus", 3, false, 0x10C9C0),
    EXCHANGE(14, "Permutatio", 4, false, 0x578357),
    LIFE(26, "Victus", 3, false, 0xDE0005),
    DEATH(27, "Mortuus", 6, true, 0x404040),
    SPIRIT(30, "Animus", 1, false, 0xE0E0E0),
    VOID(2, "Vacuos", 1, true, 0xC0C0D7),
    VISION(3, "Visum", 3, false, 0xD5D4EC),
    KNOWLEDGE(4, "Cognitio", 1, false, 0x8080EE),
    DESTRUCTION(7, "Fractus", 2, true, 0x506050),
    POWER(9, "Potentia", 2, true, 0xC0FFFF),
    MECHANISM(10, "Machina", 2, false, 0x8080A0),
    ARMOR(17, "Tutamen", 4, false, 0x00C0C0),
    WEAPON(18, "Telum", 2, true, 0xC05050),
    TOOL(19, "Instrumentum", 4, false, 0x4040EE),
    POISON(29, "Venenum", 3, true, 0x89F000),
    VALUABLE(31, "Carus", 4, false, 0xE6BE44),
    PURE(37, "Purus", 3, false, 0xA5FFFD),
    MAGIC(40, "Praecantatio", 5, false, 0x9700C0),
    TIME(41, "Tempus", 5, false, 0x9070E0),
    CONTROL(48, "Imperito", 5, false, 0x98994B),
    DARK(49, "Tenebris", 5, true, 0x252525),
    CRAFT(50, "Fabrico", 2, false, 0x809D80),
    EVIL(56, "Malum", 5, true, 0x700000),
    FLUX(57, "Mutatio", 5, true, 0xB80BB9),
    ELDRITCH(58, "Alienis", 5, true, 0x805080),
    TRAP(28, "Vinculum", 4, true, 0x9A8080),
    INSECT(55, "Bestiola", 3, false, 0x808880),
    MOTION(1, "Motus", 1, false, 0xCDCCF4),
    FLIGHT(5, "Volito", 1, false, 0xE7E7D7),
    LIGHT(8, "Lux", 2, false, 0xFFF663),
    CLOTH(15, "Pannus", 3, false, 0xEAEAC2),
    EARTH(16, "Solum", 4, false, 0x713F2D),
    WEATHER(22, "Aer", 3, false, 0xC0FFFF),
    COLD(23, "Gelum", 3, true, 0xE1FFFF),
    HEAL(25, "Sano", 3, false, 0xFF8184),
    WOOD(32, "Lignum", 4, false, 0x058105),
    FLOWER(33, "Flos", 4, false, 0xFFFF40),
    FUNGUS(34, "Fungus", 4, false, 0xF7E5C7),
    CROP(35, "Messis", 4, false, 0xE3FF80);

    private static final Map<Integer, Aspect> BY_ID = new HashMap<>();
    public static final StringRepresentable.EnumCodec<Aspect> CODEC = StringRepresentable.fromEnum(Aspect::values);
    public static final StreamCodec<ByteBuf, Aspect> STREAM_CODEC = ByteBufCodecs.idMapper(Aspect::get, Aspect::getId);

    public final int id;
    public final String latinName;
    public final int element;
    public final boolean aggro;
    public final int color;

    Aspect(int id, String latinName, int element, boolean aggro, int color) {
        this.id = id;
        this.latinName = latinName;
        this.element = element;
        this.aggro = aggro;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public static Aspect get(int id) {
        Aspect aspect = BY_ID.get(id);
        return aspect != null ? aspect : FLUX;
    }

    public Component getDisplayName() {
        return Component.literal(latinName);
    }

    public Component getMeaning() {
        return Component.translatable("aspect.thaumcraft." + getSerializedName() + ".meaning");
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    static {
        for (Aspect aspect : values()) {
            BY_ID.put(aspect.id, aspect);
        }
    }
}
