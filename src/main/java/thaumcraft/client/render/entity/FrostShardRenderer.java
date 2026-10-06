package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.entity.projectile.FrostShard;

public class FrostShardRenderer extends EntityRenderer<FrostShard, FrostShardRenderState> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/frostshard.png");
    private final ModelPart crystal;

    public FrostShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.crystal = context.bakeLayer(TcModelLayers.CRYSTAL).getChild("crystal");
    }

    @Override
    public FrostShardRenderState createRenderState() {
        return new FrostShardRenderState();
    }

    @Override
    public void extractRenderState(FrostShard entity, FrostShardRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.ticksInGround = entity.getTicksInGround();
        state.seed = entity.getId();
    }

    @Override
    public void submit(FrostShardRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Random random = new Random(state.seed);
        float alpha = Math.min(1.0F, (200.0F - state.ticksInGround) / 150.0F);
        int color = FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        RenderType renderType = RenderTypes.entityTranslucent(TEXTURE);
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.1F - state.ticksInGround / 200.0F * 0.2F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        for (int index = 0; index < 2; index++) {
            if (index == 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            }
            poseStack.pushPose();
            poseStack.scale(0.1F + random.nextFloat() * 0.1F, 0.1F + random.nextFloat() * 0.1F, 0.1F + random.nextFloat() * 0.1F);
            submitNodeCollector.submitModelPart(this.crystal, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null, color);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
