package thaumcraft.lib;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MovementModifiers {
    public static final float VANILLA_AIR_SPEED = 0.02F;
    public static final float VANILLA_SPRINT_AIR_SPEED = 0.026F;
    public static final float SPRINT_MULTIPLIER = 1.3F;

    private MovementModifiers() {
    }

    public static void setAdditive(Player player, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier current = instance.getModifier(id);
        if (amount == 0.0) {
            if (current != null) {
                instance.removeModifier(id);
            }
            return;
        }
        if (current == null || current.amount() != amount) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void addAirSpeed(Player player, float targetSpeed) {
        if (player.onGround() || player.isInLiquid() || player.isFallFlying() || player.isPassenger()) {
            return;
        }
        float vanilla = player.isSprinting() ? VANILLA_SPRINT_AIR_SPEED : VANILLA_AIR_SPEED;
        float target = player.isSprinting() ? targetSpeed * SPRINT_MULTIPLIER : targetSpeed;
        float extra = target - vanilla;
        if (extra > 0.0F) {
            player.moveRelative(extra, new Vec3(player.xxa, 0.0, player.zza));
        }
    }

    public static void multiplyWaterSpeed(Player player, double factor) {
        if (player.isInWater()) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
        }
    }

    public static void reduceFallDistance(Player player, double amount) {
        if (player.fallDistance > amount) {
            player.fallDistance -= amount;
        }
    }
}
