package thaumcraft.client.fx;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;

public final class TcParticleLayers {
    public static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");
    public static final ResourceLocation P_LARGE = Thaumcraft.id("textures/misc/p_large.png");
    public static final ResourceLocation P_SMALL = Thaumcraft.id("textures/misc/p_small.png");
    public static final ResourceLocation BURST = Thaumcraft.id("textures/misc/burst.png");
    public static final ResourceLocation WISP = Thaumcraft.id("textures/misc/wisp.png");

    private static final Map<ResourceLocation, SingleQuadParticle.Layer> ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, SingleQuadParticle.Layer> TRANSLUCENT = new HashMap<>();

    private TcParticleLayers() {
    }

    public static SingleQuadParticle.Layer additive(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> new SingleQuadParticle.Layer(true, key, ModRenderPipelines.ADDITIVE_PARTICLE, RenderPipelines.OIT_PARTICLE));
    }

    public static SingleQuadParticle.Layer translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> new SingleQuadParticle.Layer(true, key, ModRenderPipelines.TRANSLUCENT_PARTICLE, RenderPipelines.OIT_PARTICLE));
    }

    public static SingleQuadParticle.Layer byBlend(ResourceLocation texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }
}
