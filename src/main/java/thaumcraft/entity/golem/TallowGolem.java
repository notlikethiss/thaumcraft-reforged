package thaumcraft.entity.golem;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.entity.golem.goal.EmptyAlembicGoal;
import thaumcraft.entity.golem.goal.FillCrucibleGoal;
import thaumcraft.entity.golem.goal.GatherWaterGoal;
import thaumcraft.entity.golem.goal.GotoWaterGoal;
import thaumcraft.entity.golem.goal.JarGotoGoal;
import thaumcraft.entity.golem.goal.JarPlaceGoal;
import thaumcraft.entity.golem.goal.ProvideGotoGoal;
import thaumcraft.entity.golem.goal.ProvidePlaceGoal;
import thaumcraft.entity.golem.goal.SupplyGotoGoal;
import thaumcraft.entity.golem.goal.SupplyTakeGoal;
import thaumcraft.registry.ModItems;

public class TallowGolem extends GolemWorker {
    private static final EntityDataAccessor<Integer> DATA_TALLOW_TYPE = SynchedEntityData.defineId(TallowGolem.class, EntityDataSerializers.INT);

    private ItemStack essences = ItemStack.EMPTY;

    public TallowGolem(EntityType<? extends TallowGolem> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 1, 16);
    }

    @Override
    public GolemKind kind() {
        return GolemKind.TALLOW;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TALLOW_TYPE, 0);
    }

    @Override
    protected void registerGoals() {
    }

    public int getTallowType() {
        return this.entityData.get(DATA_TALLOW_TYPE);
    }

    private void applyType(int type) {
        this.entityData.set(DATA_TALLOW_TYPE, type);
        List<ItemStack> stacks = this.inventory.saveStacks();
        this.inventory = new GolemInventory(this, 1, type == 0 ? 1 : this.maxCarried);
        this.inventory.loadStacks(stacks);
        this.goalSelector.removeAllGoals(goal -> true);
        if (type == 0) {
            this.goalSelector.addGoal(1, new FillCrucibleGoal(this));
            this.goalSelector.addGoal(2, new GatherWaterGoal(this));
            this.goalSelector.addGoal(3, new GotoWaterGoal(this));
            addBasicGoals(true, 4, 5, 7);
        } else {
            this.goalSelector.addGoal(1, new EmptyAlembicGoal(this));
            this.goalSelector.addGoal(2, new JarPlaceGoal(this));
            this.goalSelector.addGoal(3, new JarGotoGoal(this, (jar, empty) -> JarPlaceGoal.accepts(this, jar, empty)));
            this.goalSelector.addGoal(4, new ProvidePlaceGoal(this));
            this.goalSelector.addGoal(5, new ProvideGotoGoal(this));
            this.goalSelector.addGoal(6, new SupplyTakeGoal(this));
            this.goalSelector.addGoal(7, new SupplyGotoGoal(this));
            addBasicGoals(true, 8, 10, 11);
        }
        refreshDisplay();
    }

    @Override
    public void setup(int core, int color, Direction facing) {
        super.setup(core, color, facing);
        BlockEntity home = this.level().getBlockEntity(getHomeContainerPos());
        if (home instanceof CrucibleBlockEntity) {
            applyType(0);
        } else if (home instanceof AlembicBlockEntity) {
            applyType(1);
        }
    }

    @Override
    public ItemStack getCarried() {
        return this.inventory.getItem(0);
    }

    @Override
    public void setCarried(ItemStack stack) {
        this.inventory.setItem(0, stack.copy());
        refreshDisplay();
    }

    @Override
    public ItemStack getProvideStack() {
        return getTallowType() == 1 ? this.essences : getCarried();
    }

    @Override
    public void setProvideStack(ItemStack stack) {
        if (getTallowType() == 1) {
            this.essences = stack.copy();
            refreshDisplay();
        } else {
            setCarried(stack);
        }
    }

    private void refreshDisplay() {
        setDisplayCarried(getTallowType() == 1 && !this.essences.isEmpty() ? this.essences : this.inventory.getItem(0));
    }

    @Override
    public boolean hasSomething() {
        return true;
    }

    @Override
    public @Nullable List<ItemStack> getMissingItems() {
        ItemStack held = this.inventory.getItem(0);
        int found = held.is(ModItems.ESSENTIA_PHIAL.get()) ? held.getCount() : 0;
        return List.of(new ItemStack(ModItems.ESSENTIA_PHIAL.get(), 64 - found));
    }

    @Override
    public void dropContents(ServerLevel level) {
        super.dropContents(level);
        if (!this.essences.isEmpty()) {
            this.spawnAtLocation(level, this.essences, 0.5F);
            this.essences = ItemStack.EMPTY;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("TallowType", (short) getTallowType());
        if (!this.essences.isEmpty()) {
            output.store("Essences", ItemStack.CODEC, this.essences);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.essences = input.read("Essences", ItemStack.CODEC).orElse(ItemStack.EMPTY);
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
