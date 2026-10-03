package thaumcraft.entity.golem.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import thaumcraft.entity.golem.GolemBase;

public class GolemDoorGoal extends Goal {
    private final GolemBase golem;
    private BlockPos doorPos = BlockPos.ZERO;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;
    private int count;
    private int forgetTime;

    public GolemDoorGoal(GolemBase golem) {
        this.golem = golem;
    }

    private static boolean isDoor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return DoorBlock.isWoodenDoor(state) || state.getBlock() instanceof FenceGateBlock;
    }

    @Override
    public boolean canUse() {
        if (!this.golem.horizontalCollision) {
            return false;
        }
        Path path = this.golem.getNavigation().getPath();
        if (path == null || path.isDone()) {
            return false;
        }
        Level level = this.golem.level();
        for (int index = 0; index < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); index++) {
            Node node = path.getNode(index);
            BlockPos pos = new BlockPos(node.x, node.y, node.z);
            if (this.golem.distanceToSqr(pos.getX(), this.golem.getY(), pos.getZ()) <= 2.25) {
                for (BlockPos candidate : new BlockPos[] {pos, pos.above()}) {
                    if (isDoor(level, candidate)) {
                        this.doorPos = candidate;
                        return true;
                    }
                }
            }
        }
        BlockPos pos = this.golem.blockPosition();
        for (BlockPos candidate : new BlockPos[] {pos, pos.above()}) {
            if (isDoor(level, candidate)) {
                this.doorPos = candidate;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.forgetTime > 0 && this.count > 0 && !this.passed;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.count = 100;
        this.forgetTime = 20;
        this.passed = false;
        this.doorOpenDirX = (float) (this.doorPos.getX() + 0.5 - this.golem.getX());
        this.doorOpenDirZ = (float) (this.doorPos.getZ() + 0.5 - this.golem.getZ());
        setOpen(true);
    }

    @Override
    public void stop() {
        setOpen(false);
    }

    @Override
    public void tick() {
        this.count--;
        this.forgetTime--;
        float dirX = (float) (this.doorPos.getX() + 0.5 - this.golem.getX());
        float dirZ = (float) (this.doorPos.getZ() + 0.5 - this.golem.getZ());
        if (this.doorOpenDirX * dirX + this.doorOpenDirZ * dirZ < 0.0F) {
            this.passed = true;
        }
    }

    private void setOpen(boolean open) {
        Level level = this.golem.level();
        BlockState state = level.getBlockState(this.doorPos);
        if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(this.golem, level, state, this.doorPos, open);
        } else if (state.getBlock() instanceof FenceGateBlock && state.getValue(FenceGateBlock.OPEN) != open) {
            BlockState newState = state.setValue(FenceGateBlock.OPEN, open);
            if (open) {
                Direction direction = this.golem.getDirection();
                if (state.getValue(FenceGateBlock.FACING) == direction.getOpposite()) {
                    newState = newState.setValue(FenceGateBlock.FACING, direction);
                }
            }
            level.setBlock(this.doorPos, newState, 10);
            level.playSound(null, this.doorPos, open ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(this.golem, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, this.doorPos);
        }
    }
}
