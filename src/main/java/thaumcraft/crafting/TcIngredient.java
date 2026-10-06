package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.EssenceItem;
import thaumcraft.item.golem.GolemPlacerItem;

public record TcIngredient(String id, boolean tag, @Nullable Aspect aspect, int core) {
    public static TcIngredient item(String id) {
        return new TcIngredient(id, false, null, -1);
    }

    public static TcIngredient tag(String id) {
        return new TcIngredient(id, true, null, -1);
    }

    public static TcIngredient essence(String id, Aspect aspect) {
        return new TcIngredient(id, false, aspect, -1);
    }

    public static TcIngredient golem(String id, int core) {
        return new TcIngredient(id, false, null, core);
    }

    public static TcIngredient parse(String value) {
        return value.startsWith("#") ? tag(value.substring(1)) : item(value);
    }

    private @Nullable Item resolveItem() {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElse(null);
    }

    private TagKey<Item> tagKey() {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }

    public boolean isResolvable() {
        return tag || resolveItem() != null;
    }

    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean itemMatches;
        if (tag) {
            itemMatches = stack.is(tagKey());
        } else {
            Item item = resolveItem();
            itemMatches = item != null && stack.is(item);
        }
        if (!itemMatches) {
            return false;
        }
        if (core >= 0 && !(GolemPlacerItem.hasCore(stack) && GolemPlacerItem.getCore(stack) == core)) {
            return false;
        }
        return aspect == null || aspect == EssenceItem.getAspect(stack);
    }

    public List<ItemStack> displayStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        if (tag) {
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey())) {
                stacks.add(new ItemStack(holder));
            }
        } else {
            Item item = resolveItem();
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                if (aspect != null) {
                    EssenceItem.setAspect(stack, aspect);
                }
                if (core >= 0) {
                    GolemPlacerItem.setCore(stack, core);
                }
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public Ingredient toIngredient() {
        if (tag) {
            return Ingredient.of(tagKey());
        }
        Item item = resolveItem();
        return item == null ? Ingredient.of() : Ingredient.of(item);
    }
}
