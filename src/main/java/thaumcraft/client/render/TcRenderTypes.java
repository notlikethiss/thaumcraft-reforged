package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;

public final class TcRenderTypes extends RenderType {
    private static final ShaderStateShard PARTICLE_SHADER = new ShaderStateShard(GameRenderer::getParticleShader);
    private static final Map<ResourceLocation, RenderType> ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT = new HashMap<>();
    private static final RenderType TUNNEL = RenderType.create(
        "thaumcraft_tunnel",
        DefaultVertexFormat.POSITION,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        false,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_END_PORTAL_SHADER)
            .setTextureState(
                MultiTextureStateShard.builder()
                    .add(Thaumcraft.id("textures/misc/tunnel.png"), false, false)
                    .add(Thaumcraft.id("textures/misc/particlefield.png"), false, false)
                    .build()
            )
            .createCompositeState(false)
    );

    private TcRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static RenderType particleLike(String name, ResourceLocation texture, TransparencyStateShard transparency) {
        return RenderType.create(
            name,
            DefaultVertexFormat.PARTICLE,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(transparency)
                .setLightmapState(LIGHTMAP)
                .setCullState(NO_CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
        );
    }

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> particleLike("thaumcraft_additive", key, LIGHTNING_TRANSPARENCY));
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> particleLike("thaumcraft_translucent", key, TRANSLUCENT_TRANSPARENCY));
    }

    public static RenderType tunnel() {
        return TUNNEL;
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }
}
