package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import javax.annotation.Nullable;
import thaumcraft.blockentity.NitorBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.fx.Fx;

public class NitorBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

    public NitorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? (tickLevel, tickPos, tickState, blockEntity) -> clientTick(tickLevel, tickPos) : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Fx.get().sparkle(
            level,
            pos.getX() + 0.5F,
            pos.getY() + 0.5F,
            pos.getZ() + 0.5F,
            pos.getX() + 0.5F + (random.nextFloat() - random.nextFloat()) / 3.0F,
            pos.getY() + 0.5F + (random.nextFloat() - random.nextFloat()) / 3.0F,
            pos.getZ() + 0.5F + (random.nextFloat() - random.nextFloat()) / 3.0F,
            1.0F,
            6,
            3,
            0.05F
        );
    }

    public static void clientTick(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        if (random.nextInt(3) == 0) {
            Fx.get().wispFX3(
                level,
                pos.getX() + 0.5F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.5F,
                pos.getX() + 0.3F + random.nextFloat() * 0.4F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.3F + random.nextFloat() * 0.4F,
                0.5F,
                4,
                true,
                -0.025F
            );
        }
        if (random.nextInt(5) == 0) {
            Fx.get().wispFX3(
                level,
                pos.getX() + 0.5F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.5F,
                pos.getX() + 0.4F + random.nextFloat() * 0.2F,
                pos.getY() + 0.5F,
                pos.getZ() + 0.4F + random.nextFloat() * 0.2F,
                0.25F,
                1,
                true,
                -0.02F
            );
        }
    }
}
