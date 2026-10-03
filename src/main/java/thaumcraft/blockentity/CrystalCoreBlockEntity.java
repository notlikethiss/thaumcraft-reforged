package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import thaumcraft.aura.AuraManager;
import thaumcraft.aura.AuraNode;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;

public class CrystalCoreBlockEntity extends TcBlockEntity {
    private static final int[][] TOTEMS = {{-1, -1}, {1, 1}, {-1, 1}, {1, -1}};

    private boolean active;
    private int nodeKey = -1;
    private float speed;
    private float rotation;
    private int count = -1;

    public CrystalCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_CORE.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public float getSpeed() {
        return speed;
    }

    public float getRotation() {
        return rotation;
    }

    public void activate() {
        active = true;
        sync();
    }

    private boolean isValidStructure(Level level) {
        for (int[] totem : TOTEMS) {
            for (int dy = 1; dy <= 2; dy++) {
                if (!isStructureBlock(level.getBlockState(worldPosition.offset(totem[0], -dy, totem[1])))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isStructureBlock(BlockState state) {
        return state.is(ModBlocks.OBSIDIAN_TOTEM.get()) || state.is(ModBlocks.OBSIDIAN_TILE.get()) || state.is(ModBlocks.TRAVEL_PAVING_STONE.get());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrystalCoreBlockEntity core) {
        core.count++;
        core.rotation += core.speed;
        if (core.active) {
            core.speed = core.speed < 1.0F ? core.speed + 0.001F + core.speed / 100.0F : 1.0F;
        } else {
            core.speed = core.speed > 0.0F ? core.speed - 0.01F : 0.0F;
        }
        if (level.isClientSide()) {
            core.clientTick(level);
        } else {
            core.serverTick(level);
        }
    }

    private void clientTick(Level level) {
        if (!active || speed <= 0.9F || nodeKey < 0) {
            return;
        }
        RandomSource random = level.getRandom();
        if (count % 2 == 0) {
            int[] totem = TOTEMS[count % 8 / 2];
            Fx.get().blockRunes(level, worldPosition.getX() + totem[0], worldPosition.getY() - 1, worldPosition.getZ() + totem[1],
                0.3F + random.nextFloat() * 0.7F, 0.0F, 0.3F + random.nextFloat() * 0.7F, 20);
        }
        if (count % 20 == 0) {
            Fx.get().crystalCoreBeam(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5 + speed, worldPosition.getZ() + 0.5, nodeKey);
        }
    }

    private void serverTick(Level level) {
        if (!active || speed <= 0.9F || count % 20 != 0) {
            return;
        }
        if (count % 100 == 0 && nodeKey > -1 && AuraManager.getNode(nodeKey) == null) {
            nodeKey = -1;
        }
        if (!isValidStructure(level)) {
            active = false;
            sync();
        }
        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY() + 0.5;
        double cz = worldPosition.getZ() + 0.5;
        if (nodeKey < 0) {
            nodeKey = AuraManager.getClosestAuraWithinRange(level, cx, cy, cz, 24.0);
            if (nodeKey < 0) {
                active = false;
            }
            sync();
        }
        if (!active) {
            return;
        }
        AuraNode node = AuraManager.getNode(nodeKey);
        if (node == null) {
            return;
        }
        if (count % 80 == 0) {
            AuraManager.decreaseClosestAura(level, cx, cy, cz, 1);
        }
        double dist = Math.sqrt(node.distanceSq(cx, cy, cz));
        float x;
        float y;
        float z;
        if (dist < 0.2) {
            active = false;
            sync();
            x = (float) (cx - node.x);
            y = (float) (cy - node.y);
            z = (float) (cz - node.z);
        } else {
            dist *= 20.0;
            x = (float) ((cx - node.x) / dist);
            y = (float) ((cy - node.y) / dist);
            z = (float) ((cz - node.z) / dist);
        }
        AuraManager.queueNodeChanges(nodeKey, 0, 0, false, null, Mth.clamp(x, -0.25F, 0.25F), Mth.clamp(y, -0.25F, 0.25F), Mth.clamp(z, -0.25F, 0.25F));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        active = input.getBooleanOr("active", false);
        speed = input.getFloatOr("speed", 0.0F);
        nodeKey = input.getIntOr("node", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("active", active);
        output.putFloat("speed", speed);
        output.putInt("node", nodeKey);
    }
}
