package thaumcraft.item.wand;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import javax.annotation.Nullable;
import thaumcraft.aura.AuraManager;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.registry.ModSounds;
import thaumcraft.registry.ModDataComponents;

public class CastingWandItem extends Item {
    private final int maxVis;
    private final int rechargeInterval;

    public CastingWandItem(int maxVis, int rechargeInterval, Properties properties) {
        super(properties.stacksTo(1).component(ModDataComponents.WAND_VIS.get(), maxVis));
        this.maxVis = maxVis;
        this.rechargeInterval = rechargeInterval;
    }

    public static @Nullable Integer getVis(ItemStack stack) {
        return stack.get(ModDataComponents.WAND_VIS.get());
    }

    public static boolean hasVis(ItemStack stack) {
        return stack.has(ModDataComponents.WAND_VIS.get());
    }

    public static void setVis(ItemStack stack, int vis) {
        stack.set(ModDataComponents.WAND_VIS.get(), vis);
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
                setVis(stack, vis + 1);
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
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof GolemBase golem) || !golem.isAlive()) {
            return InteractionResult.PASS;
        }
        if (!(golem.level() instanceof ServerLevel level)) {
            golem.spawnAnim();
            return InteractionResult.SUCCESS;
        }
        golem.spawnAtLocation(level, golem.toItem(), 0.5F);
        golem.playSound(ModSounds.ZAP.get(), 0.5F, 1.0F);
        golem.dropContents(level);
        golem.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return WandManager.useOn(stack, context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        Integer vis = getVis(stack);
        if (vis != null) {
            builder.accept(Component.translatable("tc.thaumcraft.wandcharge", vis));
        }
    }
}
