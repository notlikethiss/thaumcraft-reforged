package thaumcraft.block.device;

import com.mojang.serialization.MapCodec;
import thaumcraft.blockentity.TcBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import thaumcraft.Config;
import thaumcraft.block.WandTarget;
import thaumcraft.block.ward.WardHelper;
import thaumcraft.blockentity.OwnedBlockEntity;

public class ArcanePressurePlateBlock extends BasePressurePlateBlock implements EntityBlock, WandTarget {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 2);

    public ArcanePressurePlateBlock(Properties properties) {
        super(properties, BlockSetType.STONE);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(MODE, 0));
    }

    @Override
    protected MapCodec<? extends BasePressurePlateBlock> codec() {
        return simpleCodec(ArcanePressurePlateBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, MODE);
    }

    @Override
    protected int getSignalForState(BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected BlockState setSignalForState(BlockState state, int signal) {
        return state.setValue(POWERED, signal > 0);
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        int mode = level.getBlockState(pos).getValue(MODE);
        OwnedBlockEntity owned = level.getBlockEntity(pos) instanceof OwnedBlockEntity blockEntity ? blockEntity : null;
        Class<? extends Entity> entityClass = switch (mode) {
            case 1 -> LivingEntity.class;
            case 2 -> Player.class;
            default -> Entity.class;
        };
        for (Entity entity : entities(level, TOUCH_AABB.move(pos), entityClass)) {
            boolean user = entity instanceof Player player && owned != null && owned.canUse(player);
            if (mode == 1 && user || mode == 2 && !user) {
                continue;
            }
            return 15;
        }
        return 0;
    }

    private static <T extends Entity> List<T> entities(Level level, AABB box, Class<T> entityClass) {
        return level.getEntitiesOfClass(entityClass, box, EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OwnedBlockEntity owned && owned.canGrant(player)) {
            int mode = (state.getValue(MODE) + 1) % 3;
            level.setBlock(pos, state.setValue(MODE, mode), Block.UPDATE_ALL);
            player.sendSystemMessage(Component.translatable("tc.thaumcraft.plate_mode" + mode));
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.1F, 0.9F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        WardHelper.setOwner(level, pos, by);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return Config.wardedStone() ? 0.0F : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        WardHelper.markCreativeRemoval(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Config.wardedStone() ? List.of() : super.getDrops(state, params);
    }

    @Override
    public InteractionResult onWandUse(Level level, BlockPos pos, BlockState state, Player player, ItemStack wand, Direction side) {
        return WardHelper.removeWithWand(level, pos, state, player, new ItemStack(this));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwnedBlockEntity(pos, state);
    }
}
