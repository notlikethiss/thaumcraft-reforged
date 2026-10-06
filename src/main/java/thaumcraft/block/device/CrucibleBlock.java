package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.block.WandTarget;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.crafting.CrucibleCrafting;
import thaumcraft.item.wand.WandManager;
import thaumcraft.registry.ModBlockEntities;

public class CrucibleBlock extends Block implements EntityBlock, WandTarget {
    private static final ResourceLocation TALLOW_GOLEM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "tallow_golem");
    private static final ResourceLocation ADVANCED_TALLOW_GOLEM = ResourceLocation.fromNamespaceAndPath("thaumcraft", "decanting_golem");
    private static final VoxelShape COLLISION = Shapes.or(
        Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 2.0, 13.6, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 13.6, 2.0),
        Block.box(14.0, 0.0, 0.0, 16.0, 13.6, 16.0),
        Block.box(0.0, 0.0, 14.0, 16.0, 13.6, 16.0)
    );

    public CrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.CRUCIBLE.get()) {
            return null;
        }
        BlockEntityTicker<CrucibleBlockEntity> ticker = level.isClientSide() ? CrucibleBlockEntity::clientTick : CrucibleBlockEntity::serverTick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!(level instanceof ServerLevel serverLevel) || !(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return;
        }
        if (entity instanceof ItemEntity item && crucible.isBoiling()) {
            crucible.attemptSmelt(item);
            return;
        }
        if (!crucible.tickContactDelay()) {
            return;
        }
        if (entity instanceof LivingEntity && !isTallowGolem(entity) && crucible.isBoiling()) {
            entity.hurt(level.damageSources().magic(), 1.0F);
            RandomSource random = level.getRandom();
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.4F, 2.0F + random.nextFloat() * 0.4F);
            if (random.nextInt(25) == 0) {
                crucible.addToSource(Aspect.FLESH, 1);
            }
        }
    }

    private static boolean isTallowGolem(Entity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return TALLOW_GOLEM.equals(id) || ADVANCED_TALLOW_GOLEM.equals(id);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (!stack.is(Items.WATER_BUCKET) || !(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) || crucible.hasLiquid()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            crucible.fill();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            RandomSource random = level.getRandom();
            level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.33F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.3F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) || !crucible.isBoiling() || crucible.getTags().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            crucible.spillRemnants();
            return InteractionResult.SUCCESS;
        }
        if (WandManager.spendCharge(level, wand, player, CrucibleCrafting.getOutputCost(crucible))) {
            CrucibleCrafting.perform(level, player, crucible);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.updateBellows();
        }
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextBoolean()
            && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible
            && crucible.isBoiling()
            && !crucible.getTags().isEmpty()) {
            level.playLocalSound(pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F, 1.2F + random.nextFloat() * 0.2F, false);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int param) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(type, param);
    }
}
