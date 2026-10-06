package thaumcraft.block.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.aura.AuraManager;
import thaumcraft.aura.NodeType;
import thaumcraft.world.gen.GreatwoodTreeGenerator;
import thaumcraft.world.gen.SilverwoodTreeGenerator;

public class MagicalSaplingBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);
    private final boolean silverwood;

    public MagicalSaplingBlock(boolean silverwood, Properties properties) {
        super(properties);
        this.silverwood = silverwood;
    }

    public boolean isSilverwood() {
        return silverwood;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return;
        }
        if (!silverwood && random.nextInt(25) == 0) {
            growGreatTree(level, pos, random);
        } else if (silverwood && random.nextInt(50) == 0) {
            growSilverTree(level, pos, random);
        }
    }

    public void growGreatTree(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState sapling = level.getBlockState(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (!new GreatwoodTreeGenerator(true).generate(level, new Random(random.nextLong()), pos, false)) {
            level.setBlock(pos, sapling, Block.UPDATE_NONE);
        }
    }

    public void growSilverTree(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState sapling = level.getBlockState(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        int value = random.nextInt(50) + 50;
        int cost = (int) (value * 1.5F);
        if (AuraManager.decreaseClosestAura(level, pos.getX(), pos.getY(), pos.getZ(), cost, false)
            && new SilverwoodTreeGenerator(true).generate(level, new Random(random.nextLong()), pos)) {
            if (AuraManager.decreaseClosestAura(level, pos.getX(), pos.getY(), pos.getZ(), cost)) {
                AuraManager.registerAuraNode(level, value, NodeType.PURE, pos.above());
            }
        } else {
            level.setBlock(pos, sapling, Block.UPDATE_NONE);
        }
    }
}
