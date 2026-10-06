package thaumcraft.item.golem;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class GolemCoreItem extends Item {
    public static final String[] NAMES = {"basic", "speed", "intelligence", "perception", "strength"};

    private final int core;

    public GolemCoreItem(int core, Properties properties) {
        super(properties);
        this.core = core;
    }

    public int core() {
        return core;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.golem_core");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tc.thaumcraft.golem_core." + NAMES[core]).withStyle(ChatFormatting.GRAY));
    }
}
