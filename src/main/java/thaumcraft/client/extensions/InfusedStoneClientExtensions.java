package thaumcraft.client.extensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import thaumcraft.block.world.InfusedStoneBlock;
import thaumcraft.fx.Fx;

public class InfusedStoneClientExtensions implements IClientBlockExtensions {
    @Override
    public boolean addHitEffects(BlockState state, Level level, BlockPos pos, Direction face, ParticleEngine manager) {
        if (state.getBlock() instanceof InfusedStoneBlock stone && stone.getType() < 6) {
            Fx.get().infusedStoneSparkle(level, pos.getX(), pos.getY(), pos.getZ(), stone.getType());
        }
        return false;
    }
}
