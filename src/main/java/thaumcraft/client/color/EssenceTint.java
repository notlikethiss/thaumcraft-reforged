package thaumcraft.client.color;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.EssenceItem;

public record EssenceTint(int layer) implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != layer) {
            return -1;
        }
        Aspect aspect = EssenceItem.getAspect(stack);
        return aspect == null ? -1 : FastColor.ARGB32.opaque(aspect.color);
    }
}
