package thaumcraft.item.wand;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.Nullable;
import thaumcraft.aura.AuraManager;
import thaumcraft.registry.ModDataComponents;

public class CastingWandItem extends Item {
    private final int maxVis;
    private final int rechargeInterval;

    public CastingWandItem(int maxVis, int rechargeInterval, Properties properties) {
        super(properties.stacksTo(1).component(ModDataComponents.WAND_VIS.get(), maxVis));
        this.maxVis = maxVis;
        this.rechargeInterval = rechargeInterval;
    }

    public int getMaxVis() {
        return maxVis;
    }

    public int getRechargeInterval() {
        return rechargeInterval;
    }

    public boolean recharge(ItemStack stack, ServerLevel level, int count, double x, double y, double z) {
        if (count % rechargeInterval != 0) {
            return false;
        }
        int vis = WandManager.getCharge(stack);
        if (vis < maxVis) {
            if (AuraManager.decreaseClosestAura(level, x, y, z, 1)) {
                stack.set(ModDataComponents.WAND_VIS.get(), vis + 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        recharge(stack, level, owner.tickCount, owner.getX(), owner.getY(), owner.getZ());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return WandManager.useOn(stack, context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        Integer vis = stack.get(ModDataComponents.WAND_VIS.get());
        if (vis != null) {
            builder.accept(Component.translatable("tc.thaumcraft.wandcharge", vis));
        }
    }
}
