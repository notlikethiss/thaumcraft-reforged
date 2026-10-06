package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.blockentity.ArcaneWorktableBlockEntity;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneWorktableBlock extends Block implements EntityBlock {
    public ArcaneWorktableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneWorktableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.ARCANE_WORKTABLE.get()) {
            return null;
        }
        BlockEntityTicker<MagicWorkbenchBlockEntity> ticker = MagicWorkbenchBlockEntity::serverTick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof ArcaneWorktableBlockEntity workbench)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(workbench, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
