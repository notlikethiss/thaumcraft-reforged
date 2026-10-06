package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.model.FireBatModel;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.entity.monster.FireBat;

public class FireBatRenderer extends MobRenderer<FireBat, FireBatRenderState, FireBatModel> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/firebat.png");

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(TcModelLayers.FIRE_BAT)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(FireBatRenderState state) {
        return TEXTURE;
    }

    @Override
    public FireBatRenderState createRenderState() {
        return new FireBatRenderState();
    }

    @Override
    public void extractRenderState(FireBat entity, FireBatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hanging = entity.isHanging();
    }

    @Override
    protected int getBlockLightLevel(FireBat entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(FireBat entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected void scale(FireBatRenderState state, PoseStack poseStack) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupRotations(FireBatRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        if (state.hanging) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
        } else {
            poseStack.translate(0.0F, Mth.cos(state.ageInTicks * 0.3F) * 0.1F, 0.0F);
        }
        super.setupRotations(state, poseStack, bodyRot, entityScale);
    }
}
