package thaumcraft.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.menu.MagicWorkbenchMenu;

public abstract class MagicWorkbenchBlockEntity extends TcBlockEntity implements Container {
    public static final int GRID_SIZE = 9;
    public static final int RESULT_SLOT = 9;
    public static final int WAND_SLOT = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(11, ItemStack.EMPTY);
    private final List<MagicWorkbenchMenu> listeners = new ArrayList<>();
    protected int count;

    protected MagicWorkbenchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MagicWorkbenchBlockEntity workbench) {
        workbench.tick((ServerLevel) level);
    }

    protected void tick(ServerLevel level) {
        count++;
        ItemStack wand = items.get(WAND_SLOT);
        if (wand.getItem() instanceof CastingWandItem castingWand
            && castingWand.recharge(wand, level, count, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ())) {
            setChanged();
            notifyListeners();
        }
    }

    public void addListener(MagicWorkbenchMenu menu) {
        listeners.add(menu);
    }

    public void removeListener(MagicWorkbenchMenu menu) {
        listeners.remove(menu);
    }

    protected void notifyListeners() {
        for (MagicWorkbenchMenu menu : List.copyOf(listeners)) {
            menu.slotsChanged(this);
        }
    }

    public ItemStack getGridItem(int x, int y) {
        return items.get(x + y * 3);
    }

    public ItemStack getWand() {
        return items.get(WAND_SLOT);
    }

    public void setItemSoftly(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            onContentsChanged(slot);
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        onContentsChanged(slot);
    }

    private void onContentsChanged(int slot) {
        if (slot == WAND_SLOT) {
            sync();
        } else {
            setChanged();
        }
        if (slot != RESULT_SLOT) {
            notifyListeners();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < items.size(); slot++) {
            if (slot != RESULT_SLOT) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items.get(slot));
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
    }
}
