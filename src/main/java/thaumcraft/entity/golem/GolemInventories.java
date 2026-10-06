package thaumcraft.entity.golem;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class GolemInventories {
    private GolemInventories() {
    }

    private static @Nullable IItemHandler items(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
    }

    private static @Nullable IFluidHandler fluids(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
    }

    public static boolean hasItems(Level level, BlockPos pos, @Nullable Direction side) {
        return items(level, pos, side) != null;
    }

    public static boolean hasFluids(Level level, BlockPos pos, @Nullable Direction side) {
        return fluids(level, pos, side) != null;
    }

    public static int count(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack inSlot = handler.getStackInSlot(index);
            if (!inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, stack)) {
                total += inSlot.getCount();
            }
        }
        return total;
    }

    public static boolean contains(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null) {
            return false;
        }
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack inSlot = handler.getStackInSlot(index);
            if (!inSlot.isEmpty() && filter.test(inSlot.copy())) {
                return true;
            }
        }
        return false;
    }

    public static int insert(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack, boolean simulate) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null || stack.isEmpty()) {
            return 0;
        }
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), simulate);
        return stack.getCount() - remainder.getCount();
    }

    public static ItemStack extractFirst(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter, int maxAmount) {
        IItemHandler handler = items(level, pos, side);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack inSlot = handler.getStackInSlot(index);
            if (inSlot.isEmpty()) {
                continue;
            }
            if (!filter.test(inSlot.copy())) {
                continue;
            }
            int wanted = Math.min(inSlot.getCount(), maxAmount);
            if (wanted <= 0) {
                return ItemStack.EMPTY;
            }
            return handler.extractItem(index, wanted, false);
        }
        return ItemStack.EMPTY;
    }

    public static int fluidAmount(Level level, BlockPos pos, @Nullable Direction side, Fluid fluid) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.getTanks(); index++) {
            FluidStack inTank = handler.getFluidInTank(index);
            if (!inTank.isEmpty() && inTank.getFluid().isSame(fluid)) {
                total += inTank.getAmount();
            }
        }
        return total;
    }

    public static int fillFluid(Level level, BlockPos pos, @Nullable Direction side, Fluid fluid, int amount, boolean simulate) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        return handler.fill(new FluidStack(fluid, amount), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    public static int drainFluid(Level level, BlockPos pos, @Nullable Direction side, Fluid fluid, int amount, boolean simulate) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        return handler.drain(new FluidStack(fluid, amount), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE).getAmount();
    }

    public static @Nullable Fluid firstFluid(Level level, BlockPos pos, @Nullable Direction side) {
        IFluidHandler handler = fluids(level, pos, side);
        if (handler == null) {
            return null;
        }
        for (int index = 0; index < handler.getTanks(); index++) {
            FluidStack inTank = handler.getFluidInTank(index);
            if (!inTank.isEmpty() && inTank.getAmount() > 0) {
                return inTank.getFluid();
            }
        }
        return null;
    }
}
