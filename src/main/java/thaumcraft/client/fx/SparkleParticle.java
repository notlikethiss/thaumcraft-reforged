package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SparkleParticle extends TcParticle {
    public boolean leyLineEffect;
    public int multiplier;
    public boolean shrink = true;
    public int particle = 16;
    public boolean tinkle;
    public int blendMode = 1;
    public boolean slowdown = true;
    public int currentColor;
    private final float particleScale;

    public SparkleParticle(ClientLevel level, double x, double y, double z, float size, float red, float green, float blue, int multiplier) {
        super(level, x, y, z);
        if (red == 0.0F) {
            red = 1.0F;
        }
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.particleScale = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F * size;
        this.lifetime = 3 * multiplier;
        this.multiplier = multiplier;
    }

    public SparkleParticle(ClientLevel level, double x, double y, double z, float size, int type, int multiplier) {
        this(level, x, y, z, size, 0.0F, 0.0F, 0.0F, multiplier);
        this.currentColor = type;
        float[] color = TcColors.typeColor(type, level.getRandom());
        if (type >= 0 && type <= 7) {
            this.rCol = color[0];
            this.gCol = color[1];
            this.bCol = color[2];
        }
        if (type == 5) {
            this.blendMode = 771;
        }
    }

    public SparkleParticle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float size, int type, int multiplier) {
        this(level, x, y, z, size, type, multiplier);
        this.xd = (tx - this.x) / this.lifetime;
        this.yd = (ty - this.y) / this.lifetime;
        this.zd = (tz - this.z) / this.lifetime;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float size = 0.1F * particleScale;
        if (shrink) {
            size *= (float) (this.lifetime - this.age + 1) / this.lifetime;
        }
        return size;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        int part = particle + this.age / multiplier;
        float u0 = part % 8 / 8.0F;
        float v0 = part / 8 / 8.0F;
        setUv(u0, u0 + 0.124875F, v0, v0 + 0.124875F);
        return TcParticleLayers.byBlend(TcParticleLayers.PARTICLES, blendMode);
    }

    @Override
    public void tick() {
        savePrevious();
        if (this.age == 0 && tinkle && this.level.getRandom().nextInt(10) == 0) {
            this.level.playLocalSound(
                this.x,
                this.y,
                this.z,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.AMBIENT,
                0.02F,
                0.7F * ((this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.6F + 2.0F),
                false
            );
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        this.yd -= 0.04 * this.gravity;
        if (this.hasPhysics) {
            pushOutOfBlocks(this.x, this.y, this.z);
        }
        moveRaw();
        if (slowdown) {
            this.xd *= 0.908F;
            this.yd *= 0.908F;
            this.zd *= 0.908F;
        }
        if (leyLineEffect) {
            SparkleParticle trail = new SparkleParticle(
                this.level,
                this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                this.yo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                1.0F,
                currentColor,
                3 + this.random.nextInt(3)
            );
            trail.setNoClip(true);
            Minecraft.getInstance().particleEngine.add(trail);
        }
    }

    private void pushOutOfBlocks(double px, double py, double pz) {
        int bx = Mth.floor(px);
        int by = Mth.floor(py);
        int bz = Mth.floor(pz);
        BlockPos pos = new BlockPos(bx, by, bz);
        if (this.level.isEmptyBlock(pos)) {
            return;
        }
        double fx = px - bx;
        double fy = py - by;
        double fz = pz - bz;
        boolean west = !isNormalCube(bx - 1, by, bz);
        boolean east = !isNormalCube(bx + 1, by, bz);
        boolean down = !isNormalCube(bx, by - 1, bz);
        boolean up = !isNormalCube(bx, by + 1, bz);
        boolean north = !isNormalCube(bx, by, bz - 1);
        boolean south = !isNormalCube(bx, by, bz + 1);
        int side = -1;
        double best = 9999.0;
        if (west && fx < best) {
            best = fx;
            side = 0;
        }
        if (east && 1.0 - fx < best) {
            best = 1.0 - fx;
            side = 1;
        }
        if (down && fy < best) {
            best = fy;
            side = 2;
        }
        if (up && 1.0 - fy < best) {
            best = 1.0 - fy;
            side = 3;
        }
        if (north && fz < best) {
            best = fz;
            side = 4;
        }
        if (south && 1.0 - fz < best) {
            side = 5;
        }
        float push = this.random.nextFloat() * 0.05F + 0.025F;
        float drift = (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
        switch (side) {
            case 0 -> setMotion(-push, drift, drift);
            case 1 -> setMotion(push, drift, drift);
            case 2 -> setMotion(drift, -push, drift);
            case 3 -> setMotion(drift, push, drift);
            case 4 -> setMotion(drift, drift, -push);
            case 5 -> setMotion(drift, drift, push);
            default -> {
            }
        }
    }

    private void setMotion(double x, double y, double z) {
        this.xd = x;
        this.yd = y;
        this.zd = z;
    }

    private boolean isNormalCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return this.level.getBlockState(pos).isRedstoneConductor(this.level, pos);
    }
}
