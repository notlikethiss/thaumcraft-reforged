package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aura.AuraManager;
import thaumcraft.registry.ModBlockEntities;

public class CrystalClusterBlockEntity extends BlockEntity {
    private static final int INTERVAL = 6000;

    private int countdown = -1;

    public CrystalClusterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_CLUSTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalClusterBlockEntity crystal) {
        if (crystal.countdown < 0) {
            crystal.countdown = level.getRandom().nextInt(INTERVAL);
        }
        if (--crystal.countdown <= 0) {
            crystal.countdown = INTERVAL;
            AuraManager.increaseLowestAuraWithLimit(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 1.1F);
        }
    }
}
