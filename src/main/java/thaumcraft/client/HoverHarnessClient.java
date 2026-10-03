package thaumcraft.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.bolt.BoltRenderer;
import thaumcraft.client.fx.bolt.LightningBolt;
import thaumcraft.item.armor.Hover;
import thaumcraft.lib.MiningUtils;
import thaumcraft.network.HoverTogglePayload;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.registry.ModSounds;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class HoverHarnessClient {
    private static int timer;
    private static Boolean hovering;
    private static long nextSound;
    private static final Map<Integer, Long> NEXT_BOLT = new HashMap<>();

    private HoverHarnessClient() {
    }

    private static void toggleHover(LocalPlayer player) {
        hovering = !hovering;
        ClientPacketDistributor.sendToServer(new HoverTogglePayload(hovering));
        player.level().playLocalSound(
            player.getX(), player.getY(), player.getZ(),
            hovering ? ModSounds.HHON.get() : ModSounds.HHOFF.get(),
            SoundSource.PLAYERS, 0.33F, 1.0F, false
        );
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        ItemStack armor = Hover.getHarness(player);
        if (armor.isEmpty()) {
            hovering = null;
            return;
        }
        if (player.getAbilities().flying) {
            return;
        }
        if (hovering == null) {
            hovering = Hover.isHovering(armor);
        }
        if (timer > 0) {
            timer--;
        }
        Input keys = player.input.keyPresses;
        if (keys.jump()) {
            if (timer == 0 || timer >= 5) {
                timer = 7;
            } else {
                timer = 0;
                toggleHover(player);
            }
        }
        if (hovering && Hover.getFuel(armor) > 0) {
            long now = System.currentTimeMillis();
            if (nextSound < now) {
                nextSound = now + 1200L;
                player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.JACOBS.get(), SoundSource.PLAYERS, 0.05F, 1.0F + player.getRandom().nextFloat() * 0.05F, false
                );
            }
            Vec3 motion = player.getDeltaMovement();
            double motionY = motion.y;
            if (keys.jump() && motionY < 0.2F) {
                motionY += 0.15;
                if (motionY > 0.2F) {
                    motionY = 0.2F;
                }
            }
            if (!keys.shift() && motionY <= 0.0) {
                motionY = 0.0;
            }
            player.setDeltaMovement(motion.x, motionY, motion.z);
            int haste = MiningUtils.enchantmentLevel(player.level(), armor, ModEnchantments.HASTE);
            if (!player.onGround() && haste > 0 && (player.zza > 0.0F || player.xxa > 0.0F)) {
                player.move(MoverType.SELF, player.getDeltaMovement().scale(haste * 0.3F));
            }
        } else if (hovering) {
            toggleHover(player);
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            NEXT_BOLT.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (AbstractClientPlayer player : level.players()) {
            ItemStack armor = Hover.getHarness(player);
            if (armor.isEmpty() || !Hover.isHovering(armor)) {
                continue;
            }
            long next = NEXT_BOLT.getOrDefault(player.getId(), 0L);
            if (next >= now) {
                continue;
            }
            NEXT_BOLT.put(player.getId(), now + 50L + player.getRandom().nextInt(50));
            float mod = player.isCrouching() ? 0.075F : 0.0F;
            double y = player.getY() - 0.45F - mod;
            float yaw = player.yBodyRot - 90.0F - player.getRandom().nextInt(180);
            float pitch = -80 + player.getRandom().nextInt(160);
            Vec3 start = new Vec3(player.getX(), y, player.getZ());
            Vec3 look = Vec3.directionFromRotation(pitch, yaw);
            BlockHitResult hit = level.clip(new ClipContext(start, start.add(look.scale(6.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS) {
                continue;
            }
            Vec3 target = hit.getLocation();
            float bodyYaw = (player.yBodyRot + 90.0F) / 180.0F * (float) Math.PI;
            LightningBolt bolt = new LightningBolt(
                player.getX() - Mth.cos(bodyYaw) * 0.5F,
                y,
                player.getZ() - Mth.sin(bodyYaw) * 0.5F,
                target.x, target.y, target.z,
                player.getRandom().nextLong(), 1, 2.0F, 3
            ).setType(6).setWidth(0.015F);
            BoltRenderer.add(bolt.defaultFractal());
        }
    }
}
