package thaumcraft.client.fx.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import thaumcraft.client.fx.TcParticleLayers;
import thaumcraft.client.render.TcRenderTypes;

public class BoreBeamFx extends BeamFx {
    private double prevTargetX;
    private double prevTargetY;
    private double prevTargetZ;
    private int impact;

    public BoreBeamFx(Level level, double x, double y, double z, double targetX, double targetY, double targetZ, int color, int type, boolean reverse, float endMod) {
        super(level, x, y, z, targetX, targetY, targetZ, color, type, reverse, endMod, 8);
        pulse = true;
        dayTimeSlide = true;
        prevTargetX = targetX;
        prevTargetY = targetY;
        prevTargetZ = targetZ;
    }

    public void update(double targetX, double targetY, double targetZ, float endMod, int impact) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.endMod = endMod;
        this.impact = impact;
        while (maxAge - age < 4) {
            maxAge++;
        }
    }

    @Override
    public void tick() {
        prevTargetX = targetX;
        prevTargetY = targetY;
        prevTargetZ = targetZ;
        super.tick();
        if (impact > 0) {
            impact--;
        }
    }

    @Override
    public int passes() {
        return impact > 0 ? 2 : 1;
    }

    @Override
    public RenderType renderType(int pass) {
        return pass == 0 ? renderType() : TcRenderTypes.additive(TcParticleLayers.PARTICLES);
    }

    @Override
    public void render(int pass, PoseStack.Pose pose, VertexConsumer buffer, float partialTick, Vec3 camera) {
        if (pass == 0) {
            render(pose, buffer, partialTick, camera);
            return;
        }
        int part = age % 16;
        float u0 = part % 8 / 8.0F;
        float u1 = u0 + 0.125F;
        float v0 = 0.375F + part / 8 / 8.0F;
        float v1 = v0 + 0.125F;
        float size = endMod / 2.0F / (6 - impact);
        float cx = (float) (Mth.lerp(partialTick, prevTargetX, targetX) - camera.x);
        float cy = (float) (Mth.lerp(partialTick, prevTargetY, targetY) - camera.y);
        float cz = (float) (Mth.lerp(partialTick, prevTargetZ, targetZ) - camera.z);
        Camera view = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f left = view.getLeftVector();
        Vector3f up = view.getUpVector();
        float lx = left.x() * size;
        float ly = left.y() * size;
        float lz = left.z() * size;
        float ux = up.x() * size;
        float uy = up.y() * size;
        float uz = up.z() * size;
        buffer.addVertex(pose, cx - lx - ux, cy - ly - uy, cz - lz - uz).setUv(u1, v1).setColor(red, green, blue, 0.66F).setLight(FULL_BRIGHT);
        buffer.addVertex(pose, cx - lx + ux, cy - ly + uy, cz - lz + uz).setUv(u1, v0).setColor(red, green, blue, 0.66F).setLight(FULL_BRIGHT);
        buffer.addVertex(pose, cx + lx + ux, cy + ly + uy, cz + lz + uz).setUv(u0, v0).setColor(red, green, blue, 0.66F).setLight(FULL_BRIGHT);
        buffer.addVertex(pose, cx + lx - ux, cy + ly - uy, cz + lz - uz).setUv(u0, v1).setColor(red, green, blue, 0.66F).setLight(FULL_BRIGHT);
    }
}
