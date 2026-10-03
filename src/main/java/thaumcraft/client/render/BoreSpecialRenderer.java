package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BoreSpecialRenderer implements NoDataSpecialModelRenderer {
    private final BoreRendering.Models models;
    private final boolean base;

    public BoreSpecialRenderer(BoreRendering.Models models, boolean base) {
        this.models = models;
        this.base = base;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (base) {
            BoreRendering.submitBase(models, poseStack, collector, lightCoords, Direction.EAST);
        } else {
            BoreRendering.submitBore(models, poseStack, collector, lightCoords, new BoreRendering.BoreState(0.0F, 0.0F, false, false, 0.0F, CrystalRendering.ticks() % 45));
        }
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

    public record Unbaked(boolean base) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("base", false).forGetter(Unbaked::base)
        ).apply(instance, Unbaked::new));

        @Override
        public BoreSpecialRenderer bake(BakingContext context) {
            return new BoreSpecialRenderer(BoreRendering.Models.bake(context.entityModelSet()), base);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
