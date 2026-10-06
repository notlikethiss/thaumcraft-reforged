package thaumcraft.blockentity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.block.device.JarBlock;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;

public class BrainJarBlockEntity extends AbstractJarBlockEntity {
    public static final int MAX_XP = 2000;

    public float rota;
    public float rotb;
    private float targetRotation;
    private int xp;
    private int eatDelay;
    private int sighDelay = 30;

    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRAIN_JAR.get(), pos, state);
    }

    @Override
    protected boolean canSpaz() {
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity jar) {
        jar.tickCommon(level);
        if (jar.eatDelay > 0) {
            jar.eatDelay--;
        } else if (jar.xp < MAX_XP) {
            jar.eatOrbs(level);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity jar) {
        Entity target = jar.tickCommon(level);
        jar.clientTick(level);
        jar.tickBrain(level, target);
        if (jar.eatDelay > 0) {
            jar.eatDelay--;
        }
    }

    private @Nullable Entity tickCommon(Level level) {
        if (xp > MAX_XP) {
            xp = MAX_XP;
        }
        if (xp >= MAX_XP) {
            return null;
        }
        Entity orb = closestOrb(level);
        if (orb != null && eatDelay == 0) {
            double dx = (worldPosition.getX() + 0.5 - orb.getX()) / 7.0;
            double dy = (worldPosition.getY() + 0.5 - orb.getY()) / 7.0;
            double dz = (worldPosition.getZ() + 0.5 - orb.getZ()) / 7.0;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double strength = 1.0 - distance;
            if (strength > 0.0) {
                strength *= strength;
                orb.setDeltaMovement(orb.getDeltaMovement().add(
                    dx / distance * strength * 0.15,
                    dy / distance * strength * 0.33,
                    dz / distance * strength * 0.15
                ));
            }
        }
        return orb;
    }

    private void tickBrain(Level level, @Nullable Entity target) {
        RandomSource random = level.getRandom();
        rotb = rota;
        if (target == null) {
            target = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 6.0, false);
            if (target != null && --sighDelay <= 0) {
                level.playLocalSound(worldPosition, ModSounds.BRAIN.get(), SoundSource.BLOCKS, 0.25F, 0.8F + random.nextFloat() * 0.4F, false);
                sighDelay = 100 + random.nextInt(500);
            }
        }
        if (target != null) {
            targetRotation = (float) Math.atan2(target.getZ() - (worldPosition.getZ() + 0.5), target.getX() - (worldPosition.getX() + 0.5));
        } else {
            targetRotation += 0.01F;
        }
        rota = Mth.wrapDegrees(rota * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        targetRotation = Mth.wrapDegrees(targetRotation * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        float delta = Mth.wrapDegrees((targetRotation - rota) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        rota += delta * 0.04F;
    }

    private @Nullable Entity closestOrb(Level level) {
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(worldPosition).inflate(6.0));
        Vec3 center = Vec3.atCenterOf(worldPosition);
        ExperienceOrb closest = null;
        double best = Double.MAX_VALUE;
        for (ExperienceOrb orb : orbs) {
            double distance = orb.distanceToSqr(center);
            if (distance < best) {
                best = distance;
                closest = orb;
            }
        }
        return closest;
    }

    private void eatOrbs(Level level) {
        RandomSource random = level.getRandom();
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(worldPosition))) {
            xp += orb.getValue() * orb.count;
            level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.GENERIC_EAT.value(), SoundSource.BLOCKS, 0.1F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            orb.discard();
            setChanged();
        }
    }

    public void release(ServerLevel level) {
        eatDelay = 40;
        int amount = level.getRandom().nextInt(Math.min(xp + 1, 64));
        if (amount > 0) {
            xp -= amount;
            setChanged();
            ExperienceOrb.award(level, Vec3.atCenterOf(worldPosition), amount);
        }
    }

    public void startEatDelay() {
        eatDelay = 40;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            JarBlock.dropExperience(serverLevel, pos, this);
        }
    }

    public int getXp() {
        return xp;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        xp = input.getIntOr("XP", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("XP", xp);
    }
}
