package thaumcraft.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface FxProxy {
    default int particleCount(int base) {
        return 0;
    }

    default void blockSparkle(Level level, int x, int y, int z, int color, int count) {
    }

    default void infusedStoneSparkle(Level level, int x, int y, int z, int type) {
    }

    default void sparkle(float x, float y, float z, float size, int color, float gravity) {
    }

    default void sparkle(float x, float y, float z, int color) {
    }

    default void sparkle(Level level, double x, double y, double z, double tx, double ty, double tz, float size, int color, int multiplier, float gravity) {
    }

    default void wisp(Level level, double x, double y, double z, float size, float red, float green, float blue, boolean tinkle) {
    }

    default void wispFX(Level level, double x, double y, double z, float size, float red, float green, float blue) {
    }

    default void wispFX2(Level level, double x, double y, double z, float size, int type, boolean shrink, float gravity) {
    }

    default void wispFX3(Level level, double x, double y, double z, double tx, double ty, double tz, float size, int type, boolean shrink, float gravity) {
    }

    default void burst(Level level, double x, double y, double z, float size) {
    }

    default void crucibleBubble(Level level, float x, float y, float z, float red, float green, float blue) {
    }

    default void crucibleFroth(Level level, float x, float y, float z) {
    }

    default void crucibleFrothDown(Level level, float x, float y, float z) {
    }

    default void crucibleBoil(Level level, int x, int y, int z, float fluidHeight, int[] colors, int strength) {
    }

    default void sourceStream(Level level, double x, double y, double z, double tx, double ty, double tz, int color) {
    }

    default void alembicSpill(Level level, int x, int y, int z, int color) {
    }

    default void bubbles(Level level, double x, double y, double z, int color, double speed, int count) {
    }

    default void bolt(Level level, Entity source, Entity target) {
    }

    default void wandLightning(Level level, Entity shooter, double x, double y, double z, boolean hasBlock, double blockX, double blockY, double blockZ, boolean hasEntity) {
    }

    default void wandFire(Level level, Entity shooter, int range) {
    }

    default void nodeBolt(Level level, float x, float y, float z, Entity target) {
    }

    default void auraTransfer(Level level, float x, float y, float z, float tx, float ty, float tz) {
    }

    default void furnaceLava(Level level, int x, int y, int z, int facingX, int facingZ) {
    }

    default void beam(Level level, double x, double y, double z, double tx, double ty, double tz, int type, int color, boolean reverse, float endMod, int age) {
    }

    default void crystalSparkle(Level level, float x, float y, float z, int color) {
    }

    default void crystalCoreBeam(Level level, double x, double y, double z, int nodeKey) {
    }

    default @Nullable Object boreBeam(Level level, double x, double y, double z, double tx, double ty, double tz, int type, int color, boolean reverse,
                                      float endMod, @Nullable Object previous, int impact) {
        return null;
    }

    default void wandBeam(Level level, Player player, double tx, double ty, double tz, int type, int color, boolean reverse, float endMod, int impact) {
    }

    default void boreDigFx(Level level, BlockPos pos, BlockPos target, BlockState state) {
    }

    default void blockRunes(Level level, int x, int y, int z, float red, float green, float blue, int duration) {
    }
}
