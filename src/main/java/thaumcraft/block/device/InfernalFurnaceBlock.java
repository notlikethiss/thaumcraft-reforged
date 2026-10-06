package thaumcraft.block.device;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.blockentity.InfernalFurnaceBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class InfernalFurnaceBlock extends Block implements EntityBlock {
    public static final IntegerProperty X = IntegerProperty.create("x", 0, 2);
    public static final IntegerProperty Y = IntegerProperty.create("y", 0, 2);
    public static final IntegerProperty Z = IntegerProperty.create("z", 0, 2);
    private static final VoxelShape LAVA = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

    private static boolean disassembling;

    public InfernalFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(X, 0).setValue(Y, 0).setValue(Z, 0).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(X, Y, Z, HorizontalDirectionalBlock.FACING);
    }

    public static boolean isLava(BlockState state) {
        return state.getValue(X) == 1 && state.getValue(Y) == 1 && state.getValue(Z) == 1;
    }

    public static boolean isGrate(BlockState state) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        return state.getValue(Y) == 1 && state.getValue(X) == 1 + facing.getStepX() && state.getValue(Z) == 1 + facing.getStepZ();
    }

    public static BlockPos center(BlockPos pos, BlockState state) {
        return pos.offset(1 - state.getValue(X), 1 - state.getValue(Y), 1 - state.getValue(Z));
    }

    public static BlockState restoreState(BlockState state) {
        if (isLava(state)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (isGrate(state)) {
            return Blocks.IRON_BARS.defaultBlockState();
        }
        boolean corner = state.getValue(X) != 1 && state.getValue(Z) != 1;
        return corner ? Blocks.NETHER_BRICKS.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
    }

    public static int lightLevel(BlockState state) {
        return isLava(state) || isGrate(state) ? 13 : 3;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isLava(state) ? LAVA : Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (isLava(state)) {
            return LAVA;
        }
        if (isGrate(state)) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return switch (facing) {
                case EAST -> Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0);
                case WEST -> Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0);
                case SOUTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0);
                default -> Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0);
            };
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return isLava(state) || isGrate(state) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockState restored = restoreState(state);
        return restored.isAir() ? List.of() : List.of(new ItemStack(restored.getBlock()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isLava(state) ? new InfernalFurnaceBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.INFERNAL_FURNACE.get()) {
            return null;
        }
        BlockEntityTicker<InfernalFurnaceBlockEntity> ticker = InfernalFurnaceBlockEntity::serverTick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!isLava(state)) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        double dx = entity.getX() < pos.getX() + 0.3 ? 1.0E-4 : entity.getX() > pos.getX() + 0.7 ? -1.0E-4 : 0.0;
        double dz = entity.getZ() < pos.getZ() + 0.3 ? 1.0E-4 : entity.getZ() > pos.getZ() + 0.7 ? -1.0E-4 : 0.0;
        entity.setDeltaMovement(motion.x + dx, motion.y, motion.z + dz);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity instanceof ItemEntity item) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0.025, entity.getDeltaMovement().z);
            if (item.onGround() && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace && furnace.addItems(item.getItem())) {
                item.discard();
            }
        } else if (entity instanceof LivingEntity && !entity.fireImmune()) {
            entity.hurt(level.damageSources().lava(), 3.0F);
            entity.igniteForSeconds(10.0F);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (disassembling) {
            return;
        }
        BlockPos center = center(pos, state);
        if (isLava(state)) {
            disassemble(level, center, null);
        } else {
            BlockState centerState = level.getBlockState(center);
            if (centerState.is(this) && isLava(centerState)) {
                level.setBlock(center, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static void disassemble(ServerLevel level, BlockPos center, @Nullable InfernalFurnaceBlockEntity furnace) {
        disassembling = true;
        try {
            Blaze blaze = EntityType.BLAZE.create(level, MobSpawnType.TRIGGERED);
            if (blaze != null) {
                blaze.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 0.0F, 0.0F);
                level.addFreshEntity(blaze);
            }
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof InfernalFurnaceBlock) {
                    level.setBlock(pos, restoreState(state), Block.UPDATE_ALL);
                }
            }
        } finally {
            disassembling = false;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isLava(state) || !level.getBlockState(pos.above()).isAir()) {
            return;
        }
        for (int index = 0; index < 3; index++) {
            level.addParticle(
                ParticleTypes.LARGE_SMOKE,
                pos.getX() + random.nextFloat(),
                pos.getY() + 1.0 + random.nextFloat() * 0.5,
                pos.getZ() + random.nextFloat(),
                0.0,
                0.0,
                0.0
            );
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int param) {
        if (type == 1) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), param, 5);
            }
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(type, param);
    }
}
