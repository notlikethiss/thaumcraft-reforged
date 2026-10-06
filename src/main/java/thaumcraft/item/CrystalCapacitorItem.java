package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nullable;
import thaumcraft.registry.ModDataComponents;

public class CrystalCapacitorItem extends BlockItem {
    public CrystalCapacitorItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static @Nullable Integer getStoredVis(ItemStack stack) {
        return stack.get(ModDataComponents.STORED_VIS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        Integer vis = getStoredVis(stack);
        if (vis != null) {
            builder.accept(Component.translatable("tc.thaumcraft.capacitor_vis", vis));
        }
    }
}
