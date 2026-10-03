package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.client.fx.bolt.BoltRenderer;
import thaumcraft.client.fx.bolt.LightningBolt;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.fx.world.BeamFx;
import thaumcraft.client.fx.world.RuneFx;
import thaumcraft.client.fx.world.WorldFxRenderer;
import thaumcraft.fx.FxProxy;

public final class ClientFx implements FxProxy {
    private static ClientLevel level() {
        return Minecraft.getInstance().level;
    }

    private static void add(Particle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }

    @Override
    public int particleCount(int base) {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        return switch (status) {
            case MINIMAL -> 0;
            case DECREASED -> base;
            case ALL -> base * 2;
        };
    }

    @Override
    public void beam(Level level, double x, double y, double z, double tx, double ty, double tz, int type, int color, boolean reverse, float endMod, int age) {
        WorldFxRenderer.add(new BeamFx(level, x, y, z, tx, ty, tz, color, type, reverse, endMod, age));
    }

    @Override
    public void crystalSparkle(Level level, float x, float y, float z, int color) {
        if (level instanceof ClientLevel clientLevel) {
            SparkleParticle fx = new SparkleParticle(clientLevel, x, y, z, 1.0F, color, 3);
            fx.setNoClip(true);
            add(fx);
        }
    }

    @Override
    public void crystalCoreBeam(Level level, double x, double y, double z, int nodeKey) {
        AuraClientData.ClientNode node = AuraClientData.NODES.get(nodeKey);
        if (node == null) {
            return;
        }
        float[] position = AuraClientData.RENDER_POSITIONS.get(nodeKey);
        double tx = position != null ? position[0] : node.x();
        double ty = position != null ? position[1] : node.y();
        double tz = position != null ? position[2] : node.z();
        float size = node.level() / 100.0F;
        beam(level, x, y, z, tx, ty, tz, 0, 0xFFFFFF, true, size, 20);
        beam(level, x, y, z, tx, ty, tz, 1, 0xFFFFFF, true, size / 2.0F, 20);
    }

    @Override
    public void blockRunes(Level level, int x, int y, int z, float red, float green, float blue, int duration) {
        WorldFxRenderer.add(new RuneFx(x + 0.5, y + 0.5, z + 0.5, red, green, blue, duration, 0.03F, level.getRandom()));
    }

