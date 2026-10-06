package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.EssentiaContainer;
import thaumcraft.block.device.JarBlock;
import thaumcraft.item.JarContents;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModDataComponents;

public class JarBlockEntity extends AbstractJarBlockEntity implements EssentiaContainer {
    public static final int MAX_AMOUNT = 64;

    private Aspect aspect = Aspect.UNKNOWN;
    private int amount;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WARDED_JAR.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, JarBlockEntity jar) {
        jar.clientTick(level);
    }

    public Aspect getAspect() {
        return aspect;
    }

    public int getAmount() {
        return amount;
    }

    public @Nullable JarContents getContents() {
        return amount > 0 ? new JarContents(aspect, amount) : null;
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

    @Override
    public boolean acceptsPhials() {
        return true;
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
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            JarBlock.dropContents(level, pos, this);
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        JarContents contents = components.get(ModDataComponents.JAR_CONTENTS.get());
        if (contents != null) {
            aspect = contents.aspect();
            amount = Math.min(contents.amount(), MAX_AMOUNT);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        JarContents contents = getContents();
        if (contents != null) {
            components.set(ModDataComponents.JAR_CONTENTS.get(), contents);
        }
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
