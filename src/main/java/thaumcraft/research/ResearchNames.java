package thaumcraft.research;

import java.util.function.Function;

public final class ResearchNames {
    private static Function<String, String> resolver = key -> key;

    private ResearchNames() {
    }

    public static void setResolver(Function<String, String> newResolver) {
        resolver = newResolver;
    }

    public static String name(String key) {
        return resolver.apply(key);
    }
}
