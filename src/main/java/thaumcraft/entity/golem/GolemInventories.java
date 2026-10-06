package thaumcraft.entity.golem;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import javax.annotation.Nullable;

public final class GolemInventories {
    private GolemInventories() {
    }

    private static @Nullable ResourceHandler<ItemResource> items(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    private static @Nullable ResourceHandler<FluidResource> fluids(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    }

    public static boolean hasItems(Level level, BlockPos pos, @Nullable Direction side) {
        return items(level, pos, side) != null;
    }

    public static boolean hasFluids(Level level, BlockPos pos, @Nullable Direction side) {
        return fluids(level, pos, side) != null;
    }

    public static int count(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (!resource.isEmpty() && resource.matches(stack)) {
                total += handler.getAmountAsInt(index);
            }
        }
        return total;
    }

    public static boolean contains(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null) {
            return false;
        }
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (!resource.isEmpty() && filter.test(resource.toStack(handler.getAmountAsInt(index)))) {
                return true;
            }
        }
        return false;
    }

    public static int insert(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack, boolean simulate) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null || stack.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted;
        }
    }

    public static ItemStack extractFirst(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter, int maxAmount) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (resource.isEmpty()) {
                continue;
            }
            int amount = handler.getAmountAsInt(index);
            if (!filter.test(resource.toStack(amount))) {
                continue;
            }
            int wanted = Math.min(amount, maxAmount);
            if (wanted <= 0) {
                return ItemStack.EMPTY;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(index, resource, wanted, transaction);
                transaction.commit();
                return resource.toStack(extracted);
            }
        }
        return ItemStack.EMPTY;
    }

    public static int fluidAmount(Level level, BlockPos pos, @Nullable Direction side, Fluid fluid) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (!resource.isEmpty() && resource.getFluid().isSame(fluid)) {
                total += handler.getAmountAsInt(index);
            }
        }
        return total;
    }

    public static int fillFluid(Level level, BlockPos pos, @Nullable Direction side, Fluid fluid, int amount, boolean simulate) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(FluidResource.of(fluid), amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted;
        }
    }

    public static int drainFluid(Level level, BlockPos pos, @Nullable Direction side, Fluid fluid, int amount, boolean simulate) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(FluidResource.of(fluid), amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return extracted;
        }
    }

    public static @Nullable Fluid firstFluid(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null) {
            return null;
        }
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (!resource.isEmpty() && handler.getAmountAsInt(index) > 0) {
                return resource.getFluid();
            }
        }
        return null;
    }
}
