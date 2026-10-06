package thaumcraft.aspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import javax.annotation.Nullable;

public final class AspectRegistry {
    private static final int HISTORY_LIMIT = 100;
    private static volatile Map<Item, AspectList> active = Map.of();

    private final Map<Item, AspectList> tags = new HashMap<>();
    private final Map<Item, List<AspectSourceRecipe>> recipesByOutput = new HashMap<>();

    public AspectRegistry(List<AspectSourceRecipe> recipes) {
        for (AspectSourceRecipe recipe : recipes) {
            if (!recipe.output().isEmpty()) {
                recipesByOutput.computeIfAbsent(recipe.output().getItem(), item -> new ArrayList<>()).add(recipe);
            }
        }
    }

    public static Map<Item, AspectList> active() {
        return active;
    }

    public static void setActive(Map<Item, AspectList> tags) {
        active = Map.copyOf(tags);
    }

    public Map<Item, AspectList> buildAll() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                getObjectTags(item);
            }
        }
        Map<Item, AspectList> result = new LinkedHashMap<>();
        for (Map.Entry<Item, AspectList> entry : tags.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public boolean exists(Item item) {
        return tags.get(item) != null;
    }

    public void register(Item item, @Nullable AspectList aspects) {
        tags.put(item, aspects == null ? null : aspects.copy().cull(5));
    }

    public @Nullable AspectList getObjectTags(Item item) {
        AspectList existing = tags.get(item);
        if (existing != null) {
            return existing.copy();
        }
        AspectList generated = generate(item, new ArrayList<>());
        return generated == null ? null : generated.copy();
    }

    public @Nullable AspectList generate(Item item, List<Item> history) {
        if (exists(item)) {
            return tags.get(item).copy();
        }
        if (history.contains(item)) {
            return null;
        }
        history.add(item);
        if (history.size() >= HISTORY_LIMIT) {
            return null;
        }
        AspectList generated = generateFromRecipes(item, history);
        register(item, generated);
        return generated;
    }

    private @Nullable AspectList generateFromRecipes(Item item, List<Item> history) {
        List<AspectSourceRecipe> recipes = recipesByOutput.getOrDefault(item, List.of());
        AspectList result = fromCrucible(recipes);
        if (result != null) {
            return result;
        }
        result = fromWeighted(recipes, AspectSourceRecipe.Kind.ARCANE, history);
        if (result != null) {
            return result;
        }
        result = fromWeighted(recipes, AspectSourceRecipe.Kind.INFUSION, history);
        if (result != null) {
            return result;
        }
        return fromCrafting(recipes, history);
    }

    private @Nullable AspectList fromCrucible(List<AspectSourceRecipe> recipes) {
        for (AspectSourceRecipe recipe : recipes) {
            if (recipe.kind() != AspectSourceRecipe.Kind.CRUCIBLE) {
                continue;
            }
            AspectList result = new AspectList();
            int outputCount = recipe.output().getCount();
            result.add(Aspect.MAGIC, Math.round(recipe.cost() / 10.0F));
            for (Aspect aspect : recipe.aspects().getAspects()) {
                result.add(aspect, recipe.aspects().getAmount(aspect) / outputCount);
            }
            return result;
        }
        return null;
    }

    private @Nullable AspectList fromWeighted(List<AspectSourceRecipe> recipes, AspectSourceRecipe.Kind kind, List<Item> history) {
        AspectList best = null;
        int bestValue = 0;
        for (AspectSourceRecipe recipe : recipes) {
            if (recipe.kind() != kind) {
                continue;
            }
            Map<Item, Integer> ingredients = collectIngredients(recipe.ingredients(), history);
            if (ingredients == null) {
                continue;
            }
            AspectList result = new AspectList();
            int value = sumIngredients(ingredients, recipe.output().getCount(), result, history);
            result.add(Aspect.MAGIC, Math.round(recipe.cost() / 10.0F / recipe.output().getCount()));
            if (kind == AspectSourceRecipe.Kind.INFUSION) {
                for (Aspect aspect : recipe.aspects().getAspects()) {
                    result.add(aspect, Math.round((float) recipe.aspects().getAmount(aspect) / recipe.output().getCount()));
                }
            }
            if (value >= bestValue) {
                best = result;
                bestValue = value;
            }
        }
        return best;
    }

    private @Nullable AspectList fromCrafting(List<AspectSourceRecipe> recipes, List<Item> history) {
        AspectList best = null;
        int bestValue = Integer.MAX_VALUE;
        for (AspectSourceRecipe recipe : recipes) {
            if (recipe.kind() != AspectSourceRecipe.Kind.CRAFTING) {
                continue;
            }
            Map<Item, Integer> ingredients = collectIngredients(recipe.ingredients(), history);
            if (ingredients == null) {
                continue;
            }
            AspectList result = new AspectList();
            int value = sumIngredients(ingredients, recipe.output().getCount(), result, history);
            if (value < bestValue && value > 0) {
                best = result;
                bestValue = value;
            }
        }
        return best;
    }

    private @Nullable Map<Item, Integer> collectIngredients(List<Ingredient> ingredients, List<Item> history) {
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient == null || ingredient.isEmpty()) {
                continue;
            }
            List<Item> options = Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).toList();
            Item chosen = null;
            if (options.size() == 1) {
                chosen = options.getFirst();
            } else {
                for (Item option : options) {
                    AspectList aspects = generate(option, history);
                    if (aspects != null && aspects.size() > 0) {
                        chosen = option;
                        break;
                    }
                }
            }
            if (chosen != null) {
                counts.merge(chosen, 1, Integer::sum);
            }
        }
        return counts;
    }

    private int sumIngredients(Map<Item, Integer> ingredients, int outputCount, AspectList result, List<Item> history) {
        int value = 0;
        for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
            AspectList aspects = generate(entry.getKey(), history);
            AspectList container = null;
            Item remainder = entry.getKey().getCraftingRemainingItem();
            if (remainder != null) {
                container = generate(remainder, history);
            }
            if (aspects == null) {
                continue;
            }
            for (Aspect aspect : aspects.getAspects()) {
                if (container != null && container.getAmount(aspect) > 0) {
                    continue;
                }
                float amount = (float) (aspects.getAmount(aspect) * entry.getValue()) / outputCount;
                if (amount > 0.5F) {
                    int scaled = Math.max(Math.round(amount * 0.8F), 1);
                    result.add(aspect, scaled);
                    value += scaled;
                }
            }
        }
        return value;
    }

    public static Optional<AspectList> lookup(Item item) {
        return Optional.ofNullable(active.get(item)).map(AspectList::copy);
    }
}
