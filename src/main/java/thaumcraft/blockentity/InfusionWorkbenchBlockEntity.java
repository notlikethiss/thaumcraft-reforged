package thaumcraft.blockentity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.fx.Fx;
import thaumcraft.menu.InfusionWorkbenchMenu;
import thaumcraft.registry.ModBlockEntities;

public class InfusionWorkbenchBlockEntity extends MagicWorkbenchBlockEntity implements MenuProvider {
    private static final Codec<Map<Aspect, BlockPos>> SOURCES_CODEC = Codec.unboundedMap(Aspect.CODEC, BlockPos.CODEC);
    private static final int RANGE = 12;
    private static final int HEIGHT_RANGE = 5;

    private AspectList foundTags = new AspectList();
    private Map<Aspect, BlockPos> sources = new HashMap<>();
    private AspectList partTags = new AspectList();

    public InfusionWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFUSION_WORKBENCH.get(), pos, state);
    }

    public AspectList getFoundTags() {
        return foundTags;
    }

    @Override
    protected void tick(ServerLevel level) {
        super.tick(level);
        if (count % 20 == 0 && isCrafting()) {
            findSources(true);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, InfusionWorkbenchBlockEntity workbench) {
        RandomSource random = level.getRandom();
        for (Aspect aspect : workbench.foundTags.getAspects()) {
            BlockPos source = workbench.sources.get(aspect);
            int required = workbench.partTags.getAmount(aspect);
            if (source == null || random.nextInt(10) != 0 || required <= 0 || workbench.foundTags.getAmount(aspect) < required) {
                continue;
            }
            Fx.get().sourceStream(
                level,
                source.getX() + 0.1 + random.nextFloat() * 0.8,
                source.getY() + 0.5 + random.nextFloat() * 0.5,
                source.getZ() + 0.1 + random.nextFloat() * 0.8,
                pos.getX() + 1 + random.nextFloat() - random.nextFloat(),
                pos.getY() + 1,
                pos.getZ() + 1 + random.nextFloat() - random.nextFloat(),
                aspect.color
            );
        }
    }

    private boolean isCrafting() {
        if (getWand().isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < GRID_SIZE; slot++) {
            if (!getItem(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void findSources(boolean transmit) {
        if (level == null) {
            return;
        }
        int oldSize = foundTags.size();
        int oldTotal = foundTags.visSize();
        AspectList found = new AspectList();
        Map<Aspect, BlockPos> linked = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -HEIGHT_RANGE; dy <= HEIGHT_RANGE; dy++) {
                for (int dz = -RANGE; dz <= RANGE; dz++) {
                    cursor.setWithOffset(worldPosition, dx, dy, dz);
                    if (level.isOutsideBuildHeight(cursor) || !level.isLoaded(cursor)) {
                        continue;
                    }
                    BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (blockEntity == this || !(blockEntity instanceof AspectSource source)) {
                        continue;
                    }
                    for (Aspect aspect : source.getSourceTags().getAspects()) {
                        int amount = source.sourceContains(aspect);
                        if (amount > found.getAmount(aspect)) {
                            found.merge(aspect, amount);
                            linked.put(aspect, cursor.immutable());
                        }
                    }
                }
            }
        }
        foundTags = found;
        sources = linked;
        if (found.size() != oldSize || found.visSize() != oldTotal) {
            notifyListeners();
            if (transmit) {
                sync();
            }
        }
    }

    public boolean doSourcesMatch(AspectList required) {
        findSources(false);
        AspectList parts = new AspectList();
        boolean matches = true;
        for (Aspect aspect : required.getAspects()) {
            if (foundTags.getAmount(aspect) >= required.getAmount(aspect)) {
                parts.merge(aspect, foundTags.getAmount(aspect));
            } else {
                matches = false;
            }
        }
        if (!parts.equals(partTags)) {
            partTags = parts;
            sync();
        }
        return matches;
    }

    public void clearParts() {
        if (!partTags.isEmpty()) {
            partTags = new AspectList();
            sync();
        }
    }

    public @Nullable AspectSource getLinkedSource(Aspect aspect) {
        BlockPos pos = sources.get(aspect);
        if (pos == null || level == null) {
            return null;
        }
        return level.getBlockEntity(pos) instanceof AspectSource source ? source : null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.arcane_stone");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new InfusionWorkbenchMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        foundTags = input.read("Found", AspectList.CODEC).orElseGet(AspectList::new);
        sources = new HashMap<>(input.read("Sources", SOURCES_CODEC).orElseGet(Map::of));
        partTags = input.read("Parts", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Found", AspectList.CODEC, foundTags);
        output.store("Sources", SOURCES_CODEC, sources);
        output.store("Parts", AspectList.CODEC, partTags);
    }
}
