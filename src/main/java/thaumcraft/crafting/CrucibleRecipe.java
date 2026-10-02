package thaumcraft.crafting;

import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public record CrucibleRecipe(String researchKey, String key, TcResult output, AspectList tags, int cost) {
    public boolean matches(@Nullable AspectList input) {
        if (input == null) {
            return false;
        }
        for (Aspect aspect : tags.getAspects()) {
            if (input.getAmount(aspect) < tags.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    public @Nullable AspectList removeMatching(AspectList input) {
        AspectList result = input.copy();
        for (Aspect aspect : tags.getAspects()) {
            if (!result.reduceAmount(aspect, tags.getAmount(aspect))) {
                return null;
            }
        }
        return result;
    }
}
