package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.entity.projectile.FrostShard;

public class FrostShardRenderer extends EntityRenderer<FrostShard> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/frostshard.png");
    private final ModelPart crystal;

    public FrostShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.crystal = context.bakeLayer(TcModelLayers.CRYSTAL).getChild("crystal");
    }

    @Override
    public ResourceLocation getTextureLocation(FrostShard entity) {
        return TEXTURE;
    }

    @Override
    public void render(FrostShard entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        Random random = new Random(entity.getId());
        int ticksInGround = entity.getTicksInGround();
        float alpha = Math.min(1.0F, (200.0F - ticksInGround) / 150.0F);
        int color = FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        RenderType renderType = RenderType.entityTranslucent(TEXTURE);
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.1F - ticksInGround / 200.0F * 0.2F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        for (int index = 0; index < 2; index++) {
            if (index == 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            }
            poseStack.pushPose();
            poseStack.scale(0.1F + random.nextFloat() * 0.1F, 0.1F + random.nextFloat() * 0.1F, 0.1F + random.nextFloat() * 0.1F);
            this.crystal.render(poseStack, bufferSource.getBuffer(renderType), light, OverlayTexture.NO_OVERLAY, color);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, light);
    }
}
