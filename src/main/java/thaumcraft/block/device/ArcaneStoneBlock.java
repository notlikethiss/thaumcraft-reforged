package thaumcraft.block.device;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.block.WandTarget;
import thaumcraft.blockentity.InfusionWorkbenchBlockEntity;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.item.wand.WandManager;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.research.ResearchManager;

public class ArcaneStoneBlock extends Block implements EntityBlock, WandTarget {
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public enum Part implements StringRepresentable {
        NONE(0, 0),
        ORIGIN(0, 0),
        X(1, 0),
        Z(0, 1),
        XZ(1, 1);

        public final int dx;
        public final int dz;

        Part(int dx, int dz) {
            this.dx = dx;
            this.dz = dz;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Part[] FORMED = {Part.ORIGIN, Part.X, Part.Z, Part.XZ};

    public ArcaneStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, Part.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.ORIGIN ? new InfusionWorkbenchBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.INFUSION_WORKBENCH.get()) {
            return null;
        }
        BlockEntityTicker<InfusionWorkbenchBlockEntity> ticker = level.isClientSide()
            ? InfusionWorkbenchBlockEntity::clientTick
            : MagicWorkbenchBlockEntity::serverTick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }

    private static BlockPos origin(BlockPos pos, Part part) {
        return pos.offset(-part.dx, 0, -part.dz);
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        if (state.getValue(PART) != Part.NONE || !ResearchManager.isResearchComplete(player, "MAGBLOCK")) {
            return InteractionResult.PASS;
        }
        for (int ox = pos.getX() - 1; ox <= pos.getX(); ox++) {
            for (int oz = pos.getZ() - 1; oz <= pos.getZ(); oz++) {
                BlockPos origin = new BlockPos(ox, pos.getY(), oz);
                if (fits(level, origin)) {
                    if (WandManager.spendCharge(level, wand, player, 25) && !level.isClientSide()) {
                        form(level, origin);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private boolean fits(Level level, BlockPos origin) {
        for (Part part : FORMED) {
            BlockState state = level.getBlockState(origin.offset(part.dx, 0, part.dz));
            if (!state.is(this) || state.getValue(PART) != Part.NONE) {
                return false;
            }
        }
        return true;
    }

    private void form(Level level, BlockPos origin) {
        for (Part part : FORMED) {
            BlockPos target = origin.offset(part.dx, 0, part.dz);
            level.setBlock(target, defaultBlockState().setValue(PART, part), Block.UPDATE_CLIENTS);
            level.blockEvent(target, this, 1, 0);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        Part part = state.getValue(PART);
        if (part != Part.NONE && !isComplete(level, origin(pos, part))) {
            level.setBlockAndUpdate(pos, defaultBlockState());
            level.blockEvent(pos, this, 2, 6);
        }
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    private boolean isComplete(Level level, BlockPos origin) {
        for (Part part : FORMED) {
            BlockState state = level.getBlockState(origin.offset(part.dx, 0, part.dz));
            if (!state.is(this) || state.getValue(PART) != part) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Part part = state.getValue(PART);
        if (part == Part.NONE || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos origin = origin(pos, part);
        if (!(level.getBlockEntity(origin) instanceof InfusionWorkbenchBlockEntity workbench)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(workbench, origin);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int type, int param) {
        if (type == 1) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), param, 5);
            }
            return true;
        }
        if (type == 2) {
            if (level.isClientSide()) {
                RandomSource random = level.getRandom();
                for (int i = 0; i < 8; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
                }
                level.playLocalSound(pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
            }
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(type, param);
    }
}
