package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.client.gui.AspectRenderer;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class GogglesTagRenderer {
    private static final int ROW_SIZE = 5;
    private static final int BRIGHTNESS = 220;
    private static float tagScale;
    private static @Nullable PinnedTags pinned;

    private GogglesTagRenderer() {
    }

    @SubscribeEvent
    static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        TagState state = computeState(event);
        if (state != null) {
            render(event, state);
        }
    }

    private static @Nullable TagState computeState(RenderLevelStageEvent event) {
        if (tagScale > 0.0F) {
            tagScale -= 0.005F;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return null;
        }
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = blockHit.getBlockPos();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        if (pinned != null && pinned.pos().equals(pos)) {
            if (tagScale < 0.5F) {
                tagScale += 0.031F - tagScale / 10.0F;
            }
            Direction side = pinned.side();
            return new TagState(
                pos.getX() + side.getStepX() / 2.0F, pos.getY() + side.getStepY() / 2.0F, pos.getZ() + side.getStepZ() / 2.0F,
                entriesOf(pinned.aspects()), tagScale, player.getPosition(partialTick), side
            );
        }
        if (!AuraNodeRenderer.hasGoggles(player)) {
            return null;
        }
        AspectList tags = tagsAt(minecraft.level.getBlockEntity(pos));
        if (tags == null || tags.size() == 0) {
            return null;
        }
        if (tagScale < 0.3F) {
            tagScale += 0.031F - tagScale / 10.0F;
        }
        return new TagState(pos.getX(), pos.getY() + 0.4, pos.getZ(), entriesOf(tags), tagScale, player.getPosition(partialTick), Direction.UP);
    }

    private static List<Entry> entriesOf(AspectList tags) {
        List<Entry> entries = new ArrayList<>();
        for (Aspect aspect : tags.getAspects()) {
            entries.add(new Entry(aspect, tags.getAmount(aspect)));
        }
        return entries;
    }

    public static void showBlockTags(BlockPos pos, AspectList aspects, Direction side) {
        tagScale = 0.0F;
        pinned = aspects.isEmpty() ? null : new PinnedTags(pos.immutable(), aspects, side);
    }

    private static @Nullable AspectList tagsAt(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible.getTags();
        }
        if (blockEntity instanceof AlembicBlockEntity alembic) {
            return alembic.getSourceTags();
        }
        if (blockEntity instanceof JarBlockEntity jar) {
            return jar.getSourceTags();
        }
        return null;
    }

    private static void render(RenderLevelStageEvent event, TagState state) {
        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Font font = minecraft.font;
        float scale = state.scale();
        int current = 0;
        float shiftY = 0.0F;
        int left = state.entries().size();
        for (Entry entry : state.entries()) {
            int div = Math.min(left, ROW_SIZE);
            if (current >= ROW_SIZE) {
                current = 0;
                shiftY -= scale * 1.05F;
                left -= ROW_SIZE;
                if (left < ROW_SIZE) {
                    div = left % ROW_SIZE;
                }
            }
            float shift = (current - div / 2.0F + 0.5F) * scale * 4.0F;
            shift *= scale;
            poseStack.pushPose();
            poseStack.translate(
                state.x() + 0.5 + scale * 2.0F * state.side().getStepX() - cameraPos.x,
                state.y() - shiftY + 0.5 + scale * 2.0F * state.side().getStepY() - cameraPos.y,
                state.z() + 0.5 + scale * 2.0F * state.side().getStepZ() - cameraPos.z
            );
            float xd = (float) (state.viewer().x - (state.x() + 0.5));
            float zd = (float) (state.viewer().z - (state.z() + 0.5));
            float rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotYaw + 180.0F));
            poseStack.translate(shift, 0.0F, 0.0F);
            poseStack.scale(scale, scale, scale);
            renderIcon(bufferSource, poseStack, entry.aspect());
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.scale(0.04F, 0.04F, 0.04F);
            poseStack.translate(0.0F, 6.0F, -0.1F);
            String amount = String.valueOf(entry.amount());
            int width = font.width(amount);
            FormattedCharSequence text = Component.literal(amount).getVisualOrderText();
            font.drawInBatch(text, 14 - width, 1, 0xFF111111, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.translate(0.0F, 0.0F, -0.1F);
            font.drawInBatch(text, 13 - width, 0, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            current++;
        }
        bufferSource.endBatch();
    }

    private static void renderIcon(MultiBufferSource bufferSource, PoseStack poseStack, Aspect aspect) {
        float cell = 32.0F / 256.0F;
        float u0 = aspect.id % 8 * cell;
        float u1 = u0 + cell - 0.01F / 256.0F;
        float v0 = aspect.id / 8 * cell;
        float v1 = v0 + cell - 0.01F / 256.0F;
        int color = FastColor.ARGB32.color(Math.round(0.75F * 255), aspect.color);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = bufferSource.getBuffer(TcRenderTypes.translucent(AspectRenderer.TAGS));
        buffer.addVertex(pose, -0.5F, 0.5F, 0.0F).setUv(u1, v0).setColor(color).setLight(BRIGHTNESS);
        buffer.addVertex(pose, 0.5F, 0.5F, 0.0F).setUv(u0, v0).setColor(color).setLight(BRIGHTNESS);
        buffer.addVertex(pose, 0.5F, -0.5F, 0.0F).setUv(u0, v1).setColor(color).setLight(BRIGHTNESS);
        buffer.addVertex(pose, -0.5F, -0.5F, 0.0F).setUv(u1, v1).setColor(color).setLight(BRIGHTNESS);
    }

    private record Entry(Aspect aspect, int amount) {
    }

    private record TagState(double x, double y, double z, List<Entry> entries, float scale, Vec3 viewer, Direction side) {
    }

    private record PinnedTags(BlockPos pos, AspectList aspects, Direction side) {
    }
}
