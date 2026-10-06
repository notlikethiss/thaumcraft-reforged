package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.blockentity.AbstractJarBlockEntity;
import thaumcraft.blockentity.BrainJarBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;

public class JarRenderer<T extends AbstractJarBlockEntity> implements BlockEntityRenderer<T, JarRenderer.State> {
    public static class State extends BlockEntityRenderState {
        float wobbleX;
        float wobbleZ;
        float fill;
        int color;
        boolean brain;
        float brainYaw;
        float bob;
    }

    private final JarRendering.Models models;

    public JarRenderer(BlockEntityRendererProvider.Context context) {
        models = JarRendering.Models.bake(context.entityModelSet());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T jar, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(jar, state, partialTicks, cameraPosition, breakProgress);
        state.wobbleX = jar.wobbleX;
        state.wobbleZ = jar.wobbleZ;
        state.fill = 0.0F;
        state.brain = false;
        if (jar instanceof JarBlockEntity fillable && fillable.getAmount() > 0) {
            state.fill = fillable.getAmount() / (float) JarBlockEntity.MAX_AMOUNT * 0.625F;
            state.color = fillable.getAspect().color;
        }
        if (jar instanceof BrainJarBlockEntity brain) {
            state.brain = true;
            float delta = Mth.wrapDegrees((brain.rota - brain.rotb) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            state.brainYaw = brain.rotb + delta * partialTicks;
            Minecraft minecraft = Minecraft.getInstance();
            int ticks = minecraft.player == null ? 0 : minecraft.player.tickCount;
            state.bob = Mth.sin(ticks / 14.0F) * 0.03F + 0.03F;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        JarRendering.submit(models, poseStack, collector, state.lightCoords, state.wobbleX, state.wobbleZ, state.fill, state.color, state.brain, state.brainYaw, state.bob);
    }
}
