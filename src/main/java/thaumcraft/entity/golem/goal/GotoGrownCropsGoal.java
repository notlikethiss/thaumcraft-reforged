package thaumcraft.entity.golem.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.CropHelper;
import thaumcraft.entity.golem.GolemBase;

public class GotoGrownCropsGoal extends GolemMoveGoal {
    private @Nullable BlockPos crop;

    public GotoGrownCropsGoal(GolemBase golem) {
        super(golem);
    }

    @Override
    public boolean canUse() {
        this.crop = findGrownCrop();
        return this.crop != null;
    }

    @Override
    public void start() {
        startMoving(this.crop.getX(), this.crop.getY(), this.crop.getZ());
    }

    @Override
    public void stop() {
        this.count = 0;
        this.crop = null;
    }

    private @Nullable BlockPos findGrownCrop() {
        RandomSource random = this.golem.getRandom();
        Level level = this.golem.level();
        int range = this.golem.getCore() == 3 ? 16 : 10;
        if (this.golem.hasDecoration("G")) {
            range = (int) (range * 1.2F);
        }
        for (int attempt = 0; attempt < range; attempt++) {
            int x = Mth.floor(this.golem.getX() + random.nextInt(range * 2) - range);
            int y = Mth.floor(this.golem.getBoundingBox().minY + random.nextInt(range / 2) - range / 4);
            int z = Mth.floor(this.golem.getZ() + random.nextInt(range * 2) - range);
            int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > height) {
                y = height;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (CropHelper.isGrownCrop(level, pos)) {
                return pos;
            }
        }
        return null;
    }
}
