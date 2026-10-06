package thaumcraft.entity.golem.goal;

import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.entity.golem.GolemWorker;

public class JarGotoGoal extends GolemMoveGoal {
    private final BiPredicate<JarBlockEntity, Boolean> accepts;
    private @Nullable BlockPos destination;

    public JarGotoGoal(GolemWorker golem, BiPredicate<JarBlockEntity, Boolean> accepts) {
        super(golem);
        this.accepts = accepts;
    }

    @Override
    public boolean canUse() {
        int range = GolemGoals.searchRange(this.golem);
        JarBlockEntity jar = JarGoals.findJar(this.golem, range * range, Double.MAX_VALUE, this.accepts);
        this.destination = jar == null ? null : jar.getBlockPos();
        return this.destination != null;
    }

    @Override
    public void start() {
        startMoving(this.destination.getX(), this.destination.getY(), this.destination.getZ());
    }

    @Override
    public void stop() {
        this.destination = null;
        this.count = 0;
    }
}
