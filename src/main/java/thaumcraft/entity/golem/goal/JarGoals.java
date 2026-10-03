package thaumcraft.entity.golem.goal;

import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemUtils;

final class JarGoals {
    private JarGoals() {
    }

    static @Nullable JarBlockEntity findJar(GolemBase golem, double markerDistanceSq, double jarDistanceSq, BiPredicate<JarBlockEntity, Boolean> accepts) {
        for (boolean empty : new boolean[] {false, true}) {
            JarBlockEntity found = null;
            for (BlockPos marker : GolemUtils.markersForGolem(golem, markerDistanceSq)) {
                for (JarBlockEntity jar : GolemUtils.adjacentJars(golem.level(), marker)) {
                    BlockPos pos = jar.getBlockPos();
                    if (golem.distanceToSqr(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F) <= jarDistanceSq
                        && (empty ? jar.getAmount() == 0 : jar.getAmount() > 0)
                        && accepts.test(jar, empty)) {
                        found = jar;
                        if (jarDistanceSq < Double.MAX_VALUE) {
                            return found;
                        }
                    }
                }
            }
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
