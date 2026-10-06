package thaumcraft.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import thaumcraft.network.ModNetwork;
import org.jspecify.annotations.Nullable;
import thaumcraft.aura.AuraManager;
import thaumcraft.block.bore.ArcaneBoreBlock;
import thaumcraft.fx.Fx;
import thaumcraft.item.wand.ExcavationWandItem;
import thaumcraft.lib.MiningUtils;
import thaumcraft.menu.ArcaneBoreMenu;
import thaumcraft.network.BoreDigPayload;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;

public class ArcaneBoreBlockEntity extends BaseContainerBlockEntity {
    public static final int MAX_RADIUS = 2;
    private static final int EVENT_DIG = 99;

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int spiral;
    private float currentRadius;
    public float vRadX;
    public float vRadZ;
    private float tRadX;
    private float tRadZ;
    private float mRadX;
    private float mRadZ;
    private int count;
    public int topRotation;
    private int soundDelay;
    private @Nullable Object beam1;
    private @Nullable Object beam2;
    private int beamLength;
    public int rotX;
    public int rotZ;
    private int tarX;
    private int tarZ;
    public int speedX;
    public int speedZ;
    private int lastX;
    private int lastY;
    private int lastZ;
    private boolean toDig;
    private BlockPos digPos = BlockPos.ZERO;
    private float radInc;
    private int paused = 100;
    private int maxPause = 100;
    private long repairCounter;
    private Direction orientation = Direction.UP;
    private int blockCount;
    private boolean propertiesDirty = true;
    private boolean hasWand;
    private boolean hasPickaxe;
    private int fortune;
    private int speed;
    private int area;

