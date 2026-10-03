package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import thaumcraft.entity.projectile.Dart;

public class DartRenderer extends EntityRenderer<Dart, ArrowRenderState> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private static final int TINT = 0xFF7F7F99;
    private final ArrowModel model;

    public DartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    public void extractRenderState(Dart entity, ArrowRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.shake = entity.shakeTime - partialTicks;
    }

    @Override
    public void submit(ArrowRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        poseStack.rotateDegrees(Axis.ZP, state.xRot);
        poseStack.scale(0.75F, 1.0F, 1.0F);
        submitNodeCollector.submitModel(
            this.model,
            state,
            poseStack,
            this.model.renderType(TEXTURE),
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            TINT,
            null,
            state.outlineColor
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
