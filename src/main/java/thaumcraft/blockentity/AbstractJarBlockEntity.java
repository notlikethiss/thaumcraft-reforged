package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModSounds;

public abstract class AbstractJarBlockEntity extends TcBlockEntity {
    public float wobbleX;
    public float wobbleZ;
    protected int spazAttack;

    protected AbstractJarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected boolean canSpaz() {
        return false;
    }

    public void wobble(Direction side, RandomSource random) {
        switch (side) {
            case NORTH -> wobbleZ = 5.0F;
            case SOUTH -> wobbleZ = -5.0F;
            case WEST -> wobbleX = 5.0F;
            case EAST -> wobbleX = -5.0F;
            default -> {
                switch (random.nextInt(4)) {
                    case 0 -> wobbleZ = 5.0F;
                    case 1 -> wobbleZ = -5.0F;
                    case 2 -> wobbleX = 5.0F;
                    default -> wobbleX = -5.0F;
                }
            }
        }
    }

    public boolean isStill() {
        return wobbleX == 0.0F && wobbleZ == 0.0F;
    }

    protected void clientTick(Level level) {
        RandomSource random = level.getRandom();
        if (canSpaz() && spazAttack == 0 && random.nextInt(2000) == 0) {
            spazAttack = 5 + random.nextInt(50);
        }
        if (spazAttack > 0) {
            spazAttack--;
            if (isStill()) {
                level.playLocalSound(worldPosition, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.1F, 1.0F, false);
                switch (random.nextInt(4)) {
                    case 0 -> wobbleZ = 2 + random.nextInt(5);
                    case 1 -> wobbleZ = -2 - random.nextInt(5);
                    case 2 -> wobbleX = 2 + random.nextInt(5);
                    default -> wobbleX = -2 - random.nextInt(5);
                }
            }
        }
        wobbleX -= Math.signum(wobbleX);
        wobbleZ -= Math.signum(wobbleZ);
    }
}
