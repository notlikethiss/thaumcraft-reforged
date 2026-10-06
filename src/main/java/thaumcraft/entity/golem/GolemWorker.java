package thaumcraft.entity.golem;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public abstract class GolemWorker extends GolemBase {
    public ItemStack itemWatched = ItemStack.EMPTY;

    protected GolemWorker(EntityType<? extends GolemWorker> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public @Nullable List<ItemStack> getMissingItems() {
        return null;
    }

    public boolean hasHomeInventory() {
        return GolemInventories.hasItems(this.level(), getHomeContainerPos(), getHomeFacing());
    }

    public int homeCount(ItemStack stack) {
        return GolemInventories.count(this.level(), getHomeContainerPos(), getHomeFacing(), stack);
    }

    public boolean homeContains(Predicate<ItemStack> filter) {
        return GolemInventories.contains(this.level(), getHomeContainerPos(), getHomeFacing(), filter);
    }

    public int homeInsert(ItemStack stack, boolean simulate) {
        return GolemInventories.insert(this.level(), getHomeContainerPos(), getHomeFacing(), stack, simulate);
    }

    public ItemStack homeExtractFirst(Predicate<ItemStack> filter, int maxAmount) {
        return GolemInventories.extractFirst(this.level(), getHomeContainerPos(), getHomeFacing(), filter, maxAmount);
    }

    public boolean hasSomething() {
        return this.inventory.hasSomething();
    }

    public ItemStack getProvideStack() {
        return getCarried();
    }

    public void setProvideStack(ItemStack stack) {
        setCarried(stack);
    }

    public boolean isToggled() {
        return false;
    }
}
