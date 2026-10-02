package thaumcraft.block.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.ArcaneEarBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneEarBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ArcaneEarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    public static NoteBlockInstrument tone(Level level, BlockPos pos) {
        return level.getBlockState(pos.below()).instrument();
    }

    public static void triggerNote(Level level, BlockPos pos, int note, boolean echo) {
        if (level.getBlockState(pos.above()).isAir()) {
            level.blockEvent(pos, level.getBlockState(pos).getBlock(), echo ? -1 : tone(level, pos).ordinal(), note);
        }
    }

    public static void setPowered(Level level, BlockPos pos, BlockState state, boolean powered) {
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos.below(), state.getBlock());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
            ear.changePitch();
            triggerNote(level, pos, ear.getNote(), false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int b0, int b1) {
        NoteBlockInstrument[] instruments = NoteBlockInstrument.values();
        NoteBlockInstrument instrument = b0 >= 0 && b0 < instruments.length ? instruments[b0] : NoteBlockInstrument.HARP;
        if (instrument.hasCustomSound()) {
            instrument = NoteBlockInstrument.HARP;
        }
        float pitch = instrument.isTunable() ? NoteBlock.getPitchFromNote(b1) : 1.0F;
        level.playSeededSound(
            null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, instrument.getSoundEvent(), SoundSource.RECORDS, 3.0F, pitch, level.getRandom().nextLong()
        );
        level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, b1 / 24.0, 0.0, 0.0);
        return true;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!movedByPiston && state.getValue(POWERED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneEarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.ARCANE_EAR.get()) {
            return null;
        }
        BlockEntityTicker<ArcaneEarBlockEntity> ticker = ArcaneEarBlockEntity::serverTick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }
}
