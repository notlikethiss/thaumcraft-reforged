package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.TcModelLayers;

public final class JarRendering {
    private static final ResourceLocation JAR_TEXTURE = Thaumcraft.id("textures/model/jar.png");
    private static final ResourceLocation BRINE_TEXTURE = Thaumcraft.id("textures/model/jarbrine.png");
    private static final ResourceLocation BRAIN_TEXTURE = Thaumcraft.id("textures/model/brain2.png");

    private JarRendering() {
    }

    public record Models(ModelPart jar, ModelPart brine, ModelPart brain) {
        public static Models bake(EntityModelSet models) {
            return new Models(models.bakeLayer(TcModelLayers.JAR), models.bakeLayer(TcModelLayers.JAR_BRINE), models.bakeLayer(TcModelLayers.BRAIN));
        }
    }

    public static void submit(
        Models models,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int light,
        float wobbleX,
        float wobbleZ,
        float fill,
        int color,
        boolean brain,
        float brainYaw,
        float bob
    ) {
        float wobble = Math.max(Math.abs(wobbleX), Math.abs(wobbleZ)) / 150.0F;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.01F + wobble, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(wobbleX));
        poseStack.mulPose(Axis.XP.rotationDegrees(wobbleZ));
        if (brain) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -0.8F + bob, 0.0F);
            poseStack.rotate(Axis.YP, brainYaw);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.scale(0.4F, 0.4F, 0.4F);
            collector.order(0).submitModelPart(models.brain(), poseStack, RenderTypes.entityCutout(BRAIN_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
            poseStack.popPose();
            collector.order(1).submitModelPart(models.brine(), poseStack, RenderTypes.entityTranslucent(BRINE_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        }
        if (fill > 0.0F) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            TextureAtlasSprite sprite = TcRenderUtil.blockSprite(Thaumcraft.id("block/animatedglow"));
            int liquidLight = Math.max(light & 0xFFFF, 0xC8) | (light & 0xFFFF0000);
            int liquidColor = FastColor.ARGB32.opaque(color);
            collector.order(1).submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) ->
                TcRenderUtil.box(pose, buffer, sprite, -0.25F, 0.0625F, -0.25F, 0.25F, 0.0625F + fill, 0.25F, liquidColor, liquidLight)
            );
            poseStack.popPose();
        }
        collector.order(2).submitModelPart(models.jar(), poseStack, RenderTypes.entityTranslucent(JAR_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }
}
