package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import thaumcraft.client.render.RayRendering;
import thaumcraft.entity.SpecialItem;

public class SpecialItemRenderer extends EntityRenderer<SpecialItem, SpecialItemRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();
    private final boolean rays;

    public SpecialItemRenderer(EntityRendererProvider.Context context, boolean rays) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.rays = rays;
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public SpecialItemRenderState createRenderState() {
        return new SpecialItemRenderState();
    }

    @Override
    public void extractRenderState(SpecialItem entity, SpecialItemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hoverStart = entity.hoverStart;
        state.age = entity.age + partialTicks;
        state.extractItemGroupRenderState(entity, entity.getItem(), this.itemModelResolver);
    }

    @Override
    public void submit(SpecialItemRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.item.isEmpty()) {
            return;
        }
        float bob = Mth.sin(state.age / 10.0F + state.hoverStart) * 0.1F + 0.1F;
        if (this.rays) {
            poseStack.pushPose();
            poseStack.translate(0.0F, bob + 0.275F, 0.0F);
            RayRendering.submitRays(poseStack, submitNodeCollector, 10, state.age, 0x00FF00FF);
            poseStack.popPose();
        }
        poseStack.pushPose();
        AABB boundingBox = state.item.getModelBoundingBox();
        poseStack.translate(0.0F, bob - (float) boundingBox.minY + 0.0625F, 0.0F);
        poseStack.rotate(Axis.YP.rotation(ItemEntity.getSpin(state.age, state.hoverStart)));
        ItemEntityRenderer.submitMultipleFromCount(poseStack, submitNodeCollector, state.lightCoords, state, this.random, boundingBox);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
