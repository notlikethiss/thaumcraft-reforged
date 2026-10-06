package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.item.FilledJarItem;
import thaumcraft.item.JarContents;

public class JarSpecialRenderer implements SpecialModelRenderer<JarContents> {
    private final JarRendering.Models models;
    private final boolean brain;

    public JarSpecialRenderer(JarRendering.Models models, boolean brain) {
        this.models = models;
        this.brain = brain;
    }

    @Override
    public void submit(
        @Nullable JarContents contents,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int lightCoords,
        int overlayCoords,
        boolean hasFoil,
        int outlineColor
    ) {
        float fill = contents == null ? 0.0F : Math.min(contents.amount(), JarBlockEntity.MAX_AMOUNT) / (float) JarBlockEntity.MAX_AMOUNT * 0.625F;
        int color = contents == null ? 0xFFFFFF : contents.aspect().color;
        JarRendering.submit(models, poseStack, collector, lightCoords, 0.0F, 0.0F, fill, color, brain, 0.0F, 0.03F);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (float x : new float[] {0.1875F, 0.8125F}) {
            for (float y : new float[] {0.0F, 0.875F}) {
                for (float z : new float[] {0.1875F, 0.8125F}) {
                    output.accept(new Vector3f(x, y, z));
                }
            }
        }
    }

    @Override
    public @Nullable JarContents extractArgument(ItemStack stack) {
        return FilledJarItem.getContents(stack);
    }

    public record Unbaked(boolean brain) implements SpecialModelRenderer.Unbaked<JarContents> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("brain", false).forGetter(Unbaked::brain)
        ).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<JarContents> bake(SpecialModelRenderer.BakingContext context) {
            return new JarSpecialRenderer(JarRendering.Models.bake(context.entityModelSet()), brain);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
