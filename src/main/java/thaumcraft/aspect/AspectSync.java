package thaumcraft.aspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.network.AspectTagsPayload;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AspectSync {
    private static final List<RecipeSourceProvider> PROVIDERS = new ArrayList<>();

    public interface RecipeSourceProvider {
        void collect(MinecraftServer server, List<AspectSourceRecipe> output);
    }

    private AspectSync() {
    }

    public static void addProvider(RecipeSourceProvider provider) {
        PROVIDERS.add(provider);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        rebuild(event.getServer());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            rebuild(event.getPlayerList().getServer());
        }
        AspectTagsPayload payload = new AspectTagsPayload(AspectRegistry.active());
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    public static void rebuild(MinecraftServer server) {
        long start = System.currentTimeMillis();
        List<AspectSourceRecipe> recipes = new ArrayList<>();
        for (RecipeSourceProvider provider : PROVIDERS) {
            provider.collect(server, recipes);
        }
        collectCrafting(server, recipes);
        AspectRegistry registry = new AspectRegistry(recipes);
        new ConfigAspects(registry).registerAll();
        Map<Item, AspectList> tags = registry.buildAll();
        AspectRegistry.setActive(tags);
        Thaumcraft.LOGGER.info("Built aspects for {} items in {} ms", tags.size(), System.currentTimeMillis() - start);
    }

    private static void collectCrafting(MinecraftServer server, List<AspectSourceRecipe> output) {
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (recipe instanceof ShapedRecipe shaped) {
                List<Ingredient> ingredients = new ArrayList<>();
                int width = shaped.getWidth();
                List<Optional<Ingredient>> pattern = shaped.getIngredients();
                for (int x = 0; x < width && x < 3; x++) {
                    for (int y = 0; y < shaped.getHeight() && y < 3; y++) {
                        pattern.get(x + y * width).ifPresent(ingredients::add);
                    }
                }
                ItemStack result = shaped.assemble(CraftingInput.EMPTY);
                output.add(new AspectSourceRecipe(AspectSourceRecipe.Kind.CRAFTING, result, ingredients, 0, AspectList.EMPTY));
            } else if (recipe instanceof ShapelessRecipe shapeless) {
                ItemStackTemplate result = shapeless.result();
                if (result != null) {
                    List<Ingredient> ingredients = shapeless.placementInfo().ingredients();
                    output.add(new AspectSourceRecipe(
                        AspectSourceRecipe.Kind.CRAFTING,
                        result.create(),
                        ingredients.subList(0, Math.min(9, ingredients.size())),
                        0,
                        AspectList.EMPTY
                    ));
                }
            }
        }
    }
}
