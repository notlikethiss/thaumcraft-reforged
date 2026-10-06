package thaumcraft.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import thaumcraft.Config;

public class BrainyZombie extends Zombie {
    public BrainyZombie(EntityType<? extends BrainyZombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 25.0)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23F * 1.25F)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.ARMOR, 5.0);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
        return allowedBiome(level.getBiome(this.blockPosition())) && super.checkSpawnRules(level, spawnReason);
    }

    public static boolean checkBrainyZombieSpawnRules(
        EntityType<? extends Monster> type,
        ServerLevelAccessor level,
        MobSpawnType spawnReason,
        BlockPos pos,
        RandomSource random
    ) {
        if (spawnReason == MobSpawnType.NATURAL && !Config.SPAWN_ANGRY_ZOMBIES.getAsBoolean()) {
            return false;
        }
        return allowedBiome(level.getBiome(pos)) && Monster.checkMonsterSpawnRules(type, level, spawnReason, pos, random);
    }

    private static boolean allowedBiome(Holder<Biome> biome) {
        return !biome.is(BiomeTags.IS_NETHER) && !biome.is(Biomes.MUSHROOM_FIELDS);
    }
}
