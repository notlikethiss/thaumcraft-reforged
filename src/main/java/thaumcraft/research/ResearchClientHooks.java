package thaumcraft.research;

import java.util.function.Function;

public final class ResearchClientHooks {
    private static Function<String, String> nameResolver = key -> key;
    private static Runnable bookOpener = () -> {
    };

    private ResearchClientHooks() {
    }

    public static void set(Function<String, String> names, Runnable book) {
        nameResolver = names;
        bookOpener = book;
    }

    public static String name(String key) {
        return nameResolver.apply(key);
    }

    public static void openBook() {
        bookOpener.run();
    }
}
