package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.block.crystal.CrystalColors;
import thaumcraft.blockentity.CrystalCapacitorBlockEntity;
import thaumcraft.client.render.model.TcModelLayers;

public final class CrystalRendering {
    private static final ResourceLocation CRYSTAL_TEXTURE = Thaumcraft.id("textures/model/crystal.png");
    private static final ResourceLocation CAPACITOR_TEXTURE = Thaumcraft.id("textures/model/crystalcapacitor.png");

    private CrystalRendering() {
    }

    public record Models(ModelPart crystal, ModelPart cube) {
        public static Models bake(EntityModelSet models) {
            return new Models(models.bakeLayer(TcModelLayers.CRYSTAL).getChild("crystal"), models.bakeLayer(TcModelLayers.CUBE).getChild("cube"));
        }
    }

    public static int ticks() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0 : minecraft.player.tickCount;
    }

    private static int tint(int rgb) {
        int red = Math.min(255, (rgb >> 16 & 255) * 255 / 220);
        int green = Math.min(255, (rgb >> 8 & 255) * 255 / 220);
        int blue = Math.min(255, (rgb & 255) * 255 / 220);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static void submitPart(ModelPart part, ResourceLocation texture, PoseStack poseStack, SubmitNodeCollector collector, float shade, int color) {
        RenderType renderType = RenderTypes.entityTranslucent(texture);
        collector.submitModelPart(part, poseStack, renderType, (int) (210.0F * shade), OverlayTexture.NO_OVERLAY, null, tint(color));
    }

    private static void scaleCrystal(PoseStack poseStack, Random random, float size) {
        poseStack.scale(
            (0.15F + random.nextFloat() * 0.075F) * size,
            (0.5F + random.nextFloat() * 0.1F) * size,
            (0.15F + random.nextFloat() * 0.05F) * size
        );
    }

    private static void orient(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0.5F, 1.3F, 0.5F);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            }
            case NORTH -> {
                poseStack.translate(0.5F, 0.5F, 1.3F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case SOUTH -> {
                poseStack.translate(0.5F, 0.5F, -0.3F);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            case WEST -> {
                poseStack.translate(1.3F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.translate(-0.3F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }
            default -> poseStack.translate(0.5F, -0.3F, 0.5F);
        }
    }

    private static void clusterCrystal(Models models, PoseStack poseStack, SubmitNodeCollector collector, Direction facing, float angle1, float angle2, Random random,
                                       int color, float size, int ticks) {
        float shade = Mth.sin((ticks + random.nextInt(10)) / (5.0F + random.nextFloat())) * 0.075F + 0.925F;
        poseStack.pushPose();
        orient(poseStack, facing);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle1));
        poseStack.mulPose(Axis.XP.rotationDegrees(angle2));
        scaleCrystal(poseStack, random, size);
        submitPart(models.crystal(), CRYSTAL_TEXTURE, poseStack, collector, shade, color);
        poseStack.popPose();
    }

    public static void submitCluster(Models models, PoseStack poseStack, SubmitNodeCollector collector, int type, Direction facing, long seed) {
        int ticks = ticks();
        int color = type == CrystalColors.MIXED ? CrystalColors.ORE[5] : CrystalColors.ORE[type + 1];
        Random random = new Random(seed);
        clusterCrystal(models, poseStack, collector, facing, (random.nextFloat() - random.nextFloat()) * 5.0F, (random.nextFloat() - random.nextFloat()) * 5.0F,
            random, color, 1.1F, ticks);
        for (int index = 1; index < 5; index++) {
            if (type == CrystalColors.MIXED) {
                color = CrystalColors.ORE[index];
            }
            int angle1 = random.nextInt(45) + 90 * index;
            int angle2 = 15 + random.nextInt(15);
            clusterCrystal(models, poseStack, collector, facing, angle1, angle2, random, color, 0.8F, ticks);
        }
    }

    public static void submitCore(Models models, PoseStack poseStack, SubmitNodeCollector collector, long seed, float speed, float spin) {
        int ticks = ticks();
        Random random = new Random(seed);
        int col = 0;
        for (int index = 0; index < 20; index++) {
            col++;
            if (index % 5 == 0) {
                col++;
            }
            if (col > 5) {
                col = 1;
            }
            float angle1 = spin + 18 * index;
            float angle2 = 30 * (1 + index % 5);
            float shade = Mth.sin((ticks + random.nextInt(10)) / (5.0F + random.nextFloat())) * 0.075F + 0.925F;
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F + speed, 0.5F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin * 2.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(angle1));
            poseStack.mulPose(Axis.XP.rotationDegrees(angle2));
            poseStack.translate(0.0F, -0.1F + speed / 4.0F, 0.0F);
            scaleCrystal(poseStack, random, 0.7F);
            submitPart(models.crystal(), CRYSTAL_TEXTURE, poseStack, collector, shade, CrystalColors.ORE[col]);
            poseStack.popPose();
        }
        submitRays(poseStack, collector, speed, ticks);
    }

    private static void submitRays(PoseStack poseStack, SubmitNodeCollector collector, float speed, int age) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F + speed, 0.5F);
        RayRendering.submitRays(poseStack, collector, 20, age, 0x00FFCCFF);
        poseStack.popPose();
    }

    public static void submitCapacitor(Models models, PoseStack poseStack, SubmitNodeCollector collector, int x, int y, int z, int storedVis) {
        int ticks = ticks();
        float green = 1.0F - (float) storedVis / CrystalCapacitorBlockEntity.MAX_VIS * 0.8F;
        int color = 0xFF0000 | Math.round(green * 255.0F) << 8 | 0xFF;
        Random random = new Random(x + (long) y * z);
        float size = 0.3F;
        for (int xx = 0; xx < 2; xx++) {
            for (int zz = 0; zz < 2; zz++) {
                for (int yy = 0; yy < 2; yy++) {
                    float bob = Mth.sin(ticks / (10 + xx * yy + x % 8.0F)) * 0.02F;
                    float weave = Mth.sin(ticks / (10 + yy * zz + y % 6.0F)) * 0.02F;
                    float wobble = Mth.sin(ticks / (10 + zz * xx + z % 4.0F)) * 0.02F;
                    float shade = Mth.sin((ticks + random.nextInt(10)) / (10.0F + random.nextFloat())) * 0.05F + 0.95F;
                    poseStack.pushPose();
                    poseStack.translate(bob + xx * 0.4F - 0.2F + (1.0F - size) / 2.0F, wobble + yy * 0.4F - 0.2F + (1.0F - size) / 2.0F,
                        weave + zz * 0.4F - 0.2F + (1.0F - size) / 2.0F);
                    poseStack.scale(size, size, size);
                    submitPart(models.cube(), CAPACITOR_TEXTURE, poseStack, collector, shade, color);
                    poseStack.popPose();
                }
            }
        }
    }
}
