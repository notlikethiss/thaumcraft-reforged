package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.EssentiaContainer;
import thaumcraft.aura.AuraManager;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class AlembicBlockEntity extends TcBlockEntity implements EssentiaContainer {
    public static final int MAX_AMOUNT = 16;

    private Aspect aspect = Aspect.UNKNOWN;
    private int amount;

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALEMBIC.get(), pos, state);
    }

    public Aspect getAspect() {
        return aspect;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public @Nullable Aspect getContainedAspect() {
        return amount > 0 ? aspect : null;
    }

    @Override
    public int getContainedAmount() {
        return amount;
    }

    @Override
    public int getMaxAmount() {
        return MAX_AMOUNT;
    }

    public void spillRemnants() {
        if (level == null || amount <= 0) {
            return;
        }
        AuraManager.addFluxToClosest(
            level,
            worldPosition.getX() + 0.5F,
            worldPosition.getY() + 0.5F,
            worldPosition.getZ() + 0.5F,
            new AspectList().add(aspect, amount)
        );
        takeFromSource(aspect, amount);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && amount > 0) {
            AuraManager.addFluxToClosest(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, new AspectList().add(aspect, amount));
        }
    }

    @Override
    public boolean triggerEvent(int type, int param) {
        if (type == 0) {
            if (level != null && level.isClientSide()) {
                Fx.get().alembicSpill(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), param);
            }
            return true;
        }
        return super.triggerEvent(type, param);
    }

    @Override
    public int addToSource(Aspect added, int addedAmount) {
        if ((amount < MAX_AMOUNT && added == aspect) || amount == 0) {
            aspect = added;
            int accepted = Math.min(addedAmount, MAX_AMOUNT - amount);
            amount += accepted;
            addedAmount -= accepted;
        }
        sync();
        return addedAmount;
    }

    @Override
    public boolean takeFromSource(Aspect taken, int takenAmount) {
        if (amount >= takenAmount && taken == aspect) {
            amount -= takenAmount;
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean takeFromSource(AspectList aspects) {
        return false;
    }

    @Override
    public boolean doesSourceContainAmount(Aspect checked, int checkedAmount) {
        return amount >= checkedAmount && checked == aspect;
    }

    @Override
    public boolean doesSourceContain(AspectList aspects) {
        return amount > 0 && aspects.getAmount(aspect) > 0;
    }

    @Override
    public int sourceContains(Aspect checked) {
        return checked == aspect ? amount : 0;
    }

    @Override
    public AspectList getSourceTags() {
        AspectList result = new AspectList();
        if (amount > 0) {
            result.add(aspect, amount);
        }
        return result;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("Aspect", Aspect.CODEC).orElse(Aspect.UNKNOWN);
        amount = input.getIntOr("Amount", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Aspect", Aspect.CODEC, aspect);
        output.putInt("Amount", amount);
    }
}
