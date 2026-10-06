package thaumcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import thaumcraft.entity.SpecialItem;

public class SpecialItemRenderer extends EntityRenderer<SpecialItem> {
    private static final float HALF_SQRT_3 = 0.866F;
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
            renderRays(poseStack, bufferSource, 10, age, 0x00FF00FF);
            poseStack.popPose();
        }
        this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(stack));
        BakedModel model = this.itemRenderer.getModel(stack, entity.level(), null, entity.getId());
        float groundScale = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        poseStack.pushPose();
        poseStack.translate(0.0F, bob + 0.25F * groundScale, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(age / 20.0F + entity.hoverStart));
        ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, poseStack, bufferSource, light, stack, this.random, entity.level());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, light);
    }

    private static void renderRays(PoseStack poseStack, MultiBufferSource bufferSource, int count, float age, int outerColor) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.dragonRays());
        float time = age / 500.0F;
        float grow = 30.0F / (Math.min(age, 10.0F) / 10.0F);
        Random random = new Random(245L);
        Matrix4f matrix = new Matrix4f(poseStack.last().pose());
        for (int index = 0; index < count; index++) {
            matrix.rotateX((float) Math.toRadians(random.nextFloat() * 360.0F));
            matrix.rotateY((float) Math.toRadians(random.nextFloat() * 360.0F));
            matrix.rotateZ((float) Math.toRadians(random.nextFloat() * 360.0F));
            matrix.rotateX((float) Math.toRadians(random.nextFloat() * 360.0F));
            matrix.rotateY((float) Math.toRadians(random.nextFloat() * 360.0F));
            matrix.rotateZ((float) Math.toRadians(random.nextFloat() * 360.0F + time * 360.0F));
            float length = (random.nextFloat() * 20.0F + 5.0F) / grow;
            float width = (random.nextFloat() * 2.0F + 1.0F) / grow;
            float[][] corners = {{-HALF_SQRT_3 * width, length, -0.5F * width}, {HALF_SQRT_3 * width, length, -0.5F * width}, {0.0F, length, width}};
            for (int side = 0; side < 3; side++) {
                float[] first = corners[side];
                float[] second = corners[(side + 1) % 3];
                buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(0xFFFFFFFF);
                buffer.addVertex(matrix, first[0], first[1], first[2]).setColor(outerColor);
                buffer.addVertex(matrix, second[0], second[1], second[2]).setColor(outerColor);
            }
        }
    }
}
