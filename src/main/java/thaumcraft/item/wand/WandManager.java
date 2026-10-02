package thaumcraft.item.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.block.WandTarget;
import thaumcraft.network.BlockSparklePayload;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

public final class WandManager {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private WandManager() {
    }

    public static InteractionResult useOn(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof WandTarget target) {
            InteractionResult result = target.onWandUse(level, pos, state, player, stack, context.getClickedFace());
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        if (state.is(Blocks.BOOKSHELF)) {
            return createThaumonomicon(stack, player, level, pos);
        }
        if (state.is(BlockTags.CAULDRONS)) {
            return createCrucible(stack, player, level, pos);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult createThaumonomicon(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (!spendCharge(level, stack, player, 25)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.removeBlock(pos, false);
            ItemEntity book = new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, new ItemStack(ModItems.THAUMONOMICON.get()));
            book.setDeltaMovement(Vec3.ZERO);
            book.setNoGravity(true);
            book.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(book);
            PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 64.0, new BlockSparklePayload(pos, 0));
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult createCrucible(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (!spendCharge(level, stack, player, 25)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, ModBlocks.CRUCIBLE.get().defaultBlockState());
            level.blockEvent(pos, ModBlocks.CRUCIBLE.get(), 1, 1);
        }
        return InteractionResult.SUCCESS;
    }

    public static int getTotalVisDiscount(Player player) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            total += player.getItemBySlot(slot).getOrDefault(ModDataComponents.VIS_DISCOUNT.get(), 0);
        }
        return total;
    }

    public static int getDiscountedCost(Player player, int amount) {
        return Math.round(amount * ((100 - Math.min(50, getTotalVisDiscount(player))) / 100.0F));
    }

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WAND_VIS.get(), 0);
    }

    public static boolean hasCharge(ItemStack stack, Player player, int amount) {
        return stack.has(ModDataComponents.WAND_VIS.get()) && getCharge(stack) >= getDiscountedCost(player, amount);
    }

    public static boolean spendCharge(Level level, ItemStack stack, Player player, int amount) {
        amount = getDiscountedCost(player, amount);
        if (!stack.has(ModDataComponents.WAND_VIS.get())) {
            return false;
        }
        int vis = getCharge(stack);
        if (level.isClientSide()) {
            return vis >= amount;
        }
        if (vis >= amount) {
            if (!player.getAbilities().instabuild) {
                stack.set(ModDataComponents.WAND_VIS.get(), vis - amount);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.value(), SoundSource.PLAYERS, 0.5F, 1.0F);
            return true;
        }
        if (amount > 0) {
            player.sendSystemMessage(Component.translatable("tc.thaumcraft.wandnocharge"));
        }
        return false;
    }

    public static boolean spendCharge(ItemStack stack, Player player, int amount) {
        amount = getDiscountedCost(player, amount);
        if (!stack.has(ModDataComponents.WAND_VIS.get())) {
            return false;
        }
        int vis = getCharge(stack);
        if (vis < amount) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            stack.set(ModDataComponents.WAND_VIS.get(), vis - amount);
        }
        return true;
    }
}
