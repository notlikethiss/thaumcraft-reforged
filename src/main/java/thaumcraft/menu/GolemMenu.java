package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entity.golem.ClayGolem;
import thaumcraft.entity.golem.AdvancedClayGolem;
import thaumcraft.entity.golem.DecantingGolem;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemInventory;
import thaumcraft.entity.golem.IronGuardianGolem;
import thaumcraft.entity.golem.MultiColorGolem;
import thaumcraft.registry.ModMenus;

public class GolemMenu extends AbstractContainerMenu {
    private static final int[] TARGET_FLAGS = {
        IronGuardianGolem.HOSTILES, IronGuardianGolem.ANIMALS, IronGuardianGolem.PLAYERS, IronGuardianGolem.CREEPERS,
    };

    private final GolemBase golem;
    private final int golemSlots;

    public GolemMenu(int containerId, Inventory inventory, GolemBase golem) {
        super(ModMenus.GOLEM.get(), containerId);
        this.golem = golem;
        golem.paused = true;
        GolemInventory golemInventory = golem.getInventory();
        boolean smart = golem.getCore() == 2;
        switch (golem.kind()) {
            case WOOD -> {
                if (smart) {
                    addSlot(new Slot(golemInventory, 0, 144, 16));
                    addSlot(new Slot(golemInventory, 1, 144, 44));
                }
            }
            case CLAY, STONE -> {
                addSlot(new Slot(golemInventory, 0, 142, 22));
                if (smart) {
                    addSlot(new Slot(golemInventory, 1, 129, 48));
                    addSlot(new Slot(golemInventory, 2, 155, 48));
                }
            }
            case TALLOW -> addSlot(new Slot(golemInventory, 0, 144, 22));
            case ADVANCED_CLAY, ADVANCED_STONE -> {
                for (int row = 0; row < 2; row++) {
                    for (int column = 0; column < 3; column++) {
                        addSlot(new Slot(golemInventory, row * 3 + column, 97 + column * 28, 17 + row * 37));
                    }
                }
            }
            default -> {
            }
        }
        this.golemSlots = this.slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public static GolemMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        if (inventory.player.level().getEntity(buffer.readVarInt()) instanceof GolemBase golem) {
            return new GolemMenu(containerId, inventory, golem);
        }
        throw new IllegalStateException("Golem entity is missing");
    }

    public GolemBase getGolem() {
        return golem;
    }

    private static int cycleColor(int color, int step) {
        int next = color + step;
        if (next < -1) {
            return 15;
        }
        return next > 15 ? -1 : next;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (golem.kind()) {
            case IRON_GUARDIAN -> {
                if (golem instanceof IronGuardianGolem guardian && id >= 0 && id < TARGET_FLAGS.length) {
                    guardian.setTargetFlag(TARGET_FLAGS[id], !guardian.hasTargetFlag(TARGET_FLAGS[id]));
                }
            }
            case ADVANCED_CLAY, ADVANCED_STONE -> {
                if (golem instanceof MultiColorGolem multi && id >= 0 && id < 12) {
                    int slot = id % 6;
                    multi.setSlotColor(slot, cycleColor(multi.getSlotColor(slot), id < 6 ? -1 : 1));
                }
                if (id == 22 && golem instanceof AdvancedClayGolem clay) {
                    clay.setToggled(!clay.isToggled());
                }
            }
            default -> {
                if (id == 0 || id == 1) {
                    golem.setColor(cycleColor(golem.getColor(), id == 0 ? -1 : 1));
                } else if (id == 2 && golem instanceof ClayGolem clay) {
                    clay.setToggled(!clay.isToggled());
                } else if (golem instanceof DecantingGolem decanting && (id == 2 || id == 3)) {
                    decanting.cycleLiquid(id == 2 ? -1 : 1);
                }
            }
        }
        golem.level().playSound(null, golem.getX(), golem.getY(), golem.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.NEUTRAL, 0.2F, 0.8F);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < golemSlots) {
            if (!moveItemStackTo(stack, golemSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (golemSlots == 0 || !moveItemStackTo(stack, 0, golemSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return golem.isAlive() && player.distanceToSqr(golem) <= 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        golem.paused = false;
    }
}
