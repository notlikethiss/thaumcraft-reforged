package thaumcraft.block.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.fx.Fx;

public class TravelPavingStoneBlock extends Block {
    public TravelPavingStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity living) {
            int strength = 0;
            MobEffectInstance speed = living.getEffect(MobEffects.MOVEMENT_SPEED);
            if (speed != null) {
                strength = Math.min(3, speed.getAmplifier() + 1);
            }
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, strength));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, strength));
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, pos.getX(), pos.getY(), pos.getZ(), 3, 5);
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
