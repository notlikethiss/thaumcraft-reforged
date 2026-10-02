package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;

public record ShapelessWorkbenchRecipe(Kind kind, String key, List<TcIngredient> ingredients, TcResult output, int cost, AspectList aspects)
    implements WorkbenchRecipe {
    @Override
    public boolean shaped() {
        return false;
    }

    @Override
    public int width() {
        return 3;
    }

    @Override
    public int height() {
        return 3;
    }

    @Override
    public boolean matchesGrid(WorkbenchGrid grid) {
        List<TcIngredient> remaining = new ArrayList<>(ingredients);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                ItemStack stack = grid.get(x, y);
                if (stack.isEmpty()) {
                    continue;
                }
                boolean found = false;
                for (TcIngredient ingredient : remaining) {
                    if (ingredient.test(stack)) {
                        remaining.remove(ingredient);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return remaining.isEmpty();
    }
}
