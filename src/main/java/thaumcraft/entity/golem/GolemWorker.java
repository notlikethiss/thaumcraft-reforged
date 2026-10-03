package thaumcraft.entity.golem;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
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

    public @Nullable ResourceHandler<ItemResource> homeHandler() {
        return GolemUtils.handler(this.level(), getHomeContainerPos(), getHomeFacing());
    }

    public boolean isToggled() {
        return false;
    }
}
