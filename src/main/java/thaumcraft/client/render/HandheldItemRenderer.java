package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.gui.AspectRenderer;
import thaumcraft.client.gui.TcFonts;
import thaumcraft.client.research.ResearchTexts;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class HandheldItemRenderer {
    private static final RenderType PARCHMENT = RenderType.text(Thaumcraft.id("textures/misc/parchment.png"));
    private static final RenderType TAGS = RenderType.text(AspectRenderer.TAGS_BW);
    private static final RenderType ITEMS = RenderType.text(TextureAtlas.LOCATION_BLOCKS);
    private static final ResourceLocation RING = Thaumcraft.id("item/thaumometerring");
    private static final ResourceLocation CORE = Thaumcraft.id("item/thaumometercore");
    private static final int TEXT_COLOR = 0xFF685E4A;

    private static float angleH;
    private static float angleV;

    private HandheldItemRenderer() {
    }

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ItemStack stack = event.getItemStack();
        if (event.getHand() != InteractionHand.MAIN_HAND || player == null || !player.getOffhandItem().isEmpty()) {
            return;
        }
        boolean thaumometer = stack.is(ModItems.THAUMOMETER.get());
        if (!thaumometer && !(stack.getItem() instanceof ResearchNotesItem)) {
            return;
        }
        event.setCanceled(true);
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float attack = event.getSwingProgress();
        poseStack.pushPose();
        float sqrtAttack = Mth.sqrt(attack);
        float ySwing = -0.2F * Mth.sin(attack * Mth.PI);
        float zSwing = -0.4F * Mth.sin(sqrtAttack * Mth.PI);
        poseStack.translate(0.0F, -ySwing / 2.0F, zSwing);
        float tilt = mapTilt(event.getInterpolatedPitch());
        poseStack.translate(0.0F, 0.04F + event.getEquipProgress() * -1.2F + tilt * -0.5F, -0.72F);
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt * -85.0F));
        if (!player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            renderMapHand(minecraft, poseStack, buffers, light, HumanoidArm.RIGHT, player);
            renderMapHand(minecraft, poseStack, buffers, light, HumanoidArm.LEFT, player);
            poseStack.popPose();
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(sqrtAttack * Mth.PI) * 20.0F));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        if (thaumometer) {
            renderThaumometer(minecraft, player, poseStack, buffers, light, tilt);
        } else {
            renderNotes(minecraft, stack, poseStack, buffers, light);
        }
        poseStack.popPose();
    }

    private static float mapTilt(float xRot) {
        float tilt = Mth.clamp(1.0F - xRot / 45.0F + 0.1F, 0.0F, 1.0F);
        return -Mth.cos(tilt * Mth.PI) * 0.5F + 0.5F;
    }

    private static void renderMapHand(Minecraft minecraft, PoseStack poseStack, MultiBufferSource buffers, int light, HumanoidArm arm, AbstractClientPlayer player) {
        poseStack.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * -41.0F));
        poseStack.translate(invert * 0.3F, -1.1F, 0.45F);
        PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(poseStack, buffers, light, player);
        } else {
            renderer.renderLeftHand(poseStack, buffers, light, player);
        }
        poseStack.popPose();
    }

    private static void quad(PoseStack poseStack, MultiBufferSource buffers, RenderType type, float x0, float y0, float x1, float y1, float z,
                             float u0, float v0, float u1, float v1, int color, int light) {
        VertexConsumer buffer = buffers.getBuffer(type);
        PoseStack.Pose pose = poseStack.last();
        vertex(buffer, pose, x0, y1, z, u0, v1, color, light);
        vertex(buffer, pose, x1, y1, z, u1, v1, color, light);
        vertex(buffer, pose, x1, y0, z, u1, v0, color, light);
        vertex(buffer, pose, x0, y0, z, u0, v0, color, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setLight(light);
    }

    private static void renderThaumometer(Minecraft minecraft, LocalPlayer player, PoseStack poseStack, MultiBufferSource buffers, int light, float tilt) {
        TextureAtlasSprite ring = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RING);
        TextureAtlasSprite core = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(CORE);
        quad(poseStack, buffers, ITEMS, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, ring.getU0(), ring.getV0(), ring.getU1(), ring.getV1(), -1, light);
        AuraClientData.ClientNode closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (AuraClientData.ClientNode node : AuraClientData.NODES.values()) {
            if (node.dimension() != player.level().dimension()) {
                continue;
            }
            double dx = node.x() - player.getX();
            double dy = node.y() - player.getY();
            double dz = node.z() - player.getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = node;
            }
        }
        float bob = Mth.sin(player.tickCount / 5.0F) * 0.015F + 0.015F;
        if (closest != null) {
            double dx = closest.x() - player.getX();
            double dy = closest.y() - player.getY() + bob;
            double dz = closest.z() - player.getZ();
            float horizontal = (float) Math.sqrt(dx * dx + dz * dz);
            float targetH = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - player.getYRot() + 90.0F;
            float targetV = (float) (Math.atan2(horizontal, dy) * 180.0 / Math.PI) - player.getXRot();
            if (targetH < 0.0F) {
                targetH += 360.0F;
            }
            if (targetH > 360.0F) {
                targetH -= 360.0F;
            }
            angleH += (targetH - angleH) / 33.0F;
            targetV += tilt * -85.0F;
            if (targetV < -180.0F) {
                targetV += 360.0F;
            }
            if (targetV > 180.0F) {
                targetV -= 360.0F;
            }
            angleV += (targetV - angleV) / 33.0F;
        } else {
            if (angleH > 0.0F) {
                angleH -= angleH / 33.0F;
            }
            if (angleV > 0.0F) {
                angleV -= angleV / 33.0F;
            } else if (angleV < 0.0F) {
                angleV += angleV / 33.0F;
            }
        }
        poseStack.pushPose();
        poseStack.translate(0.1F, 0.1F, 0.0F);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0.5F, 0.5F + bob, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angleH));
        poseStack.mulPose(Axis.XP.rotationDegrees(angleV));
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        quad(poseStack, buffers, ITEMS, 0.0F, 0.0F, 1.0F, 1.0F, -0.01F, core.getU0(), core.getV0(), core.getU1(), core.getV1(), -1, light);
        poseStack.popPose();
    }

    private static void renderNotes(Minecraft minecraft, ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Font font = minecraft.font;
        poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        quad(poseStack, buffers, PARCHMENT, -7.0F, -7.0F, 135.0F, 135.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, -1, light);
        ResearchNoteData data = ResearchManager.getData(stack);
        if (data.key == null) {
            return;
        }
        float total = data.getTotalProgress();
        ResearchTexts.Entry entry = ResearchTexts.get(data.key);
        poseStack.pushPose();
        poseStack.translate(10.0F, 10.0F, -0.04F);
        Component name = TcFonts.text(entry == null ? data.key : entry.name(), total < 0.2F);
        int width = font.width(name);
        if (width > 110) {
            float scale = 110.0F / width;
            poseStack.scale(scale, scale, scale);
        }
        text(poseStack, buffers, name.getVisualOrderText(), 0.0F, 0.0F, light);
        poseStack.popPose();
        int index = 0;
        for (int a = 0; a < data.tags.length; a++) {
            Aspect tag = data.tags[a];
            if (data.progress[a] <= 0 && total < 0.5F) {
                continue;
            }
            float tagProgress = data.getTagProgress(tag);
            Aspect icon = tagProgress >= 0.3F ? tag : Aspect.UNKNOWN;
            float x = 15.0F + index % 4 * 30;
            float y = 28.0F + index / 4 * 13;
            float u0 = icon.getId() % 8 / 8.0F;
            float v0 = icon.getId() / 8 / 8.0F;
            int color = tagProgress >= 0.3F ? FastColor.ARGB32.color(150, tag.color) : -1;
            poseStack.pushPose();
            poseStack.translate(x, y, -0.02F);
            poseStack.scale(6.0F, 6.0F, 3.0F);
            quad(poseStack, buffers, TAGS, -1.0F, -1.0F, 1.0F, 1.0F, 0.0F, u0, v0, u0 + 0.125F, v0 + 0.125F, color, light);
            poseStack.popPose();
            if (tagProgress >= 0.333332F) {
                int completion = (int) ((float) data.progress[a] / ResearchList.getResearchAmount(data.key, tag) * 100.0F);
                poseStack.pushPose();
                poseStack.translate(x, y, -0.02F);
                poseStack.scale(0.56F, 0.56F, 1.0F);
                text(poseStack, buffers, Component.literal(completion + "%").getVisualOrderText(), 13.0F, -3.0F, light);
                poseStack.popPose();
            }
            index++;
        }
        if (entry != null) {
            poseStack.pushPose();
            poseStack.translate(11.0F, 25 + (1 + (index - 1) / 4) * 13, -0.02F);
            poseStack.scale(0.63F, 0.63F, 1.0F);
            List<FormattedCharSequence> lines = font.split(TcFonts.text(entry.longText(), total < 0.5F), 180);
            for (int i = 0; i < lines.size(); i++) {
                text(poseStack, buffers, lines.get(i), 0.0F, i * font.lineHeight, light);
            }
            poseStack.popPose();
        }
    }

    private static void text(PoseStack poseStack, MultiBufferSource buffers, FormattedCharSequence text, float x, float y, int light) {
        Minecraft.getInstance().font.drawInBatch(text, x, y, TEXT_COLOR, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
    }
}
