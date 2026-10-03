package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.GolemAccessoriesModel;
import thaumcraft.client.render.model.GolemModel;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.item.FilledJarItem;

public class GolemRenderer extends MobRenderer<GolemBase, GolemRenderState, GolemModel> {
    private static final Identifier DAMAGE = Thaumcraft.id("textures/model/golem_damage.png");
    private static final Identifier DECORATION = Thaumcraft.id("textures/model/golem_decoration.png");
    private static final int[] MARKER_COLORS = {
        0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
        0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B,
    };

    private final ItemModelResolver itemModelResolver;

    public GolemRenderer(EntityRendererProvider.Context context, boolean advanced) {
        super(context, new GolemModel(context.bakeLayer(advanced ? TcModelLayers.GOLEM_ADVANCED : TcModelLayers.GOLEM)), 0.25F);
        this.itemModelResolver = context.getItemModelResolver();
        if (!advanced) {
            this.addLayer(new MarkerLayer(this, context.bakeLayer(TcModelLayers.GOLEM_MARKER)));
        }
        this.addLayer(new AccessoriesLayer(this, new GolemAccessoriesModel(context.bakeLayer(TcModelLayers.GOLEM_ACCESSORIES))));
        this.addLayer(new DamageLayer(this, new GolemModel(context.bakeLayer(advanced ? TcModelLayers.GOLEM_ADVANCED : TcModelLayers.GOLEM))));
        this.addLayer(new CarriedLayer(this));
    }

    @Override
    public Identifier getTextureLocation(GolemRenderState state) {
        return state.texture;
    }

    @Override
    public GolemRenderState createRenderState() {
        return new GolemRenderState();
    }

    @Override
    public void extractRenderState(GolemBase entity, GolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.texture = entity.kind().texture();
        state.core = entity.getCore();
        state.color = entity.getColor();
        state.decoration = entity.getDecoration();
        state.healthPercent = entity.getHealth() / entity.getMaxHealth();
        int action = entity.getActionTimer();
        state.actionActive = action > 0;
        state.actionTimer = action - partialTicks;
        state.leftArmActive = entity.leftArm > 0;
        state.leftArm = entity.leftArm - partialTicks;
        state.healing = entity.healing;
        ItemStack carried = entity.getDisplayCarried();
        state.carrying = !carried.isEmpty() && entity.deathTime == 0;
        state.carriedJar = carried.getItem() instanceof FilledJarItem;
        state.carriedBlock = carried.getItem() instanceof BlockItem && !state.carriedJar;
        if (state.carrying) {
            this.itemModelResolver.updateForLiving(state.carried, carried, ItemDisplayContext.NONE, entity);
        } else {
            state.carried.clear();
        }
    }

    @Override
    protected int getModelTint(GolemRenderState state) {
        if (state.healing > 0) {
            float h1 = state.healing / 10.0F;
            float h2 = state.healing / 5.0F;
            return ARGB.colorFromFloat(1.0F, Math.min(1.0F, 0.5F + h1), Math.min(1.0F, 0.9F + h2), Math.min(1.0F, 0.5F + h1));
        }
        if (state.hasRedOverlay) {
            return ARGB.colorFromFloat(1.0F, 0.9F, 0.5F, 0.5F);
        }
        return -1;
    }

    @Override
    protected void setupRotations(GolemRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.walkAnimationSpeed >= 0.01F) {
            float period = 13.0F;
            float position = state.walkAnimationPos + 6.0F;
            float sway = (Math.abs(position % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
            poseStack.rotateDegrees(Axis.ZP, 6.5F * sway);
        }
    }

    private static class MarkerLayer extends RenderLayer<GolemRenderState, GolemModel> {
        private final ModelPart marker;

        MarkerLayer(GolemRenderer renderer, ModelPart marker) {
            super(renderer);
            this.marker = marker;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, GolemRenderState state, float yRot, float xRot) {
            if (state.color < 0 || state.color >= MARKER_COLORS.length || state.isInvisible) {
                return;
            }
            collector.submitModelPart(
                this.marker,
                poseStack,
                RenderTypes.entityCutout(state.texture),
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                null,
                ARGB.opaque(MARKER_COLORS[state.color])
            );
        }
    }

    private static class AccessoriesLayer extends RenderLayer<GolemRenderState, GolemModel> {
        private final GolemAccessoriesModel model;

        AccessoriesLayer(GolemRenderer renderer, GolemAccessoriesModel model) {
            super(renderer);
            this.model = model;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, GolemRenderState state, float yRot, float xRot) {
            if (state.decoration.isEmpty() || state.isInvisible) {
                return;
            }
            this.model.setupAnim(state);
            collector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(DECORATION),
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                -1,
                null,
                state.outlineColor
            );
        }
    }

    private static class DamageLayer extends RenderLayer<GolemRenderState, GolemModel> {
        private final GolemModel model;

        DamageLayer(GolemRenderer renderer, GolemModel model) {
            super(renderer);
            this.model = model;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, GolemRenderState state, float yRot, float xRot) {
            if (state.healthPercent >= 1.0F || state.isInvisible) {
                return;
            }
            this.model.setupAnim(state);
            collector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(DAMAGE),
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                ARGB.colorFromFloat(1.0F - Math.max(0.0F, state.healthPercent), 1.0F, 1.0F, 1.0F),
                null,
                state.outlineColor
            );
        }
    }

    private static class CarriedLayer extends RenderLayer<GolemRenderState, GolemModel> {
        CarriedLayer(GolemRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, GolemRenderState state, float yRot, float xRot) {
            if (!state.carrying || state.carried.isEmpty()) {
                return;
            }
            poseStack.pushPose();
            poseStack.scale(0.4F, 0.4F, 0.4F);
            if (state.carriedBlock) {
                poseStack.translate(0.0F, 2.5F, -1.25F);
                poseStack.rotateDegrees(Axis.XP, 180.0F);
                poseStack.rotateDegrees(Axis.XP, 20.0F);
                poseStack.rotateDegrees(Axis.YP, 45.0F);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            } else {
                poseStack.translate(-0.5F, 2.5F, state.carriedJar ? -1.8F : -1.25F);
                poseStack.rotateDegrees(Axis.XP, 180.0F);
                poseStack.rotateDegrees(Axis.YP, 180.0F);
                poseStack.rotateDegrees(Axis.ZN, 335.0F);
                poseStack.rotateDegrees(Axis.YN, 50.0F);
                poseStack.translate(0.0F, 0.0F, -0.5F);
            }
            state.carried.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
