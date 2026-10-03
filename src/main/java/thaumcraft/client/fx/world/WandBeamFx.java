package thaumcraft.client.fx.world;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WandBeamFx extends BoreBeamFx {
    private final Player player;

    public WandBeamFx(Level level, Player player, double targetX, double targetY, double targetZ, int color, int type, boolean reverse, float endMod) {
        super(level, player.getX(), player.getY(), player.getZ(), targetX, targetY, targetZ, color, type, reverse, endMod);
        this.player = player;
        moveToHand();
        prevX = x;
        prevY = y;
        prevZ = z;
        updateAngles();
        prevYaw = yaw;
        prevPitch = pitch;
    }

    private void moveToHand() {
        boolean firstPerson = player == Minecraft.getInstance().getCameraEntity();
        double handX = player.getX();
        double handY = firstPerson ? player.getEyeY() : player.getY() + player.getBbHeight() / 2.0F + 0.25;
        double handZ = player.getZ();
        float yaw = player.getYRot();
        handX -= Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        handY -= 0.01F;
        handZ -= Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        Vec3 look = player.getViewVector(1.0F);
        x = handX + look.x * 0.24;
        y = handY + look.y * 0.24;
        z = handZ + look.z * 0.24;
    }

    @Override
    public void tick() {
        super.tick();
        moveToHand();
        updateAngles();
        if (!player.isAlive() || player.isRemoved()) {
            maxAge = 0;
        }
    }
}
