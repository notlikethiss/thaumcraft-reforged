package thaumcraft.client.fx.bolt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.TcParticleLayers;
import thaumcraft.client.render.TcRenderTypes;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class BoltRenderer {
    private static final ContextKey<Frame> DATA_KEY = new ContextKey<>(Thaumcraft.id("lightning_bolts"));
    private static final float[][] OUTER_COLORS = {
        {0.6F, 0.3F, 0.6F},
        {0.6F, 0.6F, 0.1F},
        {0.1F, 0.1F, 0.6F},
        {0.1F, 1.0F, 0.1F},
        {0.6F, 0.1F, 0.1F},
        {0.6F, 0.2F, 0.6F},
        {0.75F, 1.0F, 1.0F}
    };
    private static final float[][] INNER_COLORS = {
        {1.0F, 0.6F, 1.0F},
        {1.0F, 1.0F, 0.1F},
        {0.1F, 0.1F, 1.0F},
        {0.1F, 0.6F, 0.1F},
        {1.0F, 0.1F, 0.1F},
        {0.0F, 0.0F, 0.0F},
        {0.75F, 1.0F, 1.0F}
    };
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final double VISIBLE_DISTANCE = 100.0;
    private static final List<LightningBolt> BOLTS = new ArrayList<>();

    private BoltRenderer() {
    }

    public static void add(LightningBolt bolt) {
        BOLTS.add(bolt.finalizeBolt());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            BOLTS.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        BOLTS.removeIf(bolt -> {
            bolt.tick();
            return bolt.isDead();
        });
    }

    @SubscribeEvent
    static void onExtract(ExtractLevelRenderStateEvent event) {
        if (BOLTS.isEmpty()) {
            return;
        }
        List<Snapshot> snapshots = new ArrayList<>(BOLTS.size());
        for (LightningBolt bolt : BOLTS) {
            snapshots.add(new Snapshot(bolt, bolt.particleAge));
        }
        event.getRenderState().setRenderData(DATA_KEY, new Frame(snapshots, event.getDeltaTracker().getGameTimeDeltaPartialTick(false)));
    }

    @SubscribeEvent
    static void onSubmit(SubmitCustomGeometryEvent event) {
        Frame frame = event.getLevelRenderState().getRenderData(DATA_KEY);
        if (frame == null) {
            return;
        }
        CameraRenderState camera = event.getLevelRenderState().cameraRenderState;
        Vec3 look = Vec3.directionFromRotation(camera.xRot, camera.yRot);
        BoltVector viewVector = new BoltVector(look.x, look.y, look.z);
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        for (Snapshot snapshot : frame.bolts()) {
            LightningBolt bolt = snapshot.bolt();
            if (camera.pos.distanceTo(new Vec3(bolt.start.x, bolt.start.y, bolt.start.z)) > VISIBLE_DISTANCE) {
                continue;
            }
            int type = Math.clamp(bolt.type, 0, OUTER_COLORS.length - 1);
            boolean translucent = type == 5 || type == 6;
            submitPass(collector, poseStack, renderType(TcParticleLayers.P_LARGE, translucent), bolt, snapshot.age(), frame.partialTick(), camera.pos,
                viewVector, OUTER_COLORS[type], 0);
            submitPass(collector, poseStack, renderType(TcParticleLayers.P_SMALL, translucent), bolt, snapshot.age(), frame.partialTick(), camera.pos,
                viewVector, INNER_COLORS[type], 1);
        }
    }

    private static RenderType renderType(ResourceLocation texture, boolean translucent) {
        return translucent ? TcRenderTypes.translucent(texture) : TcRenderTypes.additive(texture);
    }

    private static void submitPass(SubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, LightningBolt bolt, int age, float partialTick,
                                   Vec3 cameraPos, BoltVector viewVector, float[] color, int pass) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> renderBolt(pose, buffer, bolt, age, partialTick, cameraPos, viewVector, color, pass));
    }

    private static void renderBolt(PoseStack.Pose pose, VertexConsumer buffer, LightningBolt bolt, int age, float partialTick, Vec3 cameraPos,
                                   BoltVector viewVector, float[] color, int pass) {
        float boltAge = age >= 0 ? (float) age / bolt.particleMaxAge : 0.0F;
        float mainAlpha = pass == 0 ? (1.0F - boltAge) * 0.4F : 1.0F - boltAge * 0.5F;
        int lengthTicks = (int) (bolt.length * 3.0F);
        int renderLength = (int) ((age + partialTick + lengthTicks) / lengthTicks * bolt.numSegments0);
        float cx = (float) cameraPos.x;
        float cy = (float) cameraPos.y;
        float cz = (float) cameraPos.z;
        for (LightningBolt.Segment segment : bolt.segments) {
            if (segment.segmentNo > renderLength) {
                continue;
            }
            BoltVector startVec = segment.startPoint.point;
            BoltVector endVec = segment.endPoint.point;
            BoltVector relative = new BoltVector(cx - startVec.x, cy - startVec.y, cz - startVec.z);
            float width = bolt.width * (relative.length() / 5.0F + 1.0F) * (1.0F + segment.light) * 0.5F;
            BoltVector diff1 = BoltVector.crossProduct(viewVector, segment.prevDiff).scale(width / segment.sinPrev);
            BoltVector diff2 = BoltVector.crossProduct(viewVector, segment.nextDiff).scale(width / segment.sinNext);
            float rx1 = startVec.x - cx;
            float ry1 = startVec.y - cy;
            float rz1 = startVec.z - cz;
            float rx2 = endVec.x - cx;
            float ry2 = endVec.y - cy;
            float rz2 = endVec.z - cz;
            float alpha = mainAlpha * segment.light;
            vertex(pose, buffer, rx2 - diff2.x, ry2 - diff2.y, rz2 - diff2.z, 0.5F, 0.0F, color, alpha);
            vertex(pose, buffer, rx1 - diff1.x, ry1 - diff1.y, rz1 - diff1.z, 0.5F, 0.0F, color, alpha);
            vertex(pose, buffer, rx1 + diff1.x, ry1 + diff1.y, rz1 + diff1.z, 0.5F, 1.0F, color, alpha);
            vertex(pose, buffer, rx2 + diff2.x, ry2 + diff2.y, rz2 + diff2.z, 0.5F, 1.0F, color, alpha);
            if (segment.next == null) {
                BoltVector roundEnd = endVec.copy().add(segment.diff.copy().normalize().scale(width));
                float rx3 = roundEnd.x - cx;
                float ry3 = roundEnd.y - cy;
                float rz3 = roundEnd.z - cz;
                vertex(pose, buffer, rx3 - diff2.x, ry3 - diff2.y, rz3 - diff2.z, 0.0F, 0.0F, color, alpha);
                vertex(pose, buffer, rx2 - diff2.x, ry2 - diff2.y, rz2 - diff2.z, 0.5F, 0.0F, color, alpha);
                vertex(pose, buffer, rx2 + diff2.x, ry2 + diff2.y, rz2 + diff2.z, 0.5F, 1.0F, color, alpha);
                vertex(pose, buffer, rx3 + diff2.x, ry3 + diff2.y, rz3 + diff2.z, 0.0F, 1.0F, color, alpha);
            }
            if (segment.prev == null) {
                BoltVector roundEnd = startVec.copy().sub(segment.diff.copy().normalize().scale(width));
                float rx3 = roundEnd.x - cx;
                float ry3 = roundEnd.y - cy;
                float rz3 = roundEnd.z - cz;
                vertex(pose, buffer, rx1 - diff1.x, ry1 - diff1.y, rz1 - diff1.z, 0.5F, 0.0F, color, alpha);
                vertex(pose, buffer, rx3 - diff1.x, ry3 - diff1.y, rz3 - diff1.z, 0.0F, 0.0F, color, alpha);
                vertex(pose, buffer, rx3 + diff1.x, ry3 + diff1.y, rz3 + diff1.z, 0.0F, 1.0F, color, alpha);
                vertex(pose, buffer, rx1 + diff1.x, ry1 + diff1.y, rz1 + diff1.z, 0.5F, 1.0F, color, alpha);
            }
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, float[] color, float alpha) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color[0], color[1], color[2], alpha).setLight(FULL_BRIGHT);
    }

    private record Snapshot(LightningBolt bolt, int age) {
    }

    private record Frame(List<Snapshot> bolts, float partialTick) {
    }
}
