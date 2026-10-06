package thaumcraft.entity.golem.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemWorker;

public class ItemEntityGotoGoal extends GolemMoveGoal {
    private final GolemWorker worker;
    private @Nullable Entity target;

    public ItemEntityGotoGoal(GolemWorker golem) {
        super(golem);
        this.worker = golem;
    }

    @Override
    public boolean canUse() {
        int range = this.worker.getCore() == 3 ? 16 : 10;
        if (this.worker.hasDecoration("G")) {
            range = (int) (range * 1.2F);
        }
        BlockPos home = this.worker.getRestrictCenter();
        AABB area = new AABB(home).inflate(range);
        double best = Double.MAX_VALUE;
        this.target = null;
        for (Entity entity : this.worker.level().getEntities(this.worker, area)) {
            ItemStack stack = GolemItemEntities.stackOf(entity);
            if (stack.isEmpty()
                || GolemItemEntities.pickupDelay(entity) >= 5
                || !GolemItemEntities.wanted(this.worker, stack)
                || !GolemItemEntities.canCarry(this.worker, stack, true)) {
                continue;
            }
            double distance = entity.distanceToSqr(home.getX() + 0.5F, home.getY() + 0.5F, home.getZ() + 0.5F);
            if (distance < best && distance <= range * range) {
                best = distance;
                this.target = entity;
            }
        }
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.target != null && this.target.isAlive();
    }

    @Override
    public void start() {
        startMoving(this.target.getX(), this.target.getY(), this.target.getZ());
    }

    @Override
    public void stop() {
        this.count = 0;
        this.target = null;
    }
}
