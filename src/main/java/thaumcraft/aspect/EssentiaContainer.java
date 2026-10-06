package thaumcraft.aspect;

import javax.annotation.Nullable;

public interface EssentiaContainer extends AspectSource {
    @Nullable Aspect getContainedAspect();

    int getContainedAmount();

    int getMaxAmount();

    default boolean acceptsPhials() {
        return false;
    }
}
