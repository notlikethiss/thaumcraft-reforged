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

public class CrystalSpecialRenderer implements NoDataSpecialModelRenderer {
    public static final String CLUSTER = "cluster";
    public static final String CORE = "core";
    public static final String CAPACITOR = "capacitor";

    private final CrystalRendering.Models models;
    private final String kind;
    private final int type;

    public CrystalSpecialRenderer(CrystalRendering.Models models, String kind, int type) {
        this.models = models;
        this.kind = kind;
        this.type = type;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        switch (kind) {
            case CORE -> CrystalRendering.submitCore(models, poseStack, collector, 0L, 0.0F, 0.0F);
            case CAPACITOR -> CrystalRendering.submitCapacitor(models, poseStack, collector, 0, 0, 0, 50);
            default -> CrystalRendering.submitCluster(models, poseStack, collector, type, Direction.UP, type);
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

    public record Unbaked(String kind, int crystal) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("kind", CLUSTER).forGetter(Unbaked::kind),
            Codec.INT.optionalFieldOf("crystal", 0).forGetter(Unbaked::crystal)
        ).apply(instance, Unbaked::new));

        @Override
        public CrystalSpecialRenderer bake(BakingContext context) {
            return new CrystalSpecialRenderer(CrystalRendering.Models.bake(context.entityModelSet()), kind, crystal);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
