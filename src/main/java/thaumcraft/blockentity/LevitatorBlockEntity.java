package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.aura.AuraManager;
import thaumcraft.block.device.LevitatorBlock;
import thaumcraft.registry.ModBlockEntities;

public class LevitatorBlockEntity extends BlockEntity {
    private int counter;
    private int rangeAbove;
    private int rangeBelow;
    private boolean requiresUpdate = true;
    private int cost;

    public LevitatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITATOR.get(), pos, state);
    }

    public void requireUpdate() {
        requiresUpdate = true;
    }

    public int getRangeAbove() {
        return rangeAbove;
    }

    public int getRangeBelow() {
        return rangeBelow;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LevitatorBlockEntity levitator) {
        levitator.counter++;
        if (levitator.requiresUpdate || levitator.counter % 200 == 0 || level.isClientSide() && levitator.counter % 10 == 0) {
            levitator.requiresUpdate = false;
            levitator.rangeAbove = range(level, pos, 1);
            levitator.rangeBelow = range(level, pos, -1);
        }
        if (state.getValue(LevitatorBlock.POWERED)) {
            return;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (levitator.rangeAbove > 0) {
            for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(x, y + 1, z, x + 1, y + 1 + levitator.rangeAbove, z + 1), Entity::isPushable)) {
                Vec3 motion = entity.getDeltaMovement();
                if (motion.y < 0.35F) {
                    entity.setDeltaMovement(motion.x, motion.y + 0.1F, motion.z);
                }
                entity.resetFallDistance();
                levitator.cost++;
            }
        }
        if (levitator.rangeBelow > 0) {
            for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(x, y - 1 - levitator.rangeBelow, z, x + 1, y - 1, z + 1), Entity::isPushable)) {
                Vec3 motion = entity.getDeltaMovement();
                if (motion.y < 0.0) {
                    entity.setDeltaMovement(motion.x, motion.y * 0.9F, motion.z);
                }
                entity.resetFallDistance();
                levitator.cost++;
            }
        }
        if (levitator.cost > 100) {
            levitator.cost = 0;
            if (!level.isClientSide()) {
                AuraManager.decreaseClosestAura(level, x, y, z, 1);
            }
        }
    }

    private static int range(Level level, BlockPos pos, int direction) {
        int max = 10;
        for (int count = 1; isActiveLevitator(level, pos.above(-direction * count)); count++) {
            max += 10;
        }
        int range = 0;
        while (range < max && !level.getBlockState(pos.above(direction * (1 + range))).isSolidRender()) {
            range++;
        }
        return range;
    }

    private static boolean isActiveLevitator(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof LevitatorBlock && !state.getValue(LevitatorBlock.POWERED);
    }
}
