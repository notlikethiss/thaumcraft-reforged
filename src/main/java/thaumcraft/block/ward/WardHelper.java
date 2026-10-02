package thaumcraft.block.ward;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import thaumcraft.Config;
import thaumcraft.blockentity.OwnedBlockEntity;

public final class WardHelper {
    private WardHelper() {
    }

    public static void setOwner(Level level, BlockPos pos, @Nullable LivingEntity by) {
        if (by instanceof Player player && level.getBlockEntity(pos) instanceof OwnedBlockEntity owned) {
            owned.setOwner(player);
        }
    }

    public static void markCreativeRemoval(Level level, BlockPos pos, Player player) {
        if (Config.wardedStone() && !level.isClientSide() && player.isCreative() && level.getBlockEntity(pos) instanceof OwnedBlockEntity owned) {
            owned.markSafeToRemove();
        }
    }

    public static InteractionResult removeWithWand(Level level, BlockPos pos, BlockState state, Player player, @Nullable ItemStack drop) {
        if (!(level.getBlockEntity(pos) instanceof OwnedBlockEntity owned) || !owned.canRemoveWard(player)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        owned.markSafeToRemove();
        if (drop != null && !drop.isEmpty()) {
            Block.popResource(level, pos, drop);
        }
        level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_DESTROY_BLOCK, pos, Block.getId(state));
        level.removeBlock(pos, false);
        return InteractionResult.SUCCESS;
    }
}
