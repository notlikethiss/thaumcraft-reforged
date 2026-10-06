package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.CropHelper;
import thaumcraft.entity.golem.GolemBase;

public class HarvestCropGoal extends Goal {
    private static final int[] OFFSET_X = {0, 0, 1, 1, -1, 0, -1, -1, 1};
    private static final int[] OFFSET_Z = {0, 1, 0, 1, 0, -1, -1, 1, -1};

    private final GolemBase golem;
    private @Nullable BlockPos crop;
    private int delay;

    public HarvestCropGoal(GolemBase golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.delay > 0) {
            this.delay--;
        }
        if (this.delay > 0 || !this.golem.getNavigation().isDone()) {
            return false;
        }
        BlockPos center = BlockPos.containing(this.golem.getX(), this.golem.getBoundingBox().minY, this.golem.getZ());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (CropHelper.isGrownCrop(this.golem.level(), pos)) {
                        this.crop = pos;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.golem.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.delay = 2;
        if (this.crop != null && this.golem.level() instanceof ServerLevel level) {
            harvest(level, this.crop);
        }
        this.crop = null;
        this.golem.startActionTimer();
    }

    private void harvest(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block instanceof CocoaBlock
            ? List.of(new ItemStack(Items.COCOA_BEANS), new ItemStack(Items.COCOA_BEANS), new ItemStack(Items.COCOA_BEANS))
            : Block.getDrops(state, level, pos, level.getBlockEntity(pos));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        if (this.golem.getCore() == 2) {
            replant(level, pos, state, drops);
        }
        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    private void replant(ServerLevel level, BlockPos pos, BlockState harvested, List<ItemStack> drops) {
        Block block = harvested.getBlock();
        for (ItemStack stack : drops) {
            if (stack.is(Items.COCOA_BEANS) && block instanceof CocoaBlock) {
                BlockState cocoa = harvested.setValue(CocoaBlock.AGE, 0);
                if (level.getBlockState(pos.relative(harvested.getValue(CocoaBlock.FACING))).is(BlockTags.JUNGLE_LOGS)) {
                    stack.shrink(1);
                    level.setBlock(pos, cocoa, 3);
                    level.levelEvent(2001, pos, Block.getId(cocoa));
                }
                return;
            }
            if ((stack.is(Items.SUGAR_CANE) && block == Blocks.SUGAR_CANE) || (stack.is(Items.CACTUS) && block == Blocks.CACTUS)) {
                BlockState plant = block.defaultBlockState();
                if (plant.canSurvive(level, pos)) {
                    stack.shrink(1);
                    level.setBlock(pos, plant, 3);
                    level.levelEvent(2001, pos, Block.getId(plant));
                    return;
                }
            }
            for (int index = 0; index < 9 && !stack.isEmpty(); index++) {
                BlockPos target = pos.offset(OFFSET_X[index], 0, OFFSET_Z[index]);
                if (stack.getItem() instanceof BlockItem item && item.getBlock() == block && level.isEmptyBlock(target)) {
                    BlockState plant = block.defaultBlockState();
                    if (plant.canSurvive(level, target)) {
                        stack.shrink(1);
                        level.setBlock(target, plant, 3);
                        level.levelEvent(2001, target, Block.getId(plant));
                    }
                }
            }
        }
    }
}
