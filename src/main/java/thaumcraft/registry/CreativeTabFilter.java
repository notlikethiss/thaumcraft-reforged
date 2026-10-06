package thaumcraft.registry;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import thaumcraft.Config;

public final class CreativeTabFilter {
    private static final Map<String, String> REQUIRES_METAL = Map.of(
        "tin_nugget", "tin",
        "native_tin_cluster", "tin",
        "silver_nugget", "silver",
        "native_silver_cluster", "silver",
        "lead_nugget", "lead",
        "native_lead_cluster", "lead"
    );

    private static final Set<String> HIDDEN = Set.of("research_notes", "discovery");

    private CreativeTabFilter() {
    }

    public static boolean visible(Item item, CreativeModeTab.ItemDisplayParameters parameters) {
        String path = item.builtInRegistryHolder().key().location().getPath();
        if (HIDDEN.contains(path)) {
            return false;
        }
        if (path.endsWith("_warded_stone")) {
            return !Config.SPEC.isLoaded() || Config.wardedStone();
        }
        if (path.equals("thaumonomicon_cheat")) {
            return Config.SPEC.isLoaded() && Config.ALLOW_CHEAT_SHEET.getAsBoolean();
        }
        String metal = REQUIRES_METAL.get(path);
        if (metal == null) {
            return true;
        }
        TagKey<Item> ingots = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + metal));
        return parameters.holders()
            .lookup(Registries.ITEM)
            .flatMap(lookup -> lookup.get(ingots))
            .map(HolderSet::size)
            .orElse(0) > 0;
    }
}
