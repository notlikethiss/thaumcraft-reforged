package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.client.render.RayRendering;
import thaumcraft.entity.SpecialItem;

public class SpecialItemRenderer extends EntityRenderer<SpecialItem> {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();
    private final boolean rays;

    public SpecialItemRenderer(EntityRendererProvider.Context context, boolean rays) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.rays = rays;
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public ResourceLocation getTextureLocation(SpecialItem entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(SpecialItem entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }
        float age = entity.age + partialTick;
        float bob = Mth.sin(age / 10.0F + entity.hoverStart) * 0.1F + 0.1F;
        if (this.rays) {
            poseStack.pushPose();
            poseStack.translate(0.0F, bob + 0.275F, 0.0F);
            RayRendering.submitRays(poseStack, bufferSource, 10, age, 0x00FF00FF);
            poseStack.popPose();
        }
        this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(stack));
        BakedModel model = this.itemRenderer.getModel(stack, entity.level(), null, entity.getId());
        poseStack.pushPose();
        poseStack.translate(0.0F, bob - modelMinY(model) + 0.0625F, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(age / 20.0F + entity.hoverStart));
        ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, poseStack, bufferSource, light, stack, this.random, entity.level());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, light);
    }

    private static float modelMinY(BakedModel model) {
        ItemTransform transform = model.getTransforms().getTransform(ItemDisplayContext.GROUND);
        Matrix4f matrix = new Matrix4f()
            .translate(transform.translation)
            .rotate(new Quaternionf().rotationXYZ(transform.rotation.x * Mth.DEG_TO_RAD, transform.rotation.y * Mth.DEG_TO_RAD, transform.rotation.z * Mth.DEG_TO_RAD))
            .scale(transform.scale)
            .translate(-0.5F, -0.5F, -0.5F);
        float minY = Float.MAX_VALUE;
        Vector3f point = new Vector3f();
        RandomSource quadRandom = RandomSource.create(42L);
        List<Direction> directions = new ArrayList<>(List.of(Direction.values()));
        directions.add(null);
        for (Direction direction : directions) {
            for (BakedQuad quad : model.getQuads(null, direction, quadRandom)) {
                int[] vertices = quad.getVertices();
                int stride = vertices.length / 4;
                for (int index = 0; index < 4; index++) {
                    point.set(Float.intBitsToFloat(vertices[index * stride]), Float.intBitsToFloat(vertices[index * stride + 1]), Float.intBitsToFloat(vertices[index * stride + 2]));
                    matrix.transformPosition(point);
                    minY = Math.min(minY, point.y);
                }
            }
        }
        return minY == Float.MAX_VALUE ? -0.25F * transform.scale.y() : minY;
    }
}
