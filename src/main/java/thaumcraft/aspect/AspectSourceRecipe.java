package thaumcraft.aspect;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record AspectSourceRecipe(Kind kind, ItemStack output, List<Ingredient> ingredients, int cost, AspectList aspects) {
    public enum Kind {
        CRUCIBLE,
        ARCANE,
        INFUSION,
        CRAFTING
    }
}
