package thaumcraft.item.tool;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.item.ModMaterials;
import thaumcraft.network.ModNetwork;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.lib.MiningUtils;
import thaumcraft.network.BlockTagsPayload;
import thaumcraft.registry.ModSounds;

public class ElementalPickaxeItem extends PickaxeItem {
    private static final int SCAN_DEPTH = 8;

    public ElementalPickaxeItem(Properties properties) {
        super(ModMaterials.ELEMENTAL_TOOL, properties.attributes(DiggerItem.createAttributes(ModMaterials.ELEMENTAL_TOOL, 1.0F, -2.8F)));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.level() instanceof ServerLevel level && (!(entity instanceof Player) || level.getServer().isPvpAllowed())) {
            entity.igniteForSeconds(2.0F);
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        context.getItemInHand().hurtAndBreak(5, player, LivingEntity.getSlotForHand(context.getHand()));
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.WAND.value(), SoundSource.PLAYERS, 0.1F, 0.2F + level.getRandom().nextFloat() * 0.2F);
        Direction side = context.getClickedFace();
        ModNetwork.sendToPlayer(serverPlayer, new BlockTagsPayload(pos, side, scan(level, pos, side)));
        return InteractionResult.SUCCESS;
    }

    private static AspectList scan(ServerLevel level, BlockPos origin, Direction side) {
        Direction direction = side.getOpposite();
        int highest = 0;
        Aspect highestAspect = null;
        for (int depth = 0; depth < SCAN_DEPTH; depth++) {
            BlockPos center = origin.relative(direction, depth);
            for (int first = -1; first <= 1; first++) {
                for (int second = -1; second <= 1; second++) {
                    BlockPos pos = switch (direction.getAxis()) {
                        case X -> center.offset(0, first, second);
                        case Y -> center.offset(first, 0, second);
                        case Z -> center.offset(first, second, 0);
                    };
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                    for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, null, ItemStack.EMPTY)) {
                        AspectList aspects = AspectHelper.getObjectTags(drop);
                        if (aspects == null) {
                            continue;
                        }
                        for (Aspect aspect : aspects.getAspects()) {
                            if (aspects.getAmount(aspect) > highest) {
                                highest = aspects.getAmount(aspect);
                                highestAspect = aspect;
                            }
                        }
                    }
                }
            }
        }
        AspectList result = new AspectList();
        if (highestAspect != null) {
            result.add(highestAspect, highest);
        }
        return result;
    }

    public static boolean onBreakBlock(Level level, Player player, ItemStack stack, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel) || player.isShiftKeyDown() || player.isCreative()) {
            return false;
        }
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, stack);
        int fortune = MiningUtils.enchantmentLevel(level, stack, Enchantments.FORTUNE);
        float chance = 0.275F + fortune * 0.075F;
        boolean replaced = false;
        boolean ping = false;
        List<ItemStack> results = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (ItemStack.isSameItem(drop, MiningUtils.findSpecialMiningResult(drop, 1.0F, level.getRandom()))) {
                results.add(drop);
                continue;
            }
            replaced = true;
            ItemStack result = MiningUtils.findSpecialMiningResult(drop, chance, level.getRandom());
            if (!ItemStack.isSameItem(drop, result)) {
                ping = true;
            }
            results.add(result);
        }
        if (!replaced) {
            return false;
        }
        serverLevel.destroyBlock(pos, false, player);
        for (ItemStack result : results) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, result));
        }
        if (ping) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F, 0.7F + level.getRandom().nextFloat() * 0.2F);
        }
        return true;
    }
}
