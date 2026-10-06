package thaumcraft.item.tool;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.item.ModMaterials;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModSounds;

public class ElementalSwordItem extends SwordItem {
    private static final int USE_DURATION = 72000;
    private static final double PUSH_RANGE = 2.5;
    private static final double SWEEP_RANGE = 1.1;
    private static final double LIFT = 0.08;
    private static final double LIFT_LIMIT = 0.5;
    private static final double LIFT_RESET = 0.2;
    private static boolean sweeping;

    public ElementalSwordItem(Properties properties) {
        super(ModMaterials.ELEMENTAL_TOOL, properties.attributes(SwordItem.createAttributes(ModMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F)));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int ticks = player.getTicksUsingItem();
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.0) {
            player.setDeltaMovement(motion.x, motion.y / 1.2F, motion.z);
            player.fallDistance /= 1.2F;
        }
        motion = player.getDeltaMovement();
        double lift = motion.y + LIFT;
        if (lift > LIFT_LIMIT) {
            lift = LIFT_RESET;
        }
        player.setDeltaMovement(motion.x, lift, motion.z);
        if (level.isClientSide()) {
            double minY = player.getBoundingBox().minY;
            int spiralMinY = player.onGround() ? Mth.floor(minY) : (int) (minY - 2.0);
            Fx.get().smokeSpiral(level, player.getX(), minY + player.getBbHeight() / 2.0F, player.getZ(), 1.5F, level.getRandom().nextInt(360), spiralMinY);
            if (player.onGround()) {
                float angle = level.getRandom().nextFloat() * 360.0F;
                double speedX = -Mth.sin(angle / 180.0F * (float) Math.PI) / 5.0F;
                double speedZ = Mth.cos(angle / 180.0F * (float) Math.PI) / 5.0F;
                level.addParticle(ParticleTypes.SMOKE, player.getX(), minY + 0.1F, player.getZ(), speedX, 0.0, speedZ);
            }
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<Entity> targets = serverLevel.getEntities(player, player.getBoundingBox().inflate(PUSH_RANGE));
        for (Entity target : targets) {
            double deltaX = target.getX() - player.getX();
            double deltaY = target.getY() - player.getY();
            double deltaZ = target.getZ() - player.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) + 0.1;
            target.setDeltaMovement(target.getDeltaMovement().add(deltaX / PUSH_RANGE / distance, deltaY / PUSH_RANGE / distance, deltaZ / PUSH_RANGE / distance));
            target.hurtMarked = true;
        }
        if (ticks == 0 || ticks % 20 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WIND.value(), SoundSource.PLAYERS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        if (ticks % 20 == 0) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (sweeping || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        List<Entity> targets = level.getEntities(player, entity.getBoundingBox().inflate(SWEEP_RANGE));
        int count = 0;
        int ticker = player.attackStrengthTicker;
        sweeping = true;
        try {
            for (Entity target : targets) {
                if (target instanceof LivingEntity && target != entity && !isOwnedBy(target, player)) {
                    player.attack(target);
                    count++;
                }
            }
        } finally {
            sweeping = false;
        }
        player.attackStrengthTicker = ticker;
        if (count > 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.SWING.value(), SoundSource.PLAYERS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        return false;
    }

    private static boolean isOwnedBy(Entity target, Player player) {
        if (target instanceof GolemBase golem) {
            return golem.isOwner(player);
        }
        return target instanceof TamableAnimal tamable && tamable.isOwnedBy(player);
    }
}