    public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE.get(), pos, state);
    }

    public Direction getOrientation() {
        return orientation;
    }

    public Direction getBaseDirection() {
        return ArcaneBoreBlock.baseDirection(getBlockState());
    }

    public boolean hasWand() {
        return hasWand;
    }

    public int getFortune() {
        return fortune;
    }

    public int getSpeed() {
        return speed;
    }

    public int getArea() {
        return area;
    }

    private void refreshProperties() {
        if (level == null) {
            return;
        }
        propertiesDirty = false;
        ItemStack wand = items.get(0);
        ItemStack pickaxe = items.get(1);
        hasWand = wand.getItem() instanceof ExcavationWandItem;
        fortune = hasWand ? MiningUtils.enchantmentLevel(level, wand, ModEnchantments.TREASURE) : 0;
        area = hasWand ? MiningUtils.enchantmentLevel(level, wand, ModEnchantments.POTENCY) : 0;
        hasPickaxe = pickaxe.is(ItemTags.PICKAXES);
        speed = 0;
        if (hasPickaxe) {
            fortune = Math.max(fortune, MiningUtils.enchantmentLevel(level, pickaxe, Enchantments.FORTUNE));
            speed = MiningUtils.enchantmentLevel(level, pickaxe, Enchantments.EFFICIENCY);
        }
    }

    public boolean hasSilkTouch() {
        return level != null && MiningUtils.enchantmentLevel(level, items.get(1), Enchantments.SILK_TOUCH) > 0;
    }

    public boolean hasElementalPickaxe() {
        return items.get(1).is(ModItems.ELEMENTAL_PICKAXE.get());
    }

    private static boolean nearBroken(ItemStack stack) {
        return stack.getDamageValue() + 1 >= stack.getMaxDamage();
    }

    private boolean areItemsValid() {
        return hasWand && hasPickaxe && items.get(0).isDamageableItem() && items.get(1).isDamageableItem() && !nearBroken(items.get(0)) && !nearBroken(items.get(1));
    }

    private boolean gettingPower() {
        return level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(worldPosition.relative(getBaseDirection()));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        if (bore.propertiesDirty) {
            bore.refreshProperties();
        }
        bore.rotX = bore.ease(bore.rotX, bore.tarX, true);
        bore.rotZ = bore.ease(bore.rotZ, bore.tarZ, false);
        if (bore.gettingPower() && bore.areItemsValid()) {
            bore.dig();
        } else if (level.isClientSide()) {
            bore.relax();
        }
        if (!level.isClientSide()) {
            bore.repair();
        }
    }

    private int ease(int rotation, int target, boolean xAxis) {
        int velocity = xAxis ? speedX : speedZ;
        if (rotation < target) {
            rotation += velocity;
            velocity = rotation < target ? velocity + 1 : (int) (velocity / 3.0F);
        } else if (rotation > target) {
            rotation += velocity;
            velocity = rotation > target ? velocity - 1 : (int) (velocity / 3.0F);
        } else {
            velocity = 0;
        }
        if (xAxis) {
            speedX = velocity;
        } else {
            speedZ = velocity;
        }
        return rotation;
    }

    private void relax() {
        if (topRotation % 90 != 0) {
            topRotation += Math.min(10, 90 - topRotation % 90);
        }
        vRadX *= 0.9F;
        vRadZ *= 0.9F;
    }

    private void repair() {
        repairCounter++;
        if (hasWand) {
            ItemStack wand = items.get(0);
            if (MiningUtils.enchantmentLevel(level, wand, ModEnchantments.CHARGING) > 0
                && repairCounter % 50 == 0
                && wand.getDamageValue() > 0
                && AuraManager.decreaseClosestAura(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1)) {
                wand.setDamageValue(Math.max(0, wand.getDamageValue() - 5));
            }
        }
        if (hasPickaxe) {
            ItemStack pickaxe = items.get(1);
            int repair = MiningUtils.enchantmentLevel(level, pickaxe, ModEnchantments.REPAIR);
            int mod = 0;
            if (repair > 2) {
                mod = Math.min(15, (repair - 2) * 2);
                repair = 2;
            }
            if (repair > 0
                && repairCounter % (60 - repair * 20 - mod) == 0
                && pickaxe.getDamageValue() > 0
                && AuraManager.decreaseClosestAura(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1)) {
                pickaxe.setDamageValue(Math.max(0, pickaxe.getDamageValue() - 1));
            }
        }
    }

    private void dig() {
        if (rotX != tarX || rotZ != tarZ) {
            if (level.isClientSide()) {
                relax();
            }
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (--count > 0) {
                return;
            }
            if (toDig) {
                toDig = false;
                digBlock(serverLevel);
            }
            findNextBlockToDig(serverLevel);
        } else {
            clientDig();
        }
    }

    private int digTime(BlockState state, BlockPos pos) {
        return Math.max(10 - speed, (int) (state.getDestroySpeed(level, pos) * 2.0F) - speed * 2);
    }

    private void digBlock(ServerLevel serverLevel) {
        BlockState state = serverLevel.getBlockState(digPos);
        if (!state.isAir()) {
            boolean silkTouch = hasSilkTouch();
            int digFortune = silkTouch ? 0 : fortune;
            serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_DIG, Block.getId(state));
            ItemStack tool = items.get(1).copy();
            if (!silkTouch && fortune > MiningUtils.enchantmentLevel(serverLevel, tool, Enchantments.FORTUNE)) {
                serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE)
                    .ifPresent(holder -> EnchantmentHelper.updateEnchantments(tool, enchantments -> enchantments.set(holder, fortune)));
            }
            List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, serverLevel, digPos, serverLevel.getBlockEntity(digPos), null, tool));
            for (ItemEntity item : serverLevel.getEntitiesOfClass(ItemEntity.class, new AABB(digPos).inflate(1.0))) {
                drops.add(item.getItem().copy());
                item.discard();
            }
            for (ItemStack drop : drops) {
                ItemStack result = drop.copy();
                if (!silkTouch && hasElementalPickaxe()) {
                    result = MiningUtils.findSpecialMiningResult(drop, 0.275F + digFortune * 0.075F, serverLevel.getRandom());
                }
                output(serverLevel, result);
            }
        }
        items.get(0).hurtAndBreak(1, serverLevel, null, item -> {
        });
        items.get(1).hurtAndBreak(1, serverLevel, null, item -> {
        });
        setChanged();
        serverLevel.setBlock(digPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private void output(ServerLevel serverLevel, ItemStack stack) {
        BlockPos basePos = worldPosition.relative(getBaseDirection());
        BlockState base = serverLevel.getBlockState(basePos);
        if (!base.is(ModBlocks.ARCANE_BORE_BASE.get())) {
            return;
        }
        Direction facing = base.getValue(HorizontalDirectionalBlock.FACING);
        ItemStack remainder = stack;
        Container container = HopperBlockEntity.getContainerAt(serverLevel, basePos.relative(facing));
        if (container != null) {
            remainder = HopperBlockEntity.addItem(null, container, stack, facing.getOpposite());
        }
        if (!remainder.isEmpty()) {
            ItemEntity item = new ItemEntity(
                serverLevel,
                worldPosition.getX() + 0.5 + facing.getStepX() * 0.66,
                worldPosition.getY() + 0.4 + getBaseDirection().getStepY(),
                worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.66,
                remainder
            );
            item.setDeltaMovement(0.075F * facing.getStepX(), 0.025F, 0.075F * facing.getStepZ());
            serverLevel.addFreshEntity(item);
        }
    }

    private void findNextBlockToDig(ServerLevel serverLevel) {
        int radius = MAX_RADIUS + area;
        if (radInc == 0.0F) {
            radInc = radius / 360.0F;
        }
        int x = lastX;
        int y = lastY;
        int z = lastZ;
        while (x == lastX && z == lastZ && y == lastY) {
            spiral += 2;
            if (spiral >= 360) {
                spiral -= 360;
            }
            currentRadius += radInc;
            if (currentRadius > radius || currentRadius < -radius) {
                radInc *= -1.0F;
            }
            Vec3 offset = new Vec3(0.0, currentRadius, 0.0)
                .zRot(spiral / 180.0F * (float) Math.PI)
                .yRot((float) (Math.PI / 2) * orientation.getStepX())
                .xRot((float) (Math.PI / 2) * orientation.getStepY());
            Vec3 point = Vec3.atCenterOf(worldPosition.relative(orientation)).add(offset);
            x = Mth.floor(point.x);
            y = Mth.floor(point.y);
            z = Mth.floor(point.z);
        }
        lastX = x;
        lastY = y;
        lastZ = z;
        BlockPos cursor = new BlockPos(x, y, z);
        for (int depth = 0; depth < 64; depth++) {
            cursor = cursor.relative(orientation);
            BlockState state = serverLevel.getBlockState(cursor);
            if (state.getDestroySpeed(serverLevel, cursor) < 0.0F) {
                break;
            }
            if (state.isAir()
                || state.getCollisionShape(serverLevel, cursor).isEmpty()
                || !AuraManager.decreaseClosestAura(serverLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), blockCount == 2 ? 1 : 0)) {
                continue;
            }
            digPos = cursor;
            if (++blockCount > 2) {
                blockCount = 0;
            }
            count = digTime(state, cursor);
            toDig = true;
            BlockHitResult hit = serverLevel.clip(new ClipContext(
                Vec3.atCenterOf(worldPosition.relative(orientation)),
                Vec3.atCenterOf(digPos),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
            ));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockState blocking = serverLevel.getBlockState(hit.getBlockPos());
                if (blocking.getDestroySpeed(serverLevel, hit.getBlockPos()) > -1.0F && !blocking.getCollisionShape(serverLevel, hit.getBlockPos()).isEmpty()) {
                    count = digTime(blocking, hit.getBlockPos());
                    digPos = hit.getBlockPos();
                }
            }
            ModNetwork.sendToNear(serverLevel, null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 96.0,
                new BoreDigPayload(worldPosition, digPos));
            break;
        }
    }

    public void receiveDig(BlockPos target) {
        digPos = target;
        toDig = true;
    }

    private void clientDig() {
        paused++;
        if (paused < maxPause && --soundDelay <= 0) {
            soundDelay = 24 + level.getRandom().nextInt(2);
            level.playLocalSound(worldPosition, ModSounds.RUMBLE.get(), SoundSource.BLOCKS, 0.3F, 0.9F + level.getRandom().nextFloat() * 0.2F, false);
        }
        if (beamLength > 0 && paused > maxPause) {
            beamLength--;
        }
        if (toDig) {
            aimAtDigTarget();
        }
        if (paused < maxPause) {
            vRadX += vRadX < tRadX ? mRadX : vRadX > tRadX ? -mRadX : 0.0F;
            vRadZ += vRadZ < tRadZ ? mRadZ : vRadZ > tRadZ ? -mRadZ : 0.0F;
        } else {
            vRadX *= 0.9F;
            vRadZ *= 0.9F;
        }
        mRadX *= 0.9F;
        mRadZ *= 0.9F;
        float vx = rotX + 90 - vRadX;
        float vz = rotZ + 90 - vRadZ;
        float dx = Mth.sin(vx / 180.0F * (float) Math.PI) * Mth.cos(vz / 180.0F * (float) Math.PI);
        float dz = Mth.cos(vx / 180.0F * (float) Math.PI) * Mth.cos(vz / 180.0F * (float) Math.PI);
        float dy = Mth.sin(vz / 180.0F * (float) Math.PI);
        Vec3 center = Vec3.atCenterOf(worldPosition);
        Vec3 start = center.add(dx, dy, dz);
        Vec3 end = center.add(dx * beamLength, dy * beamLength, dz * beamLength);
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        int impact = 0;
        Vec3 target = end;
        if (hit.getType() == HitResult.Type.BLOCK) {
            target = hit.getLocation();
            impact = 5;
        }
        topRotation += beamLength / 6;
        beam1 = Fx.get().boreBeam(level, center.x, center.y, center.z, target.x, target.y, target.z, 1, 0x00FF66, true, impact > 0 ? 2.0F : 0.0F, beam1, impact);
        beam2 = Fx.get().boreBeam(level, center.x, center.y, center.z, target.x, target.y, target.z, 2, 0xFF8855, false, impact > 0 ? 2.0F : 0.0F, beam2, impact);
    }

    private void aimAtDigTarget() {
        paused = 0;
        beamLength = 64;
        BlockState state = level.getBlockState(digPos);
        maxPause = state.isAir() ? 20 : 10 + digTime(state, digPos);
        toDig = false;
        double xd = worldPosition.getX() + 0.5 - (digPos.getX() + 0.5);
        double yd = worldPosition.getY() + 0.5 - (digPos.getY() + 0.5);
        double zd = worldPosition.getZ() + 0.5 - (digPos.getZ() + 0.5);
        double horizontal = Math.sqrt(xd * xd + zd * zd);
        float rx = (float) (Math.atan2(zd, xd) * 180.0 / Math.PI);
        float rz = (float) (-(Math.atan2(yd, horizontal) * 180.0 / Math.PI)) + 90.0F;
        tRadX = Mth.wrapDegrees(rotX) + rx;
        if (orientation == Direction.EAST) {
            if (tRadX > 180.0F) {
                tRadX -= 360.0F;
            }
            if (tRadX < -180.0F) {
                tRadX += 360.0F;
            }
        }
        tRadZ = rz - rotZ;
        if (orientation.getAxis().isVertical()) {
            tRadZ += 180.0F;
            if (vRadX - tRadX >= 180.0F) {
                vRadX -= 360.0F;
            }
            if (vRadX - tRadX <= -180.0F) {
                vRadX += 360.0F;
            }
        }
        mRadX = Math.abs((vRadX - tRadX) / 6.0F);
        mRadZ = Math.abs((vRadZ - tRadZ) / 6.0F);
    }

    public void rotateTo(Direction direction) {
        if (level != null && !level.isClientSide()) {
            setOrientation(direction, false);
        }
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
    }

    public void setOrientation(Direction direction, boolean initial) {
        orientation = direction;
        lastX = 0;
        lastZ = 0;
        switch (direction) {
            case DOWN -> {
                tarZ = 180;
                tarX = 0;
            }
            case UP -> {
                tarZ = 0;
                tarX = 0;
            }
            case NORTH -> {
                tarZ = 90;
                tarX = 270;
            }
            case SOUTH -> {
                tarZ = 90;
                tarX = 90;
            }
            case WEST -> {
                tarZ = 90;
                tarX = 0;
            }
            case EAST -> {
                tarZ = 90;
                tarX = 180;
            }
        }
        if (initial) {
            rotX = tarX;
            rotZ = tarZ;
        }
        toDig = false;
        radInc = 0.0F;
        paused = 100;
        tRadX = 0.0F;
        tRadZ = 0.0F;
        mRadX = 0.0F;
        mRadZ = 0.0F;
        digPos = BlockPos.ZERO;
        if (level != null && !level.isClientSide()) {
            setChanged();
        }
    }

    @Override
    public boolean triggerEvent(int b0, int b1) {
        if (b0 != EVENT_DIG) {
            return super.triggerEvent(b0, b1);
        }
        if (level != null && level.isClientSide()) {
            BlockState state = Block.stateById(b1);
            if (!state.isAir()) {
                SoundType sound = state.getSoundType();
                level.playLocalSound(digPos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
                for (int index = 0; index < 10; index++) {
                    Fx.get().boreDigFx(level, digPos, worldPosition.relative(orientation), state);
                }
            }
        }
        return true;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        propertiesDirty = true;
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.arcane_bore");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ArcaneBoreMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        Direction loaded = input.read("orientation", Direction.CODEC).orElse(Direction.UP);
        boolean client = level != null && level.isClientSide();
        if (!client) {
            setOrientation(loaded, true);
        } else if (loaded != orientation) {
            setOrientation(loaded, false);
        }
        propertiesDirty = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.store("orientation", Direction.CODEC, orientation);
    }
}
