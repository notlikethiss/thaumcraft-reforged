package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectList;

public final class ThaumcraftRecipes {
    private static final List<CrucibleRecipe> CRUCIBLE = new ArrayList<>();
    private static final List<WorkbenchRecipe> WORKBENCH = new ArrayList<>();
    private static final Map<String, List<RecipeReference>> RESEARCH_RECIPES = new LinkedHashMap<>();
    private static final Map<String, String> SMELTING_BONUS = new HashMap<>();
    private static boolean initialized;

    private ThaumcraftRecipes() {
    }

    public static synchronized void init() {
        if (!initialized) {
            initialized = true;
            ConfigRecipes.registerAll();
        }
    }

    public static List<CrucibleRecipe> crucibleRecipes() {
        return CRUCIBLE;
    }

    public static List<WorkbenchRecipe> workbenchRecipes() {
        return WORKBENCH;
    }

    public static Map<String, List<RecipeReference>> researchRecipes() {
        return RESEARCH_RECIPES;
    }

    public static void addResearchRecipe(String recipeKey, RecipeReference reference) {
        RESEARCH_RECIPES.computeIfAbsent(recipeKey, key -> new ArrayList<>()).add(reference);
    }

    public static ShapedWorkbenchRecipe addShaped(
        WorkbenchRecipe.Kind kind,
        String key,
        @Nullable String recipeKey,
        int cost,
        AspectList aspects,
        TcResult output,
        Object... pattern
    ) {
        int index = 0;
        StringBuilder rows = new StringBuilder();
        int width = 0;
        int height = 0;
        while (pattern[index] instanceof String row) {
            index++;
            height++;
            width = row.length();
            rows.append(row);
        }
        Map<Character, TcIngredient> map = new HashMap<>();
        while (index < pattern.length) {
            char symbol = (Character) pattern[index];
            Object value = pattern[index + 1];
            map.put(symbol, value instanceof TcIngredient ingredient ? ingredient : TcIngredient.parse((String) value));
            index += 2;
        }
        TcIngredient[] items = new TcIngredient[width * height];
        for (int i = 0; i < width * height; i++) {
            items[i] = map.get(rows.charAt(i));
        }
        ShapedWorkbenchRecipe recipe = new ShapedWorkbenchRecipe(kind, key, width, height, items, output, cost, aspects);
        WORKBENCH.add(recipe);
        if (recipeKey != null) {
            addResearchRecipe(recipeKey, new RecipeReference.Workbench(recipe));
        }
        return recipe;
    }

    public static ShapelessWorkbenchRecipe addShapeless(
        WorkbenchRecipe.Kind kind,
        String key,
        @Nullable String recipeKey,
        int cost,
        AspectList aspects,
        TcResult output,
        Object... ingredients
    ) {
        List<TcIngredient> list = new ArrayList<>();
        for (Object value : ingredients) {
            list.add(value instanceof TcIngredient ingredient ? ingredient : TcIngredient.parse((String) value));
        }
        ShapelessWorkbenchRecipe recipe = new ShapelessWorkbenchRecipe(kind, key, list, output, cost, aspects);
        WORKBENCH.add(recipe);
        if (recipeKey != null) {
            addResearchRecipe(recipeKey, new RecipeReference.Workbench(recipe));
        }
        return recipe;
    }

    public static CrucibleRecipe addCrucible(String researchKey, String key, TcResult output, int cost, AspectList tags) {
        CrucibleRecipe recipe = new CrucibleRecipe(researchKey, key, output, tags, cost);
        CRUCIBLE.add(recipe);
        addResearchRecipe(key, new RecipeReference.Crucible(recipe));
        return recipe;
    }

    public static @Nullable CrucibleRecipe getCrucibleRecipe(String key) {
        for (CrucibleRecipe recipe : CRUCIBLE) {
            if (recipe.key().equals(key)) {
                return recipe;
            }
        }
        return null;
    }

    public static @Nullable CrucibleRecipe getCrucibleRecipe(ItemStack stack) {
        for (CrucibleRecipe recipe : CRUCIBLE) {
            if (recipe.output().isResolvable() && stack.is(recipe.output().item())) {
                return recipe;
            }
        }
        return null;
    }

    public static @Nullable WorkbenchRecipe findMatching(WorkbenchRecipe.Kind kind, WorkbenchGrid grid, Player player) {
        for (WorkbenchRecipe recipe : WORKBENCH) {
            if (recipe.kind() == kind && recipe.isResolvable() && recipe.matches(grid, player)) {
                return recipe;
            }
        }
        return null;
    }

    public static String getCraftingRecipeKey(ItemStack stack) {
        for (WorkbenchRecipe recipe : WORKBENCH) {
            if (recipe.output().isResolvable() && stack.is(recipe.output().item())) {
                return recipe.key();
            }
        }
        return "";
    }

    public static void addSmeltingBonus(String input, String output) {
        SMELTING_BONUS.put(input, output);
    }

    public static Map<String, String> smeltingBonus() {
        return SMELTING_BONUS;
    }
}
