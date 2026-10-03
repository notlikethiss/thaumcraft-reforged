package thaumcraft.entity.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

public abstract class GolemWorker extends GolemBase {
    public ItemStack itemWatched = ItemStack.EMPTY;

    protected GolemWorker(EntityType<? extends GolemWorker> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }
}
