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

public class FireBatRenderer extends MobRenderer<FireBat, FireBatModel> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/model/firebat.png");

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(TcModelLayers.FIRE_BAT)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(FireBat entity) {
        return TEXTURE;
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
    protected void scale(FireBat entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupRotations(FireBat entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        if (entity.isHanging()) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
        } else {
            poseStack.translate(0.0F, Mth.cos(bob * 0.3F) * 0.1F, 0.0F);
        }
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
    }
}
