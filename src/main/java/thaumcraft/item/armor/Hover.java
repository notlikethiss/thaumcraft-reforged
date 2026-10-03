package thaumcraft.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.FilledJarItem;
import thaumcraft.item.JarContents;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModItems;

public final class Hover {
    public static final int EFFICIENCY = 240;

    private Hover() {
    }

    public static ItemStack getHarness(Player player) {
        ItemStack armor = player.getItemBySlot(EquipmentSlot.CHEST);
        return armor.is(ModItems.HOVER_HARNESS.get()) ? armor : ItemStack.EMPTY;
    }

    public static boolean isHovering(ItemStack armor) {
        return armor.getOrDefault(ModDataComponents.HOVER.get(), false);
    }

    public static void setHovering(ItemStack armor, boolean hover) {
        if (isHovering(armor) != hover) {
            armor.set(ModDataComponents.HOVER.get(), hover);
        }
    }

    public static boolean isFuel(ItemStack jar) {
        JarContents contents = fuelContents(jar);
        return contents != null && contents.amount() > 0;
    }

    private static @Nullable JarContents fuelContents(ItemStack jar) {
        if (!(jar.getItem() instanceof FilledJarItem)) {
            return null;
        }
        JarContents contents = FilledJarItem.getContents(jar);
        return contents != null && contents.aspect() == Aspect.POWER ? contents : null;
    }

    public static ItemStack getJar(ItemStack armor) {
        ItemStackTemplate template = armor.get(ModDataComponents.HARNESS_JAR.get());
        return template == null ? ItemStack.EMPTY : template.create();
    }

    public static int getFuel(ItemStack armor) {
        JarContents contents = fuelContents(getJar(armor));
        return contents == null ? 0 : contents.amount();
    }

    public static void setJar(ItemStack armor, ItemStack jar) {
        if (jar.isEmpty()) {
            armor.remove(ModDataComponents.HARNESS_JAR.get());
        } else {
            armor.set(ModDataComponents.HARNESS_JAR.get(), ItemStackTemplate.fromNonEmptyStack(jar));
        }
    }

    public static boolean expendCharge(ItemStack armor) {
        ItemStack jar = getJar(armor);
        if (jar.isEmpty()) {
            return false;
        }
        JarContents contents = fuelContents(jar);
        int fuel = contents == null ? 0 : contents.amount();
        int charge = armor.getOrDefault(ModDataComponents.HOVER_CHARGE.get(), 0);
        if (fuel <= 0) {
            return false;
        }
        if (charge < EFFICIENCY) {
            armor.set(ModDataComponents.HOVER_CHARGE.get(), charge + 1);
            return true;
        }
        armor.set(ModDataComponents.HOVER_CHARGE.get(), 0);
        fuel--;
        jar.set(ModDataComponents.JAR_CONTENTS.get(), new JarContents(Aspect.POWER, fuel));
        setJar(armor, jar);
        return fuel > 0;
    }

    public static void handleServer(Player player, ItemStack armor) {
        boolean hover = isHovering(armor);
        if (hover && expendCharge(armor)) {
            player.fallDistance = 0.0;
        } else {
            if (hover) {
                setHovering(armor, false);
            }
            player.fallDistance *= 0.75F;
        }
    }
}
