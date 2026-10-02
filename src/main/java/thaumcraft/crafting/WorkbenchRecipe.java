package thaumcraft.crafting;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.ResearchManager;

public interface WorkbenchRecipe {
    enum Kind {
        ARCANE,
        INFUSION
    }

    Kind kind();

    String key();

    int cost();

    TcResult output();

    AspectList aspects();

    boolean shaped();

    int width();

    int height();

    List<TcIngredient> ingredients();

    boolean matchesGrid(WorkbenchGrid grid);

    default boolean matches(WorkbenchGrid grid, Player player) {
        if (!key().isEmpty() && !ResearchManager.isResearchComplete(player, key())) {
            return false;
        }
        return matchesGrid(grid);
    }

    default ItemStack assemble() {
        return output().create();
    }

    default boolean isResolvable() {
        if (!output().isResolvable()) {
            return false;
        }
        for (TcIngredient ingredient : ingredients()) {
            if (ingredient != null && !ingredient.isResolvable()) {
                return false;
            }
        }
        return true;
    }
}
