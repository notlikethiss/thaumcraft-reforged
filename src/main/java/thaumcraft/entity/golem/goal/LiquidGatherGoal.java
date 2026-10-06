package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entity.golem.DecantingGolem;
import thaumcraft.entity.golem.GolemInventories;
import thaumcraft.entity.golem.GolemUtils;

public class LiquidGatherGoal extends Goal {
    private final DecantingGolem golem;

    public LiquidGatherGoal(DecantingGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private void playSound(float volume) {
        this.golem.playSound(SoundEvents.GENERIC_SWIM, volume, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
    }

    @Override
    public boolean canUse() {
        List<DecantingGolem.MissingLiquid> missing = this.golem.getMissingLiquids();
        if (missing.isEmpty() || this.golem.getAmount() > DecantingGolem.CAPACITY - DecantingGolem.SPACE_PER_UNIT || !this.golem.getNavigation().isDone()) {
            return false;
        }
        Level level = this.golem.level();
        BlockPos home = this.golem.getHomeContainerPos();
        Fluid watched = this.golem.getWatchedLiquid();
        for (BlockPos marker : GolemUtils.markersForGolem(this.golem, 8.0)) {
            for (BlockPos pos : GolemUtils.adjacentLiquid(level, marker)) {
                if (pos.equals(home) || this.golem.distanceToSqr(Vec3.atLowerCornerOf(pos)) >= 6.0) {
                    continue;
                }
                int amount = this.golem.getAmount();
                Fluid carried = this.golem.getLiquid();
                boolean canTakeSource = amount == 0
                    || amount + DecantingGolem.SPACE_PER_UNIT * 4 <= DecantingGolem.CAPACITY && (carried == Fluids.WATER || carried == Fluids.LAVA);
                boolean water = GolemUtils.isSource(level, pos, Fluids.WATER) && (watched == null || watched == Fluids.WATER);
                boolean lava = GolemUtils.isSource(level, pos, Fluids.LAVA) && (watched == null || watched == Fluids.LAVA);
                if (canTakeSource && (water || lava)) {
                    this.golem.setLiquid(water ? Fluids.WATER : Fluids.LAVA);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    this.golem.setAmount(amount + DecantingGolem.SPACE_PER_UNIT * 4);
                    playSound(0.2F);
                    continue;
                }
                Direction side = GolemUtils.sideFacing(pos, marker);
                Fluid available = GolemInventories.firstFluid(level, pos, side);
                if (available == null || GolemInventories.drainFluid(level, pos, side, available, DecantingGolem.UNIT, true) != DecantingGolem.UNIT) {
                    continue;
                }
                for (DecantingGolem.MissingLiquid liquid : missing) {
                    if (liquid.fluid().isSame(available)
                        && this.golem.getAmount() / DecantingGolem.SPACE_PER_UNIT * DecantingGolem.UNIT < liquid.space()
                        && GolemInventories.drainFluid(level, pos, side, available, DecantingGolem.UNIT, false) > 0) {
                        this.golem.setLiquid(available);
                        this.golem.setAmount(this.golem.getAmount() + DecantingGolem.SPACE_PER_UNIT);
                        playSound(0.15F);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
