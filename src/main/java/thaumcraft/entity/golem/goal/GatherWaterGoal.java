package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemInventories;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.TallowGolem;

public class GatherWaterGoal extends Goal {
    private final TallowGolem golem;
    private @Nullable BlockPos water;
    private @Nullable BlockPos marker;

    public GatherWaterGoal(TallowGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getInventory().getItem(0).is(Items.BUCKET) || !this.golem.getNavigation().isDone()) {
            return false;
        }
        for (BlockPos marker : GolemUtils.markersForGolem(this.golem, 8.0)) {
            for (BlockPos pos : GolemUtils.adjacentWater(this.golem.level(), marker)) {
                if (this.golem.distanceToSqr(Vec3.atLowerCornerOf(pos)) < 6.0) {
                    this.water = pos;
                    this.marker = marker;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        Level level = this.golem.level();
        if (GolemUtils.isSource(level, this.water, Fluids.WATER)) {
            level.setBlock(this.water, Blocks.AIR.defaultBlockState(), 3);
            this.golem.setCarried(new ItemStack(Items.WATER_BUCKET));
            return;
        }
        Direction side = GolemUtils.sideFacing(this.water, this.marker);
        if (GolemInventories.drainFluid(level, this.water, side, Fluids.WATER, 1000, true) == 1000) {
            GolemInventories.drainFluid(level, this.water, side, Fluids.WATER, 1000, false);
            this.golem.setCarried(new ItemStack(Items.WATER_BUCKET));
        }
    }
}
