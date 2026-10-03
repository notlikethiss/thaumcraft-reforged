package thaumcraft.item.wand;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.lib.Utils;
import thaumcraft.network.BlockSparklePayload;
import thaumcraft.registry.ModSounds;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class SwapperManager {
    private static final Map<ResourceKey<Level>, Queue<Swapper>> SWAPPERS = new HashMap<>();

    private SwapperManager() {
    }

    public static void addSwapper(ServerLevel level, BlockPos pos, BlockState source, BlockState target, int life, ServerPlayer player, int slot) {
        if (source.isAir() || source.getDestroySpeed(level, pos) < 0.0F || source.equals(target)) {
            return;
        }
        SWAPPERS.computeIfAbsent(level.dimension(), key -> new ArrayDeque<>()).add(new Swapper(pos.immutable(), source, target, life, player.getUUID(), slot));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.value(), SoundSource.PLAYERS, 0.25F, 1.0F);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Queue<Swapper> queue = SWAPPERS.get(level.dimension());
        if (queue == null) {
            return;
        }
        Swapper swapper;
        while ((swapper = queue.poll()) != null) {
            if (process(level, queue, swapper)) {
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            SWAPPERS.remove(level.dimension());
        }
    }

    private static boolean process(ServerLevel level, Queue<Swapper> queue, Swapper swapper) {
        BlockPos pos = swapper.pos();
        BlockState current = level.getBlockState(pos);
        if (current.equals(swapper.target()) || !current.equals(swapper.source())) {
            return false;
        }
        if (!(level.getPlayerByUUID(swapper.playerId()) instanceof ServerPlayer player)) {
            return false;
        }
        ItemStack wandStack = player.getInventory().getItem(swapper.slot());
        if (!(wandStack.getItem() instanceof EqualTradeWandItem wand)) {
            return false;
        }
        Item targetItem = swapper.target().getBlock().asItem();
        if (!hasItem(player, targetItem)) {
            return false;
        }
        if (!level.mayInteract(player, pos) || CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, current).isCanceled()) {
            return false;
        }
        int fortune = wand.getTreasure(level, wandStack);
        Utils.consumeInventoryItem(player, stack -> stack.is(targetItem));
        ItemStack tool = wandStack.copy();
        if (fortune > 0) {
            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE)
                .ifPresent(holder -> EnchantmentHelper.updateEnchantments(tool, enchantments -> enchantments.set(holder, fortune)));
        }
        BlockEntity blockEntity = current.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        for (ItemStack drop : Block.getDrops(current, level, pos, blockEntity, player, tool)) {
            if (!player.getInventory().add(drop) && !drop.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
            }
        }
        wand.damageWand(wandStack, player, 1);
        level.levelEvent(2001, pos, Block.getId(current));
        level.setBlock(pos, swapper.target(), Block.UPDATE_ALL);
        PacketDistributor.sendToPlayersNear(level, null, pos.getX(), pos.getY(), pos.getZ(), 64.0, new BlockSparklePayload(pos, 3, 5));
        if (swapper.life() > 0) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        BlockPos neighbor = pos.offset(offsetX, offsetY, offsetZ);
                        if ((offsetX != 0 || offsetY != 0 || offsetZ != 0)
                            && level.getBlockState(neighbor).equals(swapper.source())
                            && Utils.isBlockExposed(level, neighbor)) {
                            queue.add(new Swapper(neighbor, swapper.source(), swapper.target(), swapper.life() - 1, swapper.playerId(), swapper.slot()));
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private record Swapper(BlockPos pos, BlockState source, BlockState target, int life, UUID playerId, int slot) {
    }
}
