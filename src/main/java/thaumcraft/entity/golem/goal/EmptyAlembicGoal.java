package thaumcraft.entity.golem.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.entity.golem.GolemUtils;
import thaumcraft.entity.golem.TallowGolem;
import thaumcraft.item.EssenceItem;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModItems;

public class EmptyAlembicGoal extends Goal {
    private final TallowGolem golem;
    private int delayUntil;

    public EmptyAlembicGoal(TallowGolem golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private boolean canHold(Aspect aspect) {
        ItemStack essences = this.golem.getProvideStack();
        return essences.isEmpty() || EssenceItem.getAspect(essences) == aspect && essences.getCount() < this.golem.getMaxCarried();
    }

    @Override
    public boolean canUse() {
        if (!this.golem.getInventory().getItem(0).is(ModItems.ESSENTIA_PHIAL.get())
            || !this.golem.getNavigation().isDone()
            || this.golem.tickCount < this.delayUntil) {
            return false;
        }
        BlockPos home = this.golem.getHomeContainerPos();
        if (this.golem.distanceToSqr(home.getX() + 0.5F, home.getY() + 0.5F, home.getZ() + 0.5F) > 6.0) {
            return false;
        }
        for (AlembicBlockEntity alembic : GolemUtils.alembicsAroundCrucible(this.golem.level(), home)) {
            if (alembic.getAmount() >= 8 && canHold(alembic.getAspect())) {
                this.delayUntil = this.golem.tickCount + 20;
                return true;
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
        boolean did = false;
        for (AlembicBlockEntity alembic : GolemUtils.alembicsAroundCrucible(this.golem.level(), this.golem.getHomeContainerPos())) {
            ItemStack phials = this.golem.getInventory().getItem(0);
            if (alembic.getAmount() >= 8 && !phials.isEmpty() && canHold(alembic.getAspect())) {
                ItemStack essences = this.golem.getProvideStack();
                if (essences.isEmpty()) {
                    essences = new ItemStack(ModItems.ESSENCE.get());
                    essences.set(ModDataComponents.ESSENCE_ASPECT.get(), alembic.getAspect());
                } else {
                    essences = essences.copyWithCount(essences.getCount() + 1);
                }
                alembic.takeFromSource(alembic.getAspect(), 8);
                phials.shrink(1);
                this.golem.getInventory().setChanged();
                this.golem.setProvideStack(essences);
                did = true;
                break;
            }
        }
        if (did) {
            this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.25F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
        }
        this.delayUntil = this.golem.tickCount + 2;
    }
}
