package thaumcraft.crafting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.research.ResearchManager;

public final class CrucibleCrafting {
    private CrucibleCrafting() {
    }

    public static @Nullable CrucibleRecipe findRecipe(AspectList tags) {
        CrucibleRecipe best = null;
        int bestSize = 0;
        for (CrucibleRecipe recipe : ThaumcraftRecipes.crucibleRecipes()) {
            if (recipe.tags().isEmpty() || !recipe.output().isResolvable() || !recipe.matches(tags)) {
                continue;
            }
            if (recipe.tags().size() > bestSize) {
                best = recipe;
                bestSize = recipe.tags().size();
            }
        }
        return best;
    }

    public static int getOutputCost(CrucibleBlockEntity crucible) {
        AspectList tags = crucible.getTags().copy();
        CrucibleRecipe recipe = findRecipe(tags);
        if (recipe == null) {
            return 0;
        }
        int cost = 0;
        while (tags != null && recipe.matches(tags)) {
            tags = recipe.removeMatching(tags);
            cost += recipe.cost();
        }
        return cost;
    }

    public static void perform(Level level, Player player, CrucibleBlockEntity crucible) {
        AspectList tags = crucible.getTags().copy();
        CrucibleRecipe recipe = findRecipe(tags);
        ItemStack output = ItemStack.EMPTY;
        if (recipe != null) {
            int count = 0;
            while (tags != null && recipe.matches(tags)) {
                tags = recipe.removeMatching(tags);
                count += recipe.output().count();
            }
            crucible.setTags(tags == null ? new AspectList() : tags);
            output = recipe.output().create();
            output.setCount(count);
        }
        if (!output.isEmpty() && ResearchManager.isCrucibleCreationSuccessful(level, output, player)) {
            crucible.ejectItem(output);
            level.blockEvent(crucible.getBlockPos(), crucible.getBlockState().getBlock(), 1, -1);
        }
        crucible.spillRemnants();
    }
}
