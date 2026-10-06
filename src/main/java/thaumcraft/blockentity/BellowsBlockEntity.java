package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModBlockEntities;

public class BellowsBlockEntity extends BlockEntity {
    public float inflation = 1.0F;
    public float prevInflation = 1.0F;
    private boolean inflating;
    private boolean firstRun = true;
    private int delay;

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        RandomSource random = level.getRandom();
        if (bellows.firstRun) {
            bellows.inflation = 0.35F + random.nextFloat() * 0.55F;
            bellows.firstRun = false;
        }
        bellows.prevInflation = bellows.inflation;
        if (bellows.inflation > 0.35F && !bellows.inflating) {
            bellows.inflation -= 0.075F;
        }
        if (bellows.inflation <= 0.35F && !bellows.inflating) {
            bellows.inflating = true;
        }
        if (bellows.inflation < 1.0F && bellows.inflating) {
            bellows.inflation += 0.025F;
        }
        if (bellows.inflation >= 1.0F && bellows.inflating) {
            bellows.inflating = false;
            level.playLocalSound(pos, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.033F, 0.5F + (random.nextFloat() - random.nextFloat()) * 0.2F, false);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (++bellows.delay < 3) {
            return;
        }
        bellows.delay = 0;
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        if (level.getBlockEntity(pos.relative(facing)) instanceof AbstractFurnaceBlockEntity furnace
            && furnace.cookingProgress > 0
            && furnace.cookingProgress < furnace.cookingTotalTime - 1) {
            furnace.cookingProgress++;
        }
    }
}
