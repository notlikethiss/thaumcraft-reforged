package thaumcraft.aspect;

import org.jspecify.annotations.Nullable;

public interface EssentiaContainer extends AspectSource {
    @Nullable Aspect getContainedAspect();

    int getContainedAmount();

    int getMaxAmount();
}
