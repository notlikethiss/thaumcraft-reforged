package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.entity.golem.TallowGolem;

public class FillCrucibleGoal extends Goal {
    private final TallowGolem golem;

    public FillCrucibleGoal(TallowGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getInventory().getItem(0).is(Items.WATER_BUCKET) || !this.golem.getNavigation().isDone()) {
            return false;
        }
        BlockPos pos = this.golem.getHomeContainerPos();
        if (this.golem.distanceToSqr(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F) > 6.0) {
            return false;
        }
        return this.golem.level().getBlockEntity(pos) instanceof CrucibleBlockEntity crucible && !crucible.hasLiquid();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        if (this.golem.level().getBlockEntity(this.golem.getHomeContainerPos()) instanceof CrucibleBlockEntity crucible && !crucible.hasLiquid()) {
            crucible.fill();
            this.golem.setCarried(new ItemStack(Items.BUCKET));
            this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.25F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
        }
    }
}