    @Override
    public void furnaceLava(Level level, int x, int y, int z, int facingX, int facingZ) {
        RandomSource random = level.getRandom();
        double px = x + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3 + facingX;
        double py = y + 0.3;
        double pz = z + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3 + facingZ;
        Particle particle = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LAVA, px, py, pz, 0.0, 0.0, 0.0);
        if (particle != null) {
            float qx = facingX == 0 ? (random.nextFloat() - random.nextFloat()) * 0.5F : facingX * random.nextFloat();
            float qz = facingZ == 0 ? (random.nextFloat() - random.nextFloat()) * 0.5F : facingZ * random.nextFloat();
            particle.setParticleSpeed(0.15 * qx, 0.2 * random.nextFloat(), 0.15 * qz);
        }
    }

    @Override
    public void blockSparkle(Level level, int x, int y, int z, int color, int count) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }
        for (int i = 0; i < particleCount(count); i++) {
            SparkleParticle fx = new SparkleParticle(
                clientLevel,
                x + level.getRandom().nextFloat(),
                y + level.getRandom().nextFloat(),
                z + level.getRandom().nextFloat(),
                1.75F,
                color == -1 ? level.getRandom().nextInt(5) : color,
                3 + level.getRandom().nextInt(3)
            );
            fx.setGravity(0.2F);
            add(fx);
        }
    }

    @Override
    public void infusedStoneSparkle(Level level, int x, int y, int z, int type) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }
        int color = switch (type) {
            case 1 -> 1;
            case 2 -> 4;
            case 3 -> 2;
            case 4 -> 3;
            default -> 0;
        };
        for (int i = 0; i < particleCount(3); i++) {
            SparkleParticle fx = new SparkleParticle(
                clientLevel,
                x + level.getRandom().nextFloat(),
                y + level.getRandom().nextFloat(),
                z + level.getRandom().nextFloat(),
                1.75F,
                color,
                3 + level.getRandom().nextInt(3)
            );
            fx.setGravity(0.1F);
            add(fx);
        }
    }

    @Override
    public void sparkle(float x, float y, float z, float size, int color, float gravity) {
        ClientLevel level = level();
        if (level != null && level.getRandom().nextInt(6) < particleCount(2)) {
            SparkleParticle fx = new SparkleParticle(level, x, y, z, size, color, 6);
            fx.setNoClip(true);
            fx.setGravity(gravity);
            add(fx);
        }
    }

    @Override
    public void sparkle(float x, float y, float z, int color) {
        ClientLevel level = level();
        if (level != null && level.getRandom().nextInt(6) < particleCount(2)) {
            SparkleParticle fx = new SparkleParticle(level, x, y, z, 1.5F, color, 6);
            fx.setNoClip(true);
            add(fx);
        }
    }

    @Override
    public void sparkle(Level level, double x, double y, double z, double tx, double ty, double tz, float size, int color, int multiplier, float gravity) {
        if (level instanceof ClientLevel clientLevel) {
            SparkleParticle fx = new SparkleParticle(clientLevel, x, y, z, tx, ty, tz, size, color, multiplier);
            fx.setGravity(gravity);
            fx.setNoClip(true);
            add(fx);
        }
    }

    @Override
    public void wisp(Level level, double x, double y, double z, float size, float red, float green, float blue, boolean tinkle) {
        if (level instanceof ClientLevel clientLevel) {
            WispParticle fx = new WispParticle(clientLevel, x, y, z, size, red, green, blue);
            fx.tinkle = tinkle;
            add(fx);
        }
    }

    @Override
    public void wispFX(Level level, double x, double y, double z, float size, float red, float green, float blue) {
        if (level instanceof ClientLevel clientLevel) {
            WispParticle fx = new WispParticle(clientLevel, x, y, z, size, red, green, blue);
            fx.setGravity(0.02F);
            add(fx);
        }
    }

    @Override
    public void wispFX2(Level level, double x, double y, double z, float size, int type, boolean shrink, float gravity) {
        if (level instanceof ClientLevel clientLevel) {
            WispParticle fx = new WispParticle(clientLevel, x, y, z, size, type);
            fx.setGravity(gravity);
            fx.shrink = shrink;
            add(fx);
        }
    }

    @Override
    public void wispFX3(Level level, double x, double y, double z, double tx, double ty, double tz, float size, int type, boolean shrink, float gravity) {
        if (level instanceof ClientLevel clientLevel) {
            WispParticle fx = new WispParticle(clientLevel, x, y, z, tx, ty, tz, size, type);
            fx.setGravity(gravity);
            fx.shrink = shrink;
            add(fx);
        }
    }

    @Override
    public void burst(Level level, double x, double y, double z, float size) {
        if (level instanceof ClientLevel clientLevel) {
            add(new BurstParticle(clientLevel, x, y, z, size));
        }
    }

    @Override
    public void auraTransfer(Level level, float x, float y, float z, float tx, float ty, float tz) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }
        float dx = x - tx;
        float dy = y - ty;
        float dz = z - tz;
        int distance = (int) Mth.sqrt(dx * dx + dy * dy + dz * dz);
        SparkleParticle fx = new SparkleParticle(clientLevel, x, y, z, tx, ty, tz, 2.5F, 0, Math.max(1, distance / 2));
        fx.slowdown = false;
        fx.setNoClip(true);
        fx.leyLineEffect = true;
        fx.shrink = false;
        add(fx);
    }

    @Override
    public void crucibleBubble(Level level, float x, float y, float z, float red, float green, float blue) {
        if (level instanceof ClientLevel clientLevel) {
            BubbleParticle fx = new BubbleParticle(clientLevel, x, y, z, 0.0, 0.0, 0.0, 1);
            fx.setColor(red, green, blue);
            add(fx);
        }
    }

    @Override
    public void crucibleFroth(Level level, float x, float y, float z) {
        if (level instanceof ClientLevel clientLevel) {
            BubbleParticle fx = new BubbleParticle(clientLevel, x, y, z, 0.0, 0.0, 0.0, -4);
            fx.setColor(0.5F, 0.5F, 0.7F);
            fx.setFroth();
            add(fx);
        }
    }

    @Override
    public void crucibleFrothDown(Level level, float x, float y, float z) {
        if (level instanceof ClientLevel clientLevel) {
            BubbleParticle fx = new BubbleParticle(clientLevel, x, y, z, 0.0, 0.0, 0.0, -4);
            fx.setColor(0.5F, 0.5F, 0.7F);
            fx.setFroth2();
            add(fx);
        }
    }

    @Override
    public void crucibleBoil(Level level, int x, int y, int z, float fluidHeight, int[] colors, int strength) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }
        for (int i = 0; i < particleCount(1); i++) {
            BubbleParticle fx = new BubbleParticle(
                clientLevel,
                x + 0.2F + level.getRandom().nextFloat() * 0.6F,
                y + 0.1F + fluidHeight,
                z + 0.2F + level.getRandom().nextFloat() * 0.6F,
                0.0,
                0.0,
                0.0,
                3
            );
            if (colors.length == 0) {
                fx.setColor(1.0F, 1.0F, 1.0F);
            } else {
                int color = colors[level.getRandom().nextInt(colors.length)];
                fx.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
            }
            fx.bubbleSpeed = 0.003 * strength;
            add(fx);
        }
    }

    @Override
    public void sourceStream(Level level, double x, double y, double z, double tx, double ty, double tz, int color) {
        if (level instanceof ClientLevel clientLevel) {
            WispParticle fx = new WispParticle(
                clientLevel,
                x,
                y,
                z,
                tx,
                ty,
                tz,
                0.15F,
                (color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F
            );
            fx.setGravity(0.0F);
            add(fx);
        }
    }

    @Override
    public void alembicSpill(Level level, int x, int y, int z, int color) {
        bubbles(level, x, y + 0.8, z, color, 0.01, 2);
    }

    @Override
    public void bubbles(Level level, double x, double y, double z, int color, double speed, int count) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return;
        }
        for (int i = 0; i < particleCount(count); i++) {
            BubbleParticle fx = new BubbleParticle(
                clientLevel,
                x + 0.2F + level.getRandom().nextFloat() * 0.6F,
                y + level.getRandom().nextFloat() * 0.1F,
                z + 0.2F + level.getRandom().nextFloat() * 0.6F,
                0.0,
                0.0,
                0.0,
                3
            );
            fx.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
            fx.bubbleSpeed = speed;
            add(fx);
        }
    }

    @Override
    public void nodeBolt(Level level, float x, float y, float z, Entity target) {
        double targetY = target instanceof Player ? target.getEyeY() : target.getY();
        bolt(level, x, y, z, target.getX(), targetY, target.getZ(), 10, 2.0F, 5, 3);
    }

    public static void bolt(Level level, double x1, double y1, double z1, double x2, double y2, double z2, int duration, float multiplier, int speed, int type) {
        BoltRenderer.add(new LightningBolt(x1, y1, z1, x2, y2, z2, level.getRandom().nextLong(), duration, multiplier, speed).setType(type).defaultFractal());
    }
}
