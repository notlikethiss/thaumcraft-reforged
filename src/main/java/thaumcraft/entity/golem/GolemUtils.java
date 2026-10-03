package thaumcraft.entity.golem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.block.device.AlembicBlock;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;
import thaumcraft.Config;
import thaumcraft.crafting.ConfigRecipes;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModPoiTypes;

public final class GolemUtils {
    private GolemUtils() {
    }

    public record MarkedContainer(BlockPos pos, Direction side, BlockPos marker) {
    }

    public static int markerColor(BlockState state) {
        for (int index = 0; index < ConfigRecipes.WOOL_COLORS.length; index++) {
            if (state.is(ModBlocks.MARKERS.get(ConfigRecipes.WOOL_COLORS[index]).get())) {
                return index;
            }
        }
        return -1;
    }

    public static boolean sameItem(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && ItemStack.isSameItemSameComponents(first, second);
    }

    public static @Nullable ResourceHandler<ItemResource> handler(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    public static List<BlockPos> markers(ServerLevel level, BlockPos center, int radius) {
        return level.getPoiManager()
            .getInRange(holder -> holder.is(ModPoiTypes.MARKER.getKey()), center, radius, PoiManager.Occupancy.ANY)
            .map(PoiRecord::getPos)
            .toList();
    }

    public static boolean colorMatches(Level level, BlockPos marker, int color) {
        return color == -1 || markerColor(level.getBlockState(marker)) == color;
    }

    public static List<MarkedContainer> adjacentContainers(Level level, BlockPos marker) {
        List<MarkedContainer> containers = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos pos = marker.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || blockEntity instanceof AbstractFurnaceBlockEntity && direction.getAxis().isHorizontal()) {
                continue;
            }
            Direction side = direction.getOpposite();
            if (handler(level, pos, side) != null) {
                containers.add(new MarkedContainer(pos, side, marker));
            }
        }
        return containers;
    }

    public static int count(ResourceHandler<ItemResource> handler, ItemStack stack) {
        int total = 0;
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (!resource.isEmpty() && resource.matches(stack)) {
                total += handler.getAmountAsInt(index);
            }
        }
        return total;
    }

    public static boolean contains(ResourceHandler<ItemResource> handler, Predicate<ItemStack> filter) {
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (!resource.isEmpty() && filter.test(resource.toStack(handler.getAmountAsInt(index)))) {
                return true;
            }
        }
        return false;
    }

    public static int insert(ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
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

    public static ItemStack extractFirst(ResourceHandler<ItemResource> handler, Predicate<ItemStack> filter, int maxAmount) {
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

    private static int containerRange(GolemBase golem) {
        return golem.getCore() == 3 ? 32 : 24;
    }

    public static List<MarkedContainer> containersWithGoods(GolemBase golem, ItemStack goods) {
        List<MarkedContainer> results = new ArrayList<>();
        if (!(golem.level() instanceof ServerLevel level)) {
            return results;
        }
        int range = containerRange(golem);
        for (BlockPos marker : markers(level, golem.blockPosition(), range + 1)) {
            if (!colorMatches(level, marker, golem.getColor())) {
                continue;
            }
            for (MarkedContainer container : adjacentContainers(level, marker)) {
                if (golem.distanceToSqr(Vec3.atCenterOf(container.pos())) < range * range) {
                    ResourceHandler<ItemResource> handler = handler(level, container.pos(), container.side());
                    if (handler != null && contains(handler, stack -> sameItem(stack, goods))) {
                        results.add(container);
                    }
                }
            }
        }
        return results;
    }

    public static List<MarkedContainer> containersWithRoom(GolemBase golem, ItemStack stack) {
        List<MarkedContainer> results = new ArrayList<>();
        if (!(golem.level() instanceof ServerLevel level)) {
            return results;
        }
        int range = containerRange(golem);
        for (BlockPos marker : markers(level, golem.blockPosition(), range + 1)) {
            if (!colorMatches(level, marker, golem.getColor())) {
                continue;
            }
            for (MarkedContainer container : adjacentContainers(level, marker)) {
                if (golem.distanceToSqr(Vec3.atCenterOf(container.pos())) < range * range) {
                    ResourceHandler<ItemResource> handler = handler(level, container.pos(), container.side());
                    if (handler != null && insert(handler, stack, true) > 0) {
                        results.add(container);
                    }
                }
            }
        }
        return results;
    }

    public static List<MarkedContainer> adjacentMarkedContainers(GolemBase golem, Predicate<BlockPos> markerFilter) {
        List<MarkedContainer> results = new ArrayList<>();
        if (!(golem.level() instanceof ServerLevel level)) {
            return results;
        }
        for (BlockPos marker : markers(level, golem.blockPosition(), 4)) {
            if (golem.distanceToSqr(Vec3.atCenterOf(marker)) >= 10.0 || !markerFilter.test(marker)) {
                continue;
            }
            for (MarkedContainer container : adjacentContainers(level, marker)) {
                if (golem.distanceToSqr(Vec3.atCenterOf(container.pos())) <= 8.0) {
                    results.add(container);
                }
            }
        }
        results.sort(Comparator.comparingDouble(container -> golem.distanceToSqr(Vec3.atCenterOf(container.pos()))));
        return results;
    }

    public static List<MarkedContainer> adjacentMarkedContainers(GolemBase golem) {
        return adjacentMarkedContainers(golem, marker -> colorMatches(golem.level(), marker, golem.getColor()));
    }

    public static List<BlockPos> markersForGolem(GolemBase golem, double maxDistanceSq) {
        if (!(golem.level() instanceof ServerLevel level)) {
            return List.of();
        }
        int radius = (int) Math.ceil(Math.sqrt(maxDistanceSq)) + 1;
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos marker : markers(level, golem.blockPosition(), radius)) {
            if (golem.distanceToSqr(Vec3.atCenterOf(marker)) <= maxDistanceSq && colorMatches(level, marker, golem.getColor())) {
                result.add(marker);
            }
        }
        return result;
    }

    public static @Nullable ResourceHandler<FluidResource> fluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    }

    public static int fluidAmount(ResourceHandler<FluidResource> handler, Fluid fluid) {
        int total = 0;
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (!resource.isEmpty() && resource.getFluid().isSame(fluid)) {
                total += handler.getAmountAsInt(index);
            }
        }
        return total;
    }

    public static int fillFluid(ResourceHandler<FluidResource> handler, Fluid fluid, int amount, boolean simulate) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(FluidResource.of(fluid), amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted;
        }
    }

    public static int drainFluid(ResourceHandler<FluidResource> handler, Fluid fluid, int amount, boolean simulate) {
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(FluidResource.of(fluid), amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return extracted;
        }
    }

    public static @Nullable Fluid firstFluid(ResourceHandler<FluidResource> handler) {
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (!resource.isEmpty() && handler.getAmountAsInt(index) > 0) {
                return resource.getFluid();
            }
        }
        return null;
    }

    public static boolean isSource(Level level, BlockPos pos, Fluid fluid) {
        FluidState state = level.getFluidState(pos);
        return state.isSource() && state.getType().isSame(fluid);
    }

    public static List<BlockPos> adjacentWater(Level level, BlockPos marker) {
        List<BlockPos> result = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos pos = marker.relative(direction);
            if (isSource(level, pos, Fluids.WATER)) {
                result.add(pos);
                continue;
            }
            ResourceHandler<FluidResource> handler = fluidHandler(level, pos, direction.getOpposite());
            if (handler != null && fluidAmount(handler, Fluids.WATER) > 0) {
                result.add(pos);
            }
        }
        return result;
    }

    public static List<BlockPos> adjacentLiquid(Level level, BlockPos marker) {
        List<BlockPos> result = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos pos = marker.relative(direction);
            if (isSource(level, pos, Fluids.WATER) || isSource(level, pos, Fluids.LAVA) || fluidHandler(level, pos, direction.getOpposite()) != null) {
                result.add(pos);
            }
        }
        return result;
    }

    public static List<JarBlockEntity> adjacentJars(Level level, BlockPos marker) {
        List<JarBlockEntity> result = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(marker.relative(direction)) instanceof JarBlockEntity jar) {
                result.add(jar);
            }
        }
        return result;
    }

    public static Direction sideFacing(BlockPos from, BlockPos to) {
        Direction direction = Direction.getApproximateNearest(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
        return direction;
    }

    public static List<AlembicBlockEntity> alembicsAroundCrucible(Level level, BlockPos homeAlembic) {
        List<AlembicBlockEntity> result = new ArrayList<>();
        BlockState state = level.getBlockState(homeAlembic);
        if (!state.hasProperty(AlembicBlock.FACING)) {
            return result;
        }
        BlockPos crucible = homeAlembic.relative(state.getValue(AlembicBlock.FACING));
        for (Direction direction : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            if (level.getBlockEntity(crucible.relative(direction)) instanceof AlembicBlockEntity alembic) {
                result.add(alembic);
            }
        }
        return result;
    }

    public static void chestInteract(Level level, BlockPos pos, boolean open) {
        if (!Config.GOLEM_CHEST_INTERACT.getAsBoolean() || !(level.getBlockEntity(pos) instanceof ChestBlockEntity)) {
            return;
        }
        Block block = level.getBlockState(pos).getBlock();
        level.blockEvent(pos, block, 1, open ? 1 : 0);
        level.playSound(null, pos, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
