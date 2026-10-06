package thaumcraft.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectProvidingItem;

public class WispEssenceItem extends Item implements AspectProvidingItem {
    public WispEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable AspectList getStackAspects(ItemStack stack, @Nullable AspectList base) {
        Aspect aspect = EssenceItem.getAspect(stack);
        return aspect == null ? base : new AspectList().add(aspect, 4).add(Aspect.SPIRIT, 1).add(Aspect.FLUX, 1);
    }
}
