package thaumcraft.client.color;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.EssenceItem;

public record EssenceTint() implements ItemTintSource {
    public static final MapCodec<EssenceTint> MAP_CODEC = MapCodec.unit(new EssenceTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        Aspect aspect = EssenceItem.getAspect(stack);
        return aspect == null ? -1 : FastColor.ARGB32.opaque(aspect.color);
    }

    @Override
    public MapCodec<EssenceTint> type() {
        return MAP_CODEC;
    }
}
