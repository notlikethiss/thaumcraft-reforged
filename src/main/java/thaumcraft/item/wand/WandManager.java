package thaumcraft.item.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import thaumcraft.block.WandTarget;
import thaumcraft.block.device.InfernalFurnaceBlock;
import thaumcraft.research.ResearchManager;
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
        if ((state.is(Blocks.OBSIDIAN) || state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.IRON_BARS))
            && ResearchManager.isResearchComplete(player, "INFERNALFURNACE")) {
            return createInfernalFurnace(stack, player, level, pos);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult createInfernalFurnace(ItemStack stack, Player player, Level level, BlockPos pos) {
        for (int x = pos.getX() - 2; x <= pos.getX(); x++) {
            for (int y = pos.getY() - 2; y <= pos.getY(); y++) {
                for (int z = pos.getZ() - 2; z <= pos.getZ(); z++) {
                    BlockPos origin = new BlockPos(x, y, z);
                    Direction grate = fitInfernalFurnace(level, origin);
                    if (grate != null && spendCharge(level, stack, player, 100)) {
                        if (level.isClientSide()) {
                            return InteractionResult.SUCCESS;
                        }
                        replaceInfernalFurnace(level, origin, grate);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    private static @Nullable Direction fitInfernalFurnace(Level level, BlockPos origin) {
        Direction grate = null;
        for (int layer = 0; layer < 3; layer++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, 2 - layer, z));
                    boolean corner = x != 1 && z != 1;
                    boolean center = x == 1 && z == 1;
                    boolean matches;
                    if (corner) {
                        matches = state.is(Blocks.NETHER_BRICKS);
                    } else if (center) {
                        matches = switch (layer) {
                            case 0 -> state.isAir();
                            case 1 -> state.is(Blocks.LAVA) && state.getFluidState().isSource();
                            default -> state.is(Blocks.OBSIDIAN);
                        };
                    } else {
                        matches = state.is(Blocks.OBSIDIAN);
                    }
                    if (!matches) {
                        if (layer != 1 || grate != null || center || corner || !state.is(Blocks.IRON_BARS)) {
                            return null;
                        }
                        grate = Direction.getApproximateNearest(x - 1, 0, z - 1);
                    }
                }
            }
        }
        return grate;
    }

    private static void replaceInfernalFurnace(Level level, BlockPos origin, Direction grate) {
        BlockState base = ModBlocks.INFERNAL_FURNACE.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, grate);
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    BlockPos target = origin.offset(x, y, z);
                    if (level.getBlockState(target).isAir()) {
                        continue;
                    }
                    BlockState state = base.setValue(InfernalFurnaceBlock.X, x).setValue(InfernalFurnaceBlock.Y, y).setValue(InfernalFurnaceBlock.Z, z);
                    level.setBlock(target, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    level.blockEvent(target, state.getBlock(), 1, 4);
                }
            }
        }
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
