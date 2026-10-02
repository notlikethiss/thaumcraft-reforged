package thaumcraft.client.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import thaumcraft.client.fx.ModRenderPipelines;

public final class TcRenderTypes {
    private static final Map<Identifier, RenderType> ADDITIVE = new HashMap<>();
    private static final Map<Identifier, RenderType> TRANSLUCENT = new HashMap<>();

    private TcRenderTypes() {
    }

    public static RenderType additive(Identifier texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_additive",
            RenderSetup.builder(ModRenderPipelines.ADDITIVE_PARTICLE)
                .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                .withTexture("Sampler0", key)
                .useLightmap()
                .createRenderSetup()
        ));
    }

    public static RenderType translucent(Identifier texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_translucent",
            RenderSetup.builder(ModRenderPipelines.TRANSLUCENT_PARTICLE)
                .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                .withTexture("Sampler0", key)
                .useLightmap()
                .createRenderSetup()
        ));
    }

    public static RenderType byBlend(Identifier texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }
}
