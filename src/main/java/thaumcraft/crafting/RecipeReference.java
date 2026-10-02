package thaumcraft.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public sealed interface RecipeReference {
    record Workbench(WorkbenchRecipe recipe) implements RecipeReference {
    }

    record Crucible(CrucibleRecipe recipe) implements RecipeReference {
    }

    record Vanilla(String recipeId) implements RecipeReference {
    }

    record Compound(int width, int height, int depth, int cost, List<String> blocks) implements RecipeReference {
    }

    record FakeShaped(WorkbenchRecipe.Kind kind, int width, int height, List<TcIngredient> ingredients, TcResult output, int cost) implements RecipeReference {
    }

    static ItemStack stack(String id) {
        return TcResult.of(id).create();
    }
}
