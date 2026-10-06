package thaumcraft.client.fx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;

public final class TcParticleLayers {
    public static final ResourceLocation PARTICLES = Thaumcraft.id("textures/misc/particles.png");
    public static final ResourceLocation P_LARGE = Thaumcraft.id("textures/misc/p_large.png");
    public static final ResourceLocation P_SMALL = Thaumcraft.id("textures/misc/p_small.png");
    public static final ResourceLocation BURST = Thaumcraft.id("textures/misc/burst.png");
    public static final ResourceLocation WISP = Thaumcraft.id("textures/misc/wisp.png");

    private static final Map<ResourceLocation, ParticleRenderType> ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, ParticleRenderType> TRANSLUCENT = new HashMap<>();

    private TcParticleLayers() {
    }

    public static ParticleRenderType additive(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> new Layer(key, true));
    }

    public static ParticleRenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> new Layer(key, false));
    }

    public static ParticleRenderType byBlend(ResourceLocation texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }

    private record Layer(ResourceLocation texture, boolean additive) implements ParticleRenderType {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            if (additive) {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.defaultBlendFunc();
            }
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, texture);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "thaumcraft_" + (additive ? "additive" : "translucent") + "_" + texture;
        }
    }
}
