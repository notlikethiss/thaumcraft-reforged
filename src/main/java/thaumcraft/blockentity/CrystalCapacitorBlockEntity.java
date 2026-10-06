package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.aura.AuraManager;
import thaumcraft.aura.AuraNode;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModDataComponents;

public class CrystalCapacitorBlockEntity extends TcBlockEntity {
    public static final int MAX_VIS = 100;
    private static final int INTERVAL = 100;

    private int storedVis;
    private int countdown = -1;

    public CrystalCapacitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_CAPACITOR.get(), pos, state);
    }

    public int getStoredVis() {
        return storedVis;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalCapacitorBlockEntity capacitor) {
        if (capacitor.countdown < 0) {
            capacitor.countdown = level.getRandom().nextInt(INTERVAL);
        }
        if (--capacitor.countdown > 0) {
            return;
        }
        capacitor.countdown = INTERVAL;
        for (Integer key : AuraManager.getAurasWithin(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
            AuraNode node = AuraManager.getNode(key);
            if (node == null) {
                continue;
            }
            if (node.level > node.baseLevel && capacitor.storedVis < MAX_VIS) {
                AuraManager.queueNodeChanges(node.key, -1, 0, false, null, 0.0F, 0.0F, 0.0F);
                capacitor.storedVis++;
                capacitor.sync();
                break;
            }
            if (node.level < node.baseLevel && capacitor.storedVis > 0) {
                AuraManager.queueNodeChanges(node.key, 1, 0, false, null, 0.0F, 0.0F, 0.0F);
                capacitor.storedVis--;
                capacitor.sync();
                break;
            }
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        Integer vis = components.get(ModDataComponents.STORED_VIS.get());
        if (vis != null) {
            storedVis = Mth.clamp(vis, 0, MAX_VIS);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (storedVis > 0) {
            components.set(ModDataComponents.STORED_VIS.get(), storedVis);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedVis = input.getIntOr("stored_vis", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("stored_vis", storedVis);
    }
}
