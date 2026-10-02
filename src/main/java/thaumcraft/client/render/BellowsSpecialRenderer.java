package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BellowsSpecialRenderer implements NoDataSpecialModelRenderer {
    private final BellowsRendering.Models models;

    public BellowsSpecialRenderer(BellowsRendering.Models models) {
        this.models = models;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        Minecraft minecraft = Minecraft.getInstance();
        int ticks = minecraft.player == null ? 0 : minecraft.player.tickCount;
        float inflation = Mth.sin(ticks / 8.0F) * 0.3F + 0.7F;
        BellowsRendering.submit(models, poseStack, collector, lightCoords, Direction.NORTH, inflation);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (float x : new float[] {0.0F, 1.0F}) {
            for (float y : new float[] {0.0F, 1.0F}) {
                for (float z : new float[] {0.0F, 1.0F}) {
                    output.accept(new Vector3f(x, y, z));
                }
            }
        }
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public BellowsSpecialRenderer bake(BakingContext context) {
            return new BellowsSpecialRenderer(BellowsRendering.Models.bake(context.entityModelSet()));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
