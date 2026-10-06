package thaumcraft.client.extensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import thaumcraft.blockentity.AbstractJarBlockEntity;

public class JarClientExtensions implements IClientBlockExtensions {
    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (target instanceof BlockHitResult hit && level.getBlockEntity(hit.getBlockPos()) instanceof AbstractJarBlockEntity jar && jar.isStill()) {
            jar.wobble(hit.getDirection(), level.getRandom());
        }
        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return true;
    }
}
