package thaumcraft.item.wand;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public class ExcavationState {
    public @Nullable BlockPos target;
    public float breakCount;
    public long soundDelayUntil;
    public int mined;

    public void resetTarget() {
        target = null;
        breakCount = 0.0F;
    }
}
