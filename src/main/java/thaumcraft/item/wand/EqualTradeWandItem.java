package thaumcraft.item.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModDataComponents;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public class EqualTradeWandItem extends ElementalWandItem {
    public EqualTradeWandItem(Properties properties) {
        super(4, properties);
    }

    public static @Nullable BlockState getPickedBlock(ItemStack stack) {
        return stack.get(ModDataComponents.TRADE_BLOCK.get());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState clicked = level.getBlockState(pos);
        if (player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) != null || clicked.getBlock().asItem() == Items.AIR) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                stack.set(ModDataComponents.TRADE_BLOCK.get(), clicked);
            }
            return InteractionResult.SUCCESS;
        }
        BlockState picked = getPickedBlock(stack);
        if (picked == null || level.getBlockEntity(pos) != null) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            SwapperManager.addSwapper(serverLevel, pos, clicked, picked, 3 + getPotency(level, stack), serverPlayer, serverPlayer.getInventory().getSelectedSlot());
        }
        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof EqualTradeWandItem)) {
            return;
        }
        event.setCanceled(true);
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
            || !(player.level() instanceof ServerLevel level)
            || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        BlockState picked = getPickedBlock(stack);
        if (picked == null || level.getBlockEntity(event.getPos()) != null) {
            return;
        }
        SwapperManager.addSwapper(level, event.getPos(), level.getBlockState(event.getPos()), picked, 0, serverPlayer, serverPlayer.getInventory().getSelectedSlot());
    }
}
