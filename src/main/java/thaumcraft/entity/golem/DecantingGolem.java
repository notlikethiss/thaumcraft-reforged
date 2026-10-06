package thaumcraft.entity.golem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.entity.golem.goal.EmptyAlembicAdvGoal;
import thaumcraft.entity.golem.goal.JarGotoGoal;
import thaumcraft.entity.golem.goal.JarPlaceAdvGoal;
import thaumcraft.entity.golem.goal.LiquidEmptyGoal;
import thaumcraft.entity.golem.goal.LiquidGatherGoal;
import thaumcraft.entity.golem.goal.LiquidGotoGoal;
import thaumcraft.item.FilledJarItem;
import thaumcraft.item.JarContents;
import thaumcraft.registry.ModItems;

public class DecantingGolem extends GolemWorker {
    public static final int UNIT = 250;
    public static final int SPACE_PER_UNIT = 4;
    public static final int CAPACITY = 64;

    private static final EntityDataAccessor<Integer> DATA_TALLOW_TYPE = SynchedEntityData.defineId(DecantingGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ASPECT = SynchedEntityData.defineId(DecantingGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AMOUNT = SynchedEntityData.defineId(DecantingGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_WATCHED = SynchedEntityData.defineId(DecantingGolem.class, EntityDataSerializers.STRING);

    private Fluid liquid = Fluids.EMPTY;

    public DecantingGolem(EntityType<? extends DecantingGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 1, 16);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.DECANTING;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TALLOW_TYPE, 0);
        entityData.define(DATA_ASPECT, -1);
        entityData.define(DATA_AMOUNT, 0);
        entityData.define(DATA_WATCHED, "");
    }

    @Override
    protected void registerGoals() {
    }

    public int getTallowType() {
        return this.entityData.get(DATA_TALLOW_TYPE);
    }

    public @Nullable Aspect getAspect() {
        int id = this.entityData.get(DATA_ASPECT);
        return id < 0 ? null : Aspect.get(id);
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.entityData.set(DATA_ASPECT, aspect == null ? -1 : aspect.getId());
    }

    public int getAmount() {
        return this.entityData.get(DATA_AMOUNT);
    }

    public void setAmount(int amount) {
        this.entityData.set(DATA_AMOUNT, amount);
        refreshDisplay();
    }

    public Fluid getLiquid() {
        return liquid;
    }

    public void setLiquid(Fluid liquid) {
        this.liquid = liquid;
    }

    public @Nullable Fluid getWatchedLiquid() {
        String id = this.entityData.get(DATA_WATCHED);
        return id.isEmpty() ? null : BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
    }

    private void setWatchedLiquid(@Nullable Fluid fluid) {
        this.entityData.set(DATA_WATCHED, fluid == null ? "" : BuiltInRegistries.FLUID.getKey(fluid).toString());
    }

    public static List<Fluid> registeredLiquids() {
        List<Fluid> fluids = new ArrayList<>();
        fluids.add(Fluids.WATER);
        fluids.add(Fluids.LAVA);
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState()) && !fluids.contains(fluid)) {
                fluids.add(fluid);
            }
        }
        return fluids;
    }

    public void cycleLiquid(int step) {
        List<Fluid> fluids = registeredLiquids();
        Fluid watched = getWatchedLiquid();
        int index = watched == null ? 0 : fluids.indexOf(watched) + 1;
        int size = fluids.size() + 1;
        index = Math.floorMod(index + step, size);
        setWatchedLiquid(index == 0 ? null : fluids.get(index - 1));
    }

    private void applyType(int type) {
        this.entityData.set(DATA_TALLOW_TYPE, type);
        List<ItemStack> stacks = this.inventory.saveStacks();
        this.inventory = new GolemInventory(this, 1, type == 0 ? 1 : this.maxCarried);
        this.inventory.loadStacks(stacks);
        this.inventory.setOnChange(this::refreshDisplay);
        this.goalSelector.removeAllGoals(goal -> true);
        if (type == 0) {
            this.goalSelector.addGoal(1, new LiquidEmptyGoal(this));
            this.goalSelector.addGoal(2, new LiquidGatherGoal(this));
            this.goalSelector.addGoal(3, new LiquidGotoGoal(this));
            addBasicGoals(true, 4, 5, 7);
        } else {
            this.goalSelector.addGoal(1, new EmptyAlembicAdvGoal(this));
            this.goalSelector.addGoal(2, new JarPlaceAdvGoal(this));
            this.goalSelector.addGoal(3, new JarGotoGoal(this, this::acceptsJar));
            addBasicGoals(true, 8, 10, 11);
        }
        refreshDisplay();
    }

    @Override
    public void setup(int core, int color, Direction facing) {
        super.setup(core, color, facing);
        BlockPos home = getHomeContainerPos();
        if (this.level().getBlockEntity(home) instanceof AlembicBlockEntity) {
            applyType(1);
        } else if (hasHomeTank()) {
            applyType(0);
        }
    }

    public boolean hasHomeTank() {
        return GolemInventories.hasFluids(this.level(), getHomeContainerPos(), getHomeFacing());
    }

    public int homeFill(Fluid fluid, int amount, boolean simulate) {
        return GolemInventories.fillFluid(this.level(), getHomeContainerPos(), getHomeFacing(), fluid, amount, simulate);
    }

    private void refreshDisplay() {
        ItemStack jar;
        Aspect aspect = getAspect();
        if (getTallowType() == 1 && aspect != null && getAmount() > 0) {
            jar = new ItemStack(ModItems.FILLED_JAR.get());
            FilledJarItem.setContents(jar, new JarContents(aspect, getAmount()));
        } else {
            jar = new ItemStack(ModItems.WARDED_JAR.get());
        }
        setDisplayCarried(jar);
    }

    @Override
    public boolean hasSomething() {
        return true;
    }

    public record MissingLiquid(Fluid fluid, int space) {
    }

    public List<MissingLiquid> getMissingLiquids() {
        List<MissingLiquid> result = new ArrayList<>();
        if (!hasHomeTank()) {
            return result;
        }
        Fluid watched = getWatchedLiquid();
        for (Fluid fluid : registeredLiquids()) {
            if (watched == null || watched == fluid) {
                int space = homeFill(fluid, Integer.MAX_VALUE / 2, true);
                if (space >= UNIT) {
                    result.add(new MissingLiquid(fluid, space));
                }
            }
        }
        return result;
    }

    public @Nullable BlockPos findPossibleLiquid(Fluid fluid) {
        BlockPos home = getHomeContainerPos();
        int range = getCore() == 3 ? 32 : 20;
        if (hasDecoration("G")) {
            range = (int) (range * 1.2F);
        }
        Fluid watched = getWatchedLiquid();
        BlockPos best = null;
        for (BlockPos marker : GolemUtils.markersForGolem(this, range * range)) {
            for (BlockPos pos : GolemUtils.adjacentLiquid(this.level(), marker)) {
                if (pos.equals(home) || best != null && this.distanceToSqr(Vec3.atLowerCornerOf(pos)) >= this.distanceToSqr(Vec3.atLowerCornerOf(best))) {
                    continue;
                }
                Direction side = GolemUtils.sideFacing(pos, marker);
                if (GolemInventories.hasFluids(this.level(), pos, side)) {
                    if (GolemInventories.drainFluid(this.level(), pos, side, fluid, UNIT, true) >= UNIT) {
                        best = pos;
                    }
                } else if ((fluid == Fluids.WATER || fluid == Fluids.LAVA)
                    && GolemUtils.isSource(this.level(), pos, fluid)
                    && (watched == null || watched == fluid)) {
                    best = pos;
                }
            }
        }
        return best;
    }

    public boolean acceptsJar(JarBlockEntity jar, boolean empty) {
        Aspect aspect = getAspect();
        if (aspect == null) {
            return false;
        }
        return empty || jar.getAmount() <= JarBlockEntity.MAX_AMOUNT - getAmount() && jar.getAspect() == aspect;
    }

    public @Nullable BlockPos findJarWithRoom() {
        int range = getCore() == 3 ? 32 : 20;
        if (hasDecoration("G")) {
            range = (int) (range * 1.2F);
        }
        for (boolean empty : new boolean[] {false, true}) {
            BlockPos found = null;
            for (BlockPos marker : GolemUtils.markersForGolem(this, range * range)) {
                for (JarBlockEntity jar : GolemUtils.adjacentJars(this.level(), marker)) {
                    if ((empty ? jar.getAmount() == 0 : jar.getAmount() > 0) && acceptsJar(jar, empty)) {
                        found = jar.getBlockPos();
                    }
                }
            }
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putShort("TallowType", (short) getTallowType());
        output.putByte("tag", (byte) this.entityData.get(DATA_ASPECT).intValue());
        output.putByte("amount", (byte) getAmount());
        output.putString("liquid", BuiltInRegistries.FLUID.getKey(this.liquid).toString());
        output.putString("watched", this.entityData.get(DATA_WATCHED));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.entityData.set(DATA_ASPECT, (int) input.getByteOr("tag", (byte) -1));
        this.entityData.set(DATA_AMOUNT, (int) input.getByteOr("amount", (byte) 0));
        this.liquid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(input.getStringOr("liquid", "minecraft:empty")));
        this.entityData.set(DATA_WATCHED, input.getStringOr("watched", ""));
        applyType(input.getShortOr("TallowType", (short) 0));
    }

    @Override
    protected int baseHealth() {
        return 10;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_HURT;
    }
}
