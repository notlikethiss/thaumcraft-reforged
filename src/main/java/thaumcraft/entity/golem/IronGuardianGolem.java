package thaumcraft.entity.golem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.entity.golem.goal.DartAttackGoal;
import thaumcraft.entity.golem.goal.GolemMeleeGoal;
import thaumcraft.entity.golem.goal.GolemNearestTargetGoal;
import thaumcraft.entity.projectile.Dart;
import thaumcraft.registry.ModSounds;

public class IronGuardianGolem extends GolemBase {
    private static final EntityDataAccessor<Byte> DATA_TARGETS = SynchedEntityData.defineId(IronGuardianGolem.class, EntityDataSerializers.BYTE);
    public static final int HOSTILES = 1;
    public static final int ANIMALS = 2;
    public static final int PLAYERS = 4;
    public static final int CREEPERS = 8;

    private final DartAttackGoal dartGoal = new DartAttackGoal(this);
    private boolean dartGoalAdded;

    public IronGuardianGolem(EntityType<? extends IronGuardianGolem> type, Level level) {
        super(type, level);
        this.regenInterval = 100;
    }

    @Override
    public GolemKind kind() {
        return GolemKind.IRON_GUARDIAN;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TARGETS, (byte) HOSTILES);
    }

    @Override
    protected void registerGoals() {
        addBasicGoals(true, 6, 5, 7);
        this.goalSelector.addGoal(3, new GolemMeleeGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new GolemNearestTargetGoal(this));
    }

    @Override
    protected int baseHealth() {
        return 40;
    }

    @Override
    protected int hatHealthBonus() {
        return 10;
    }

    @Override
    protected void refreshStats() {
        super.refreshStats();
        int armor = 8 + (hasDecoration("V") ? 1 : 0) + (hasDecoration("P") ? 4 : 0);
        this.getAttribute(Attributes.ARMOR).setBaseValue(Math.min(20, armor));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4 + (getCore() == 4 ? 2 : 0));
        if (hasDecoration("R") && !dartGoalAdded) {
            dartGoalAdded = true;
            this.goalSelector.addGoal(2, dartGoal);
        }
    }

    @Override
    protected boolean acceptsDecoration(String letter) {
        return super.acceptsDecoration(letter) || letter.equals("R") || letter.equals("V") || letter.equals("P");
    }

    public boolean hasTargetFlag(int flag) {
        return (this.entityData.get(DATA_TARGETS) & flag) != 0;
    }

    public void setTargetFlag(int flag, boolean value) {
        if (flag == PLAYERS && value && !(this.level() instanceof ServerLevel level && level.getServer().isPvpAllowed())) {
            value = false;
        }
        byte flags = this.entityData.get(DATA_TARGETS);
        this.entityData.set(DATA_TARGETS, (byte) (value ? flags | flag : flags & ~flag));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof AbstractVillager) && !(target instanceof GolemBase) && !(target instanceof Bat) && super.canAttack(target);
    }

    public boolean isValidTarget(Entity target) {
        if (target instanceof Player player && isOwner(player)) {
            return false;
        }
        if (hasTargetFlag(CREEPERS) && target instanceof Creeper) {
            return true;
        }
        if (hasTargetFlag(HOSTILES) && target instanceof Enemy && !(target instanceof Creeper)) {
            return true;
        }
        if (hasTargetFlag(ANIMALS)
            && target instanceof Animal
            && !(target instanceof Enemy)
            && !(target instanceof TamableAnimal tamable && tamable.isTame())
            && !(target instanceof AbstractGolem)) {
            return true;
        }
        return hasTargetFlag(PLAYERS) && target instanceof Player;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (hasDecoration("V") && getOwnerId() != null && target instanceof LivingEntity living) {
            Player owner = this.level().getPlayerByUUID(getOwnerId());
            if (owner != null) {
                living.setLastHurtByPlayer(owner);
                living.lastHurtByPlayerTime = 100;
            }
        }
        return super.doHurtTarget(target);
    }

    public void shootDart(LivingEntity target) {
        Dart dart = new Dart(this.level(), this, target, 1.6F, getCore() == 3 ? 3.5F : 7.0F);
        dart.setBaseDamage(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4F);
        this.playSound(ModSounds.GOLEMIRONSHOOT.get(), 0.5F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.6F));
        this.level().addFreshEntity(dart);
        startLeftArmTimer();
    }

    @Override
    protected float getSoundVolume() {
        return 0.2F;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.GOLEMIRON.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GOLEMIRON.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GOLEMIRON.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ValueOutput output = ValueOutput.of(tag, this.registryAccess());
        output.putByte("Targets", this.entityData.get(DATA_TARGETS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ValueInput input = ValueInput.of(tag, this.registryAccess());
        this.entityData.set(DATA_TARGETS, input.getByteOr("Targets", (byte) HOSTILES));
    }
}
