package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import thaumcraft.client.render.model.HungryChestModel;
import thaumcraft.client.render.model.TcModelLayers;

public class HungryChestSpecialRenderer implements NoDataSpecialModelRenderer {
    private final HungryChestModel model;

    public HungryChestSpecialRenderer(HungryChestModel model) {
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        HungryChestRendering.submit(model, poseStack, collector, lightCoords, Direction.NORTH, 0.0F);
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
        public HungryChestSpecialRenderer bake(BakingContext context) {
            return new HungryChestSpecialRenderer(new HungryChestModel(context.entityModelSet().bakeLayer(TcModelLayers.HUNGRY_CHEST)));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
