package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aura.AuraManager;
import thaumcraft.block.mirror.MirrorBlock;
import thaumcraft.item.HandMirrorItem;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModItems;

public class MirrorBlockEntity extends TcBlockEntity {
    public static final int EVENT_TRANSPORT = 1;

    private boolean linked;
    private @Nullable GlobalPos link;
    private int itemCount;
    private @Nullable Direction linkedFacing;

    public MirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRROR.get(), pos, state);
    }

    public boolean isLinked() {
        return linked;
    }

    private @Nullable ServerLevel targetLevel() {
        return link == null || !(level instanceof ServerLevel serverLevel) ? null : serverLevel.getServer().getLevel(link.dimension());
    }

    private @Nullable MirrorBlockEntity target(ServerLevel targetLevel) {
        return link != null && targetLevel.getBlockEntity(link.pos()) instanceof MirrorBlockEntity mirror ? mirror : null;
    }

    private GlobalPos self() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    private void setLinked(boolean value) {
        linked = value;
        if (level != null && getBlockState().getValue(MirrorBlock.LINKED) != value) {
            level.setBlock(worldPosition, getBlockState().setValue(MirrorBlock.LINKED, value), 2);
        }
        sync();
    }

    public void restoreLink() {
        if (!isDestinationValid()) {
            return;
        }
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null) {
            return;
        }
        MirrorBlockEntity target = target(targetLevel);
        if (target != null) {
            target.link = self();
            target.setLinked(true);
            linkedFacing = target.getBlockState().getValue(DirectionalBlock.FACING);
            setLinked(true);
        }
    }

    public void invalidateLink() {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel != null && link != null && targetLevel.isLoaded(link.pos())) {
            MirrorBlockEntity target = target(targetLevel);
            if (target != null) {
                target.setLinked(false);
            }
        }
    }

    public boolean isLinkValid() {
        if (!linked) {
            return false;
        }
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null) {
            return false;
        }
        MirrorBlockEntity target = target(targetLevel);
        if (target != null && target.linked && self().equals(target.link)) {
            return true;
        }
        setLinked(false);
        return false;
    }

    public boolean isDestinationValid() {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null) {
            return false;
        }
        MirrorBlockEntity target = target(targetLevel);
        if (target != null) {
            return !target.isLinkValid();
        }
        setLinked(false);
        return false;
    }

    public void transport(ItemEntity item) {
        ItemStack items = item.getItem();
        double x = worldPosition.getX();
        double y = worldPosition.getY();
        double z = worldPosition.getZ();
        if (!linked || !isLinkValid() || !AuraManager.decreaseClosestAura(level, x, y, z, Math.max(1, items.getCount() / 16), false)) {
            return;
        }
        itemCount += items.getCount();
        while (itemCount >= 16) {
            itemCount -= 16;
            AuraManager.decreaseClosestAura(level, x, y, z, 1);
        }
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null) {
            return;
        }
        if (linkedFacing == null) {
            BlockState targetState = targetLevel.getBlockState(link.pos());
            linkedFacing = targetState.hasProperty(DirectionalBlock.FACING) ? targetState.getValue(DirectionalBlock.FACING) : Direction.UP;
        }
        spawnAt(targetLevel, link.pos(), linkedFacing, items.copy());
        item.discard();
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_TRANSPORT, 0);
        targetLevel.blockEvent(link.pos(), targetLevel.getBlockState(link.pos()).getBlock(), EVENT_TRANSPORT, 0);
        setChanged();
    }

    public static void spawnAt(ServerLevel targetLevel, BlockPos pos, Direction facing, ItemStack stack) {
        ItemEntity spawned = new ItemEntity(
            targetLevel,
            pos.getX() + 0.5 - facing.getStepX() * 0.3,
            pos.getY() + 0.5 - facing.getStepY() * 0.3,
            pos.getZ() + 0.5 - facing.getStepZ() * 0.3,
            stack
        );
        spawned.setDeltaMovement(facing.getStepX() * 0.15F, facing.getStepY() * 0.15F, facing.getStepZ() * 0.15F);
        spawned.setPortalCooldown(20);
        targetLevel.addFreshEntity(spawned);
    }

    @Override
    public boolean triggerEvent(int b0, int b1) {
        if (b0 != EVENT_TRANSPORT) {
            return super.triggerEvent(b0, b1);
        }
        if (level != null && level.isClientSide()) {
            level.playLocalSound(worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.1F, 1.0F, false);
            Direction face = getBlockState().getValue(DirectionalBlock.FACING);
            RandomSource random = level.getRandom();
            for (int index = 0; index < 3; index++) {
                double px = worldPosition.getX() + 0.33 + random.nextFloat() * 0.33F - face.getStepX() / 2.0;
                double py = worldPosition.getY() + 0.33 + random.nextFloat() * 0.33F - face.getStepY() / 2.0;
                double pz = worldPosition.getZ() + 0.33 + random.nextFloat() * 0.33F - face.getStepZ() / 2.0;
                level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x80000000), px, py, pz,
                    face.getStepX() * 0.05, face.getStepY() * 0.05, face.getStepZ() * 0.05);
            }
        }
        return true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack drop = new ItemStack(ModItems.MAGIC_MIRROR.get());
        if (linked && link != null) {
            HandMirrorItem.setLink(drop, link);
            invalidateLink();
        }
        Block.popResource(serverLevel, pos, drop);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        GlobalPos target = components.get(ModDataComponents.MIRROR_LINK.get());
        if (target != null) {
            link = target;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        linked = input.getBooleanOr("linked", false);
        link = input.read("link", GlobalPos.CODEC).orElse(null);
        itemCount = input.getIntOr("item_count", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("linked", linked);
        if (link != null) {
            output.store("link", GlobalPos.CODEC, link);
        }
        output.putInt("item_count", itemCount);
    }
}
