package thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import thaumcraft.aura.AuraManager;
import thaumcraft.blockentity.HoleBlockEntity;

public class PortableHoleItem extends Item {
    private static final int MAX_DISTANCE = 33;

    public PortableHoleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (stack.getDamageValue() >= 10 && AuraManager.decreaseClosestAura(level, owner.getX(), owner.getY(), owner.getZ(), 1)) {
            stack.setDamageValue(stack.getDamageValue() - 10);
        }
    }

    private static boolean spendCharge(Level level, ItemStack stack, Player player, int amount) {
        int charge = stack.getMaxDamage() - stack.getDamageValue();
        if (charge >= amount * 10) {
            if (!level.isClientSide()) {
                stack.setDamageValue(stack.getDamageValue() + amount * 10);
            }
            return true;
        }
        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable("tc.thaumcraft.portableholeerror"));
        }
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos start = context.getClickedPos();
        Direction travel = context.getClickedFace().getOpposite();
        BlockPos cursor = start;
        int distance;
        for (distance = 0; distance < MAX_DISTANCE; distance++) {
            if (!HoleBlockEntity.canPassThrough(level, cursor)) {
                break;
            }
            cursor = cursor.relative(travel);
        }
        if (spendCharge(level, stack, player, distance) && !level.isClientSide()) {
            HoleBlockEntity.createHole(level, start, travel, distance + 1);
        }
        if (!level.isClientSide()) {
            level.playSound(null, start, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
