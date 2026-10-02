package thaumcraft.crafting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModDataComponents;

public record TcResult(String id, int count, @Nullable Aspect aspect, int core) {
    public static TcResult of(String id) {
        return new TcResult(id, 1, null, -1);
    }

    public static TcResult of(String id, int count) {
        return new TcResult(id, count, null, -1);
    }

    public static TcResult golem(String id, int core) {
        return new TcResult(id, 1, null, core);
    }

    public boolean isResolvable() {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).isPresent();
    }

    public @Nullable Item item() {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
    }

    public ItemStack create() {
        Item item = item();
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, count);
        if (aspect != null) {
            stack.set(ModDataComponents.ESSENCE_ASPECT.get(), aspect);
        }
        if (core >= 0) {
            stack.set(ModDataComponents.GOLEM_CORE.get(), core);
        }
        return stack;
    }
}
