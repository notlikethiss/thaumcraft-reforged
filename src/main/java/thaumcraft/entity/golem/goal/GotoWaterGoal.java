package thaumcraft.entity.golem.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.TallowGolem;

public class GotoWaterGoal extends GolemMoveGoal {
    private final TallowGolem tallow;
    private @Nullable BlockPos water;

    public GotoWaterGoal(TallowGolem golem) {
        super(golem);
        this.tallow = golem;
    }

    @Override
    public boolean canUse() {
        if (!this.tallow.getInventory().getItem(0).is(Items.BUCKET)) {
            return false;
        }
        int range = GolemGoals.searchRange(this.tallow);
        this.water = null;
        for (BlockPos marker : GolemUtils.markersForGolem(this.tallow, range * range)) {
            for (BlockPos pos : GolemUtils.adjacentWater(this.tallow.level(), marker)) {
                if (this.water == null || this.tallow.distanceToSqr(Vec3.atLowerCornerOf(pos)) < this.tallow.distanceToSqr(Vec3.atLowerCornerOf(this.water))) {
                    this.water = pos;
                }
            }
        }
        return this.water != null;
    }

    @Override
    public void start() {
        startMoving(this.water.getX(), this.water.getY(), this.water.getZ());
    }

    @Override
    public void stop() {
        this.count = 0;
    }
}
