package thaumcraft.blockentity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.registry.ModBlockEntities;

public class HungryChestBlockEntity extends BaseContainerBlockEntity {
    public static final int EVENT_OPEN_COUNT = 1;
    public static final int EVENT_CHOMP = 2;
    private static final int SIZE = 27;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private float openness;
    private float prevOpenness;
    private boolean shouldBeOpen;
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            level.blockEvent(pos, state.getBlock(), EVENT_OPEN_COUNT, current);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == HungryChestBlockEntity.this;
        }
    };

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.prevOpenness = chest.openness;
        if (chest.shouldBeOpen) {
            chest.openness = Math.min(chest.openness + 0.1F, 1.0F);
        } else {
            chest.openness = Math.max(chest.openness - 0.1F, 0.0F);
        }
    }

    public float getOpenness(float partialTicks) {
        return Mth.lerp(partialTicks, prevOpenness, openness);
    }

    @Override
    public boolean triggerEvent(int b0, int b1) {
        if (b0 == EVENT_OPEN_COUNT) {
            shouldBeOpen = b1 > 0;
            return true;
        }
        if (b0 == EVENT_CHOMP) {
            openness = Math.max(openness, b1 / 10.0F);
            return true;
        }
        return super.triggerEvent(b0, b1);
    }

    public boolean eat(ItemStack stack) {
        boolean eaten = false;
        if (stack.isStackable()) {
            for (int slot = 0; slot < SIZE && !stack.isEmpty(); slot++) {
                ItemStack existing = items.get(slot);
                if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
                    continue;
                }
                int max = stack.getMaxStackSize();
                int total = existing.getCount() + stack.getCount();
                if (total <= max) {
                    existing.setCount(total);
                    stack.setCount(0);
                    eaten = true;
                } else if (existing.getCount() < max) {
                    stack.shrink(max - existing.getCount());
                    existing.setCount(max);
                    eaten = true;
                }
            }
        }
        if (!stack.isEmpty()) {
            for (int slot = 0; slot < SIZE; slot++) {
                if (items.get(slot).isEmpty()) {
                    items.set(slot, stack.copy());
                    stack.setCount(0);
                    eaten = true;
                    break;
                }
            }
        }
        if (eaten) {
            setChanged();
        }
        return eaten;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!remove && !user.getLivingEntity().isSpectator() && level != null) {
            openersCounter.incrementOpeners(user.getLivingEntity(), level, getBlockPos(), getBlockState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!remove && !user.getLivingEntity().isSpectator() && level != null) {
            openersCounter.decrementOpeners(user.getLivingEntity(), level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return level == null ? List.of() : openersCounter.getEntitiesWithContainerOpen(level, getBlockPos());
    }

    public void recheckOpen() {
        if (!remove && level != null) {
            openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumcraft.hungry_chest");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
}
