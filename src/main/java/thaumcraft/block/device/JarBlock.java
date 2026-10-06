package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.blockentity.BrainJarBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.item.FilledJarItem;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

public class JarBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);
    private static final VoxelShape COLLISION = Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);

    private final boolean brain;

    public JarBlock(boolean brain, Properties properties) {
        super(properties);
        this.brain = brain;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return brain ? new BrainJarBlockEntity(pos, state) : new JarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityTicker<?> ticker;
        if (brain && type == ModBlockEntities.BRAIN_JAR.get()) {
            ticker = level.isClientSide()
                ? (BlockEntityTicker<BrainJarBlockEntity>) BrainJarBlockEntity::clientTick
                : (BlockEntityTicker<BrainJarBlockEntity>) BrainJarBlockEntity::serverTick;
        } else if (!brain && type == ModBlockEntities.WARDED_JAR.get() && level.isClientSide()) {
            ticker = (BlockEntityTicker<JarBlockEntity>) JarBlockEntity::clientTick;
        } else {
            return null;
        }
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            jar.release(serverLevel);
        } else {
            jar.startEatDelay();
            level.playLocalSound(pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.2F, 1.0F, false);
            jar.wobble(hitResult.getDirection(), level.getRandom());
        }
        return InteractionResult.SUCCESS;
    }

    public static void dropContents(Level level, BlockPos pos, JarBlockEntity jar) {
        ItemStack drop;
        if (jar.getAmount() > 0) {
            drop = new ItemStack(ModItems.FILLED_JAR.get());
            FilledJarItem.setContents(drop, jar.getContents());
        } else {
            drop = new ItemStack(ModItems.WARDED_JAR.get());
        }
        Block.popResource(level, pos, drop);
    }

    public static void dropExperience(ServerLevel level, BlockPos pos, BrainJarBlockEntity jar) {
        if (jar.getXp() > 0) {
            ExperienceOrb.award(level, Vec3.atLowerCornerOf(pos), jar.getXp());
        }
    }
}
