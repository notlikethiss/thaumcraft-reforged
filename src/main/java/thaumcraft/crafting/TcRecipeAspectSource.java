package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSourceRecipe;

public final class TcRecipeAspectSource {
    private TcRecipeAspectSource() {
    }

    public static void collect(MinecraftServer server, List<AspectSourceRecipe> output) {
        for (CrucibleRecipe recipe : ThaumcraftRecipes.crucibleRecipes()) {
            if (recipe.output().isResolvable()) {
                output.add(new AspectSourceRecipe(AspectSourceRecipe.Kind.CRUCIBLE, recipe.output().create(), List.of(), recipe.cost(), recipe.tags()));
            }
        }
        for (WorkbenchRecipe recipe : ThaumcraftRecipes.workbenchRecipes()) {
            if (!recipe.isResolvable()) {
                continue;
            }
            List<Ingredient> ingredients = new ArrayList<>();
            for (TcIngredient ingredient : recipe.ingredients()) {
                if (ingredient != null) {
                    ingredients.add(ingredient.toIngredient());
                }
            }
            AspectSourceRecipe.Kind kind = recipe.kind() == WorkbenchRecipe.Kind.ARCANE ? AspectSourceRecipe.Kind.ARCANE : AspectSourceRecipe.Kind.INFUSION;
            AspectList aspects = recipe.aspects();
            output.add(new AspectSourceRecipe(kind, recipe.output().create(), ingredients, recipe.cost(), aspects));
        }
    }
}
