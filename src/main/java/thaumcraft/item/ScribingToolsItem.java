package thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.block.device.ResearchTableBlock;
import thaumcraft.registry.ModBlocks;

public class ScribingToolsItem extends Item {
    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public ScribingToolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(ModBlocks.TABLE.get())) {
            return InteractionResult.PASS;
        }
        for (Direction direction : DIRECTIONS) {
            BlockPos other = pos.relative(direction);
            if (!level.getBlockState(other).is(ModBlocks.TABLE.get())) {
                continue;
            }
            if (!level.isClientSide()) {
                Block table = ModBlocks.RESEARCH_TABLE.get();
                level.setBlockAndUpdate(pos, table.defaultBlockState()
                    .setValue(ResearchTableBlock.FACING, direction)
                    .setValue(ResearchTableBlock.PART, ResearchTableBlock.Part.MAIN));
                level.setBlockAndUpdate(other, table.defaultBlockState()
                    .setValue(ResearchTableBlock.FACING, direction.getOpposite())
                    .setValue(ResearchTableBlock.PART, ResearchTableBlock.Part.SIDE));
                Player player = context.getPlayer();
                if (player != null) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
