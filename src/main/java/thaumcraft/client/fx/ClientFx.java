package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
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
    public void nodeBolt(Level level, float x, float y, float z, Entity target) {
    }
}
