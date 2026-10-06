package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.GolemWorker;

public class ItemEntityPickupGoal extends Goal {
    private final GolemWorker golem;
    private @Nullable Entity target;
    private int count;

    public ItemEntityPickupGoal(GolemWorker golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getNavigation().isDone()) {
            return false;
        }
        for (Entity entity : this.golem.level().getEntities(this.golem, this.golem.getBoundingBox().inflate(1.5))) {
            ItemStack stack = GolemItemEntities.stackOf(entity);
            if (!stack.isEmpty()
                && GolemItemEntities.pickupDelay(entity) < 5
                && GolemItemEntities.wanted(this.golem, stack)
                && GolemItemEntities.canCarry(this.golem, stack, false)) {
                this.target = entity;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.count > 0 && !this.golem.getNavigation().isDone() && this.target != null && this.target.isAlive();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        this.count--;
    }

    @Override
    public void start() {
        this.count = 200;
        if (this.target == null) {
            return;
        }
        ItemStack stack = GolemItemEntities.stackOf(this.target).copy();
        int amount = Math.min(stack.getCount(), this.golem.getCarrySpace());
        if (amount <= 0) {
            return;
        }
        ItemStack carried = this.golem.getCarried();
        ItemStack newCarried = carried.isEmpty() ? stack.copyWithCount(amount) : carried.copyWithCount(carried.getCount() + amount);
        this.golem.setCarried(newCarried);
        stack.shrink(amount);
        GolemItemEntities.setStack(this.target, stack);
        this.target.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }
}
