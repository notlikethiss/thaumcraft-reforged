package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.GolemAccessoriesModel;
import thaumcraft.client.render.model.GolemModel;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.item.FilledJarItem;

public class GolemRenderer extends MobRenderer<GolemBase, GolemModel> {
    private static final ResourceLocation DAMAGE = Thaumcraft.id("textures/model/golem_damage.png");
    private static final ResourceLocation DECORATION = Thaumcraft.id("textures/model/golem_decoration.png");
    private static final int[] MARKER_COLORS = {
        0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
        0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B,
    };

    public GolemRenderer(EntityRendererProvider.Context context, boolean advanced) {
        super(context, new GolemModel(context.bakeLayer(advanced ? TcModelLayers.GOLEM_ADVANCED : TcModelLayers.GOLEM)), 0.25F);
        if (!advanced) {
            this.addLayer(new MarkerLayer(this, context.bakeLayer(TcModelLayers.GOLEM_MARKER)));
        }
        this.addLayer(new AccessoriesLayer(this, new GolemAccessoriesModel(context.bakeLayer(TcModelLayers.GOLEM_ACCESSORIES))));
        this.addLayer(new DamageLayer(this, new GolemModel(context.bakeLayer(advanced ? TcModelLayers.GOLEM_ADVANCED : TcModelLayers.GOLEM))));
        this.addLayer(new CarriedLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(GolemBase entity) {
        return entity.kind().texture();
    }

    @Override
    protected void setupRotations(GolemBase entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        if (entity.walkAnimation.speed(partialTick) >= 0.01F) {
            float period = 13.0F;
            float position = entity.walkAnimation.position(partialTick) + 6.0F;
            float sway = (Math.abs(position % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(6.5F * sway));
        }
    }

    private static class MarkerLayer extends RenderLayer<GolemBase, GolemModel> {
        private final ModelPart marker;

        MarkerLayer(GolemRenderer renderer, ModelPart marker) {
            super(renderer);
            this.marker = marker;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GolemBase entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            int color = entity.getColor();
            if (color < 0 || color >= MARKER_COLORS.length || entity.isInvisible()) {
                return;
            }
            poseStack.pushPose();
            poseStack.scale(0.4F, 0.4F, 0.4F);
            this.marker.render(
                poseStack,
                bufferSource.getBuffer(RenderType.entityCutout(entity.kind().texture())),
                light,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                FastColor.ARGB32.opaque(MARKER_COLORS[color])
            );
            poseStack.popPose();
        }
    }

    private static class AccessoriesLayer extends RenderLayer<GolemBase, GolemModel> {
        private final GolemAccessoriesModel model;

        AccessoriesLayer(GolemRenderer renderer, GolemAccessoriesModel model) {
            super(renderer);
            this.model = model;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GolemBase entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.getDecoration().isEmpty() || entity.isInvisible()) {
                return;
            }
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(DECORATION)),
                light,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                -1
            );
        }
    }

    private static class DamageLayer extends RenderLayer<GolemBase, GolemModel> {
        private final GolemModel model;

        DamageLayer(GolemRenderer renderer, GolemModel model) {
            super(renderer);
            this.model = model;
            this.model.tinted = false;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GolemBase entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            float healthPercent = entity.getHealth() / entity.getMaxHealth();
            if (healthPercent >= 1.0F || entity.isInvisible()) {
                return;
            }
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(DAMAGE)),
                light,
                OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.colorFromFloat(1.0F - Math.max(0.0F, healthPercent), 1.0F, 1.0F, 1.0F)
            );
        }
    }

    private static class CarriedLayer extends RenderLayer<GolemBase, GolemModel> {
        CarriedLayer(GolemRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GolemBase entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack carried = entity.getDisplayCarried();
            if (carried.isEmpty() || entity.deathTime != 0) {
                return;
            }
            boolean carriedJar = carried.getItem() instanceof FilledJarItem;
            boolean carriedBlock = carried.getItem() instanceof BlockItem && !carriedJar;
            poseStack.pushPose();
            poseStack.scale(0.4F, 0.4F, 0.4F);
            if (carriedBlock) {
                poseStack.translate(0.0F, 2.5F, -1.25F);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            } else {
                poseStack.translate(-0.5F, 2.5F, carriedJar ? -1.8F : -1.25F);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZN.rotationDegrees(335.0F));
                poseStack.mulPose(Axis.YN.rotationDegrees(50.0F));
                poseStack.translate(0.0F, 0.0F, -0.5F);
            }
            poseStack.translate(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                entity,
                carried,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                bufferSource,
                entity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                entity.getId()
            );
            poseStack.popPose();
        }
    }
}
