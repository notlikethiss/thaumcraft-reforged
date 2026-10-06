package thaumcraft.client.research;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class ClientRecipes {
    private ClientRecipes() {
    }

    public static @Nullable RecipeHolder<?> get(String id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        return minecraft.level.getRecipeManager().byKey(ResourceLocation.parse(id)).orElse(null);
    }
}
