package thaumcraft.aspect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import thaumcraft.Thaumcraft;

public abstract class AspectRegistrar {
    private final AspectRegistry registry;

    protected AspectRegistrar(AspectRegistry registry) {
        this.registry = registry;
    }

    protected static AspectList tags() {
        return new AspectList();
    }

    protected AspectList copy(String target) {
        List<Item> items = resolve(target);
        if (items.isEmpty()) {
            return new AspectList();
        }
        AspectList existing = registry.getObjectTags(items.getFirst());
        return existing == null ? new AspectList() : existing;
    }

    protected void register(String target, AspectList aspects) {
        for (Item item : resolve(target)) {
            registry.register(item, aspects);
        }
    }

    protected void register(List<String> targets, AspectList aspects) {
        for (String target : targets) {
            register(target, aspects);
        }
    }

    protected void complex(String target, AspectList aspects) {
        for (Item item : resolve(target)) {
            if (!registry.exists(item)) {
                AspectList combined = aspects.copy();
                AspectList generated = registry.generate(item, new ArrayList<>());
                if (generated != null && generated.size() > 0) {
                    combined.add(generated);
                }
                registry.register(item, combined);
            } else {
                registry.register(item, registry.getObjectTags(item));
            }
        }
    }

    protected void complex(List<String> targets, AspectList aspects) {
        for (String target : targets) {
            complex(target, aspects);
        }
    }

    protected void registerEssences() {
        register("minecraft:splash_potion", copy("minecraft:potion"));
        register("minecraft:lingering_potion", copy("minecraft:potion"));
    }

    private static List<Item> resolve(String target) {
        List<Item> items = new ArrayList<>();
        if (target.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1)));
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                items.add(holder.value());
            }
            return items;
        }
        Identifier id = Identifier.parse(target);
        BuiltInRegistries.ITEM.getOptional(id).ifPresentOrElse(
            items::add,
            () -> Thaumcraft.LOGGER.debug("Aspect target {} is not registered", target)
        );
        return items;
    }
}
