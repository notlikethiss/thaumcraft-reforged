package thaumcraft.block.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.fx.Fx;

public class MagicalFlowerBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);
    private final boolean cinderpearl;

    public MagicalFlowerBlock(boolean cinderpearl, Properties properties) {
        super(properties);
        this.cinderpearl = cinderpearl;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return cinderpearl && state.is(Blocks.SAND)
            || state.is(Blocks.GRASS_BLOCK)
            || state.is(Blocks.DIRT)
            || state.is(Blocks.FARMLAND);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!cinderpearl && random.nextInt(3) == 0) {
            float red = 0.3F + level.getRandom().nextFloat() * 0.3F;
            float green = 0.7F + level.getRandom().nextFloat() * 0.3F;
            float blue = 0.7F + level.getRandom().nextFloat() * 0.3F;
            double x = pos.getX() + 0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F;
            double y = pos.getY() + 0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.15F;
            double z = pos.getZ() + 0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F;
            Fx.get().wisp(level, x, y, z, 0.2F, red, green, blue, false);
        }
        if (cinderpearl && random.nextBoolean()) {
            double x = pos.getX() + 0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F;
            double y = pos.getY() + 0.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F;
            double z = pos.getZ() + 0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
