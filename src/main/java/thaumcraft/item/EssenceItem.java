package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectProvidingItem;
import thaumcraft.aspect.EssentiaContainer;
import thaumcraft.registry.ModDataComponents;

public class EssenceItem extends Item implements AspectProvidingItem {
    public EssenceItem(Properties properties) {
        super(properties);
    }

    public static @Nullable Aspect getAspect(ItemStack stack) {
        return stack.get(ModDataComponents.ESSENCE_ASPECT.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        Aspect aspect = getAspect(stack);
        return aspect == null ? super.getName(stack) : Component.translatable("item.thaumcraft.essence", aspect.getDisplayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        if (aspect != null) {
            builder.accept(aspect.getMeaning());
        }
    }

    @Override
    public @Nullable AspectList getStackAspects(ItemStack stack, @Nullable AspectList base) {
        Aspect aspect = getAspect(stack);
        return aspect == null ? base : new AspectList().add(aspect, EssentiaPhialItem.PORTION);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Aspect aspect = getAspect(context.getItemInHand());
        if (player == null
            || aspect == null
            || !(level.getBlockEntity(context.getClickedPos()) instanceof EssentiaContainer container)
            || !container.acceptsPhials()
            || container.getContainedAmount() > container.getMaxAmount() - EssentiaPhialItem.PORTION) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && container.addToSource(aspect, EssentiaPhialItem.PORTION) == 0) {
            context.getItemInHand().shrink(1);
            EssentiaPhialItem.playFillSound(level, player);
        }
        return InteractionResult.SUCCESS;
    }
}
