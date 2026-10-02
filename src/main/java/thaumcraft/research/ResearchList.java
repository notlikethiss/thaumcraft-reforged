package thaumcraft.research;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;

public final class ResearchList {
    public static final Map<String, ResearchItem> RESEARCH = new LinkedHashMap<>();
    public static final Map<String, List<String>> CRAFTING_RECIPES_FOR_RESEARCH = new HashMap<>();
    public static int minDisplayColumn;
    public static int minDisplayRow;
    public static int maxDisplayColumn;
    public static int maxDisplayRow;
    private static boolean initialized;

    private ResearchList() {
    }

    public static synchronized void init() {
        if (!initialized) {
            initialized = true;
            ConfigResearch.registerAll();
        }
    }

    static void updateBounds(int column, int row) {
        minDisplayColumn = Math.min(minDisplayColumn, column);
        minDisplayRow = Math.min(minDisplayRow, row);
        maxDisplayColumn = Math.max(maxDisplayColumn, column);
        maxDisplayRow = Math.max(maxDisplayRow, row);
    }

    public static @Nullable ResearchItem getResearch(String key) {
        return RESEARCH.get(key);
    }

    public static Aspect @Nullable [] getResearchTags(String key) {
        ResearchItem research = RESEARCH.get(key);
        return research == null ? null : research.tags.getAspects().toArray(new Aspect[0]);
    }

    public static Aspect getResearchPrimaryTag(String key) {
        ResearchItem research = RESEARCH.get(key);
        Aspect best = Aspect.WIND;
        int amount = 0;
        if (research == null) {
            return best;
        }
        for (Aspect aspect : research.tags.getAspects()) {
            if (research.tags.getAmount(aspect) > amount) {
                best = aspect;
                amount = research.tags.getAmount(aspect);
            }
        }
        return best;
    }

    public static int getResearchAmount(String key, Aspect aspect) {
        ResearchItem research = RESEARCH.get(key);
        return research == null ? 0 : research.tags.getAmount(aspect);
    }
}
