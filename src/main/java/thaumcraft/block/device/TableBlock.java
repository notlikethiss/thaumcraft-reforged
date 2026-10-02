package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import thaumcraft.block.WandTarget;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.registry.ModBlocks;

public class TableBlock extends Block implements WandTarget {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public TableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, ModBlocks.ARCANE_WORKTABLE.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof MagicWorkbenchBlockEntity workbench) {
                workbench.setItem(MagicWorkbenchBlockEntity.WAND_SLOT, wand.copy());
                player.setItemInHand(player.getMainHandItem() == wand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.15F, 0.5F);
        }
        return InteractionResult.SUCCESS;
    }
}
