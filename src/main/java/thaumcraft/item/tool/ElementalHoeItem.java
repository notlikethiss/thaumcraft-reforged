package thaumcraft.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.item.ModMaterials;
import thaumcraft.block.world.MagicalSaplingBlock;
import thaumcraft.fx.Fx;
import thaumcraft.lib.Utils;
import thaumcraft.registry.ModSounds;

public class ElementalHoeItem extends HoeItem {
    private static final int GREAT_TREE_COST = 20;
    private static final int SILVER_TREE_COST = 150;
    private static final int BONE_MEAL_COST = 5;

    public ElementalHoeItem(Properties properties) {
        super(ModMaterials.ELEMENTAL_TOOL, properties.attributes(DiggerItem.createAttributes(ModMaterials.ELEMENTAL_TOOL, -3.0F, -1.0F)));
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        boolean did = false;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                BlockPos target = pos.offset(offsetX, 0, offsetZ);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), context.getClickedFace(), target, false);
                UseOnContext targetContext = new UseOnContext(level, player, context.getHand(), stack, hit);
                if (super.useOn(targetContext).consumesAction()) {
                    Fx.get().blockSparkle(level, target.getX(), target.getY(), target.getZ(), 0, 2);
                    did = true;
                }
            }
        }
        if (!did) {
            did = growAt(level, player, context, stack, pos);
            if (did) {
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.WAND.value(), SoundSource.PLAYERS, 0.75F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            }
        }
        return did ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean growAt(Level level, Player player, UseOnContext context, ItemStack stack, BlockPos pos) {
        if (Utils.useBonemealAtLoc(level, pos)) {
            stack.hurtAndBreak(BONE_MEAL_COST, player, LivingEntity.getSlotForHand(context.getHand()));
            Fx.get().blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), 0, 3);
            return true;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MagicalSaplingBlock sapling)) {
            return false;
        }
        int cost = sapling.isSilverwood() ? SILVER_TREE_COST : GREAT_TREE_COST;
        if (stack.getDamageValue() + cost > stack.getMaxDamage()) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (sapling.isSilverwood()) {
                sapling.growSilverTree(serverLevel, pos, level.getRandom());
            } else {
                sapling.growGreatTree(serverLevel, pos, level.getRandom());
            }
            stack.hurtAndBreak(cost, player, LivingEntity.getSlotForHand(context.getHand()));
        }
        Fx.get().blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), 0, 2);
        return true;
    }
}
