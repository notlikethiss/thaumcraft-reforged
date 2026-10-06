package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import thaumcraft.blockentity.AbstractJarBlockEntity;
import thaumcraft.blockentity.BrainJarBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;

public class JarRenderer<T extends AbstractJarBlockEntity> implements BlockEntityRenderer<T> {
    private final JarRendering.Models models;

    public JarRenderer(BlockEntityRendererProvider.Context context) {
        models = JarRendering.Models.bake(context.getModelSet());
    }

    @Override
    public void render(T jar, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        float fill = 0.0F;
        int color = 0;
        boolean isBrain = false;
        float brainYaw = 0.0F;
        float bob = 0.0F;
        if (jar instanceof JarBlockEntity fillable && fillable.getAmount() > 0) {
            fill = fillable.getAmount() / (float) JarBlockEntity.MAX_AMOUNT * 0.625F;
            color = fillable.getAspect().color;
        }
        if (jar instanceof BrainJarBlockEntity brain) {
            isBrain = true;
            float delta = Mth.wrapDegrees((brain.rota - brain.rotb) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            brainYaw = brain.rotb + delta * partialTick;
            Minecraft minecraft = Minecraft.getInstance();
            int ticks = minecraft.player == null ? 0 : minecraft.player.tickCount;
            bob = Mth.sin(ticks / 14.0F) * 0.03F + 0.03F;
        }
        JarRendering.render(models, poseStack, buffers, light, jar.wobbleX, jar.wobbleZ, fill, color, isBrain, brainYaw, bob);
    }
}
