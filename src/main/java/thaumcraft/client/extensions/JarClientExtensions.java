package thaumcraft.client.extensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import thaumcraft.blockentity.AbstractJarBlockEntity;

public class JarClientExtensions implements IClientBlockExtensions {
    @Override
    public boolean addHitEffects(BlockState state, Level level, BlockPos pos, Direction face, ParticleEngine manager) {
        if (level.getBlockEntity(pos) instanceof AbstractJarBlockEntity jar && jar.isStill()) {
            jar.wobble(face, level.getRandom());
        }
        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return true;
    }
}
