package thaumcraft.entity.golem;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.SimpleMenuProvider;
import thaumcraft.aura.AuraManager;
import thaumcraft.menu.GolemMenu;
import thaumcraft.entity.golem.goal.AvoidCreeperSwellGoal;
import thaumcraft.entity.golem.goal.GolemDoorGoal;
import thaumcraft.entity.golem.goal.ReturnHomeGoal;
import thaumcraft.item.golem.GolemDecorationItem;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.registry.ModDataComponents;

public abstract class GolemBase extends PathfinderMob {
    private static final EntityDataAccessor<ItemStack> DATA_CARRIED = SynchedEntityData.defineId(GolemBase.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(GolemBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CORE = SynchedEntityData.defineId(GolemBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_DECORATION = SynchedEntityData.defineId(GolemBase.class, EntityDataSerializers.STRING);

    protected GolemInventory inventory;
    protected int maxCarried = 16;
    protected Direction homeFacing = Direction.DOWN;
    protected int regenInterval = 200;
    private int regenTimer;
    private @Nullable UUID owner;
    public boolean paused;
    public int action;
    public int leftArm;
    public int healing;

    protected GolemBase(EntityType<? extends GolemBase> type, Level level) {
        super(type, level);
        this.inventory = new GolemInventory(this, 1, 64);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.STEP_HEIGHT, 1.0)
            .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    public abstract GolemKind kind();

    protected int baseHealth() {
        return 10;
    }

    protected int hatHealthBonus() {
        return 5;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GolemNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_CARRIED, ItemStack.EMPTY);
        entityData.define(DATA_COLOR, 0);
        entityData.define(DATA_CORE, 0);
        entityData.define(DATA_DECORATION, "");
    }

    public void setup(int core, int color, Direction facing) {
        this.entityData.set(DATA_CORE, core);
        this.entityData.set(DATA_COLOR, color);
        this.homeFacing = facing;
        this.maxCarried = core == 4 ? 32 : 16;
        refreshStats();
    }

    public int getCore() {
        return this.entityData.get(DATA_CORE);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }

    public String getDecoration() {
        return this.entityData.get(DATA_DECORATION);
    }

    public boolean hasDecoration(String letter) {
        return getDecoration().contains(letter);
    }

    public void setDecoration(String decoration) {
        this.entityData.set(DATA_DECORATION, decoration);
        refreshStats();
    }

    public Direction getHomeFacing() {
        return homeFacing;
    }

    public BlockPos getHomeContainerPos() {
        return getHomePosition().relative(homeFacing.getOpposite());
    }

    public GolemInventory getInventory() {
        return inventory;
    }

    public @Nullable UUID getOwnerId() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    public boolean isOwner(Player player) {
        return owner != null && owner.equals(player.getUUID());
    }

    public float getAIMoveSpeed() {
        float speed = (getCore() == 1 ? 0.36F : 0.28F) * (hasDecoration("B") ? 1.1F : 1.0F);
        if (hasDecoration("P")) {
            speed *= 0.88F;
        }
        return speed;
    }

    public float getRange() {
        float range = getCore() == 3 ? 24.0F : 16.0F;
        if (hasDecoration("G")) {
            range *= 1.2F;
        }
        return range;
    }

    @Override
    public boolean isWithinHome(BlockPos pos) {
        float range = getRange();
        return getHomePosition().distSqr(pos) < range * range;
    }

    protected void refreshStats() {
        int health = baseHealth() + (hasDecoration("H") ? hatHealthBonus() : 0);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        if (this.getHealth() > health) {
            this.setHealth(health);
        }
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getAIMoveSpeed());
    }

    public ItemStack getCarried() {
        return this.entityData.get(DATA_CARRIED);
    }

    public ItemStack getDisplayCarried() {
        return this.entityData.get(DATA_CARRIED);
    }

    protected void setDisplayCarried(ItemStack stack) {
        this.entityData.set(DATA_CARRIED, stack.copy());
    }

    public void setCarried(ItemStack stack) {
        this.entityData.set(DATA_CARRIED, stack.copy());
    }

    public int getMaxCarried() {
        return maxCarried;
    }

    public int getCarrySpace() {
        ItemStack carried = getCarried();
        return carried.isEmpty() ? maxCarried : Math.min(maxCarried - carried.getCount(), carried.getMaxStackSize() - carried.getCount());
    }

    public int getActionTimer() {
        return 4 - Math.abs(action - 4);
    }

    public void startActionTimer() {
        if (action == 0) {
            action = 8;
            this.level().broadcastEntityEvent(this, (byte) 4);
        }
    }

    public void startLeftArmTimer() {
        if (leftArm == 0) {
            leftArm = 3;
            this.level().broadcastEntityEvent(this, (byte) 6);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            action = 8;
        } else if (id == 5) {
            healing = 5;
        } else if (id == 6) {
            leftArm = 3;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || paused;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (action > 0) {
            action--;
        }
        if (leftArm > 0) {
            leftArm--;
        }
        if (healing > 0) {
            healing--;
        }
        if (regenTimer > 0) {
            regenTimer--;
        } else {
            regenTimer = regenInterval;
            if (hasDecoration("F")) {
                regenTimer /= 2;
            }
            if (!this.level().isClientSide()
                && this.getHealth() < this.getMaxHealth()
                && AuraManager.decreaseClosestAura(this.level(), this.getX(), this.getY(), this.getZ(), 2)) {
                this.level().broadcastEntityEvent(this, (byte) 5);
                this.heal(1.0F);
            }
        }
        if (!this.level().isClientSide()) {
            BlockPos home = getHomePosition();
            if (this.distanceToSqr(home.getX(), home.getY(), home.getZ()) >= 2304.0 || this.isInWall()) {
                returnToHome(home);
            }
        }
    }

    private void returnToHome(BlockPos home) {
        Level level = this.level();
        for (int dy = 1; dy >= -1; dy--) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = home.offset(dx, dy, dz);
                    BlockPos below = pos.below();
                    if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && !level.getBlockState(pos).isRedstoneConductor(level, pos)) {
                        this.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.getYRot(), this.getXRot());
                        this.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        paused = false;
        return !source.is(DamageTypes.CACTUS) && super.hurtServer(level, source, damage);
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int currentSupply) {
        return currentSupply;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        dropContents(level);
    }

    public void dropContents(ServerLevel level) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(level, stack);
            }
        }
        ItemStack carried = getCarried();
        if (!carried.isEmpty()) {
            this.spawnAtLocation(level, carried, 0.5F);
            setCarried(ItemStack.EMPTY);
        }
    }

    public ItemStack toItem() {
        ItemStack stack = new ItemStack(kind().item());
        stack.set(ModDataComponents.GOLEM_CORE.get(), getCore());
        if (!getDecoration().isEmpty()) {
            stack.set(ModDataComponents.GOLEM_DECORATION.get(), getDecoration());
        }
        return stack;
    }

    protected boolean acceptsDecoration(String letter) {
        return letter.equals("F") || letter.equals("H") || letter.equals("G") || letter.equals("B");
    }

    protected boolean addDecoration(String letter, ItemStack stack) {
        String decoration = getDecoration();
        if (decoration.contains(letter)) {
            return false;
        }
        if ((letter.equals("F") || letter.equals("H")) && (decoration.contains("F") || decoration.contains("H"))) {
            return false;
        }
        if ((letter.equals("G") || letter.equals("V")) && (decoration.contains("G") || decoration.contains("V"))) {
            return false;
        }
        if ((letter.equals("B") || letter.equals("P")) && (decoration.contains("B") || decoration.contains("P"))) {
            return false;
        }
        if (!this.level().isClientSide()) {
            setDecoration(decoration + letter);
            stack.shrink(1);
        }
        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof GolemDecorationItem decoration && acceptsDecoration(decoration.letter())) {
            addDecoration(decoration.letter(), stack);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.COOKIE)) {
            stack.consume(1, player);
            for (int index = 0; index < 3; index++) {
                this.level().addParticle(
                    ParticleTypes.HEART,
                    this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                    this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                    this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                    this.random.nextGaussian() * 0.02,
                    this.random.nextGaussian() * 0.02,
                    this.random.nextGaussian() * 0.02
                );
                this.playSound(SoundEvents.GENERIC_EAT.value(), 0.3F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                if (!this.level().isClientSide()) {
                    int duration = 600;
                    MobEffectInstance speed = this.getEffect(MobEffects.SPEED);
                    if (speed != null && speed.getDuration() < 2400) {
                        duration += speed.getDuration();
                    }
                    this.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0));
                }
            }
            this.heal(5.0F);
            return InteractionResult.SUCCESS;
        }
        if (kind().guiId() >= 0 && !(stack.getItem() instanceof CastingWandItem)) {
            return openMenu(player);
        }
        return InteractionResult.PASS;
    }

    protected void addBasicGoals(boolean avoidCreepers, int door, int home, int look) {
        if (avoidCreepers) {
            this.goalSelector.addGoal(0, new AvoidCreeperSwellGoal(this));
        }
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(door, new GolemDoorGoal(this));
        this.goalSelector.addGoal(home, new ReturnHomeGoal(this));
        this.goalSelector.addGoal(look, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(look + 1, new RandomLookAroundGoal(this));
    }

    protected InteractionResult openMenu(Player player) {
        if (!this.level().isClientSide()) {
            player.openMenu(
                new SimpleMenuProvider((id, inventory, opener) -> new GolemMenu(id, inventory, this), this.getDisplayName()),
                buffer -> buffer.writeVarInt(this.getId())
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        BlockPos home = getHomePosition();
        output.putInt("HomeX", home.getX());
        output.putInt("HomeY", home.getY());
        output.putInt("HomeZ", home.getZ());
        output.putInt("HomeFacing", homeFacing.get3DDataValue());
        output.putShort("Color", (short) getColor());
        output.putShort("GolemType", (short) getCore());
        output.putString("Decoration", getDecoration());
        if (!getCarried().isEmpty()) {
            output.store("ItemCarried", ItemStack.CODEC, getCarried());
        }
        output.storeNullable("Owner", UUIDUtil.CODEC, owner);
        output.store("Watched", ItemStack.OPTIONAL_CODEC.listOf(), inventory.saveStacks());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHomeTo(new BlockPos(input.getIntOr("HomeX", 0), input.getIntOr("HomeY", 0), input.getIntOr("HomeZ", 0)), 32);
        this.homeFacing = Direction.from3DDataValue(input.getIntOr("HomeFacing", 0));
        this.entityData.set(DATA_COLOR, (int) input.getShortOr("Color", (short) 0));
        int core = input.getShortOr("GolemType", (short) 0);
        this.entityData.set(DATA_CORE, core);
        this.maxCarried = core == 4 ? 32 : 16;
        this.entityData.set(DATA_CARRIED, input.read("ItemCarried", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        this.entityData.set(DATA_DECORATION, input.getStringOr("Decoration", ""));
        this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
        List<ItemStack> stacks = input.read("Watched", ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());
        inventory.loadStacks(stacks);
        float health = this.getHealth();
        refreshStats();
        this.setHealth(health);
    }
}
