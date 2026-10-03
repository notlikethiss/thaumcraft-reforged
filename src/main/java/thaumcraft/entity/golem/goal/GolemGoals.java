package thaumcraft.entity.golem.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemUtils;

final class GolemGoals {
    private GolemGoals() {
    }

    static int searchRange(GolemBase golem) {
        int range = golem.getCore() == 3 ? 32 : 20;
        if (golem.hasDecoration("G")) {
            range = (int) (range * 1.2F);
        }
        return range;
    }

    static @Nullable BlockPos nearest(GolemBase golem, List<GolemUtils.MarkedContainer> containers) {
        BlockPos home = golem.getHomeContainerPos();
        int range = searchRange(golem);
        double best = Double.MAX_VALUE;
        BlockPos result = null;
        for (GolemUtils.MarkedContainer container : containers) {
            double distance = golem.distanceToSqr(Vec3.atCenterOf(container.pos()));
            if (distance < best && distance <= range * range && !container.pos().equals(home)) {
                best = distance;
                result = container.pos();
            }
        }
        return result;
    }
}
