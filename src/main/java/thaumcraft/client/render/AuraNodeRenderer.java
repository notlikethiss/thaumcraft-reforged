package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.registry.ModItems;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AuraNodeRenderer {
    private static final ResourceLocation AURA_1 = Thaumcraft.id("textures/misc/aura_1.png");
    private static final ResourceLocation AURA_2 = Thaumcraft.id("textures/misc/aura_2.png");
    private static final ResourceLocation AURA_3 = Thaumcraft.id("textures/misc/aura_3.png");
    private static final ResourceLocation PURE = Thaumcraft.id("textures/misc/pure.png");
    private static final ResourceLocation VORTEX = Thaumcraft.id("textures/misc/vortex.png");
    private static final ResourceLocation CHAOS = Thaumcraft.id("textures/misc/chaos.png");
    private static final ResourceLocation LOCK = Thaumcraft.id("textures/misc/aura_lock.png");
    private static final float TAU = (float) (Math.PI * 2);
    private static final int MAX_NODES = 10;
    private static @Nullable ResourceKey<Level> previousDimension;

    private AuraNodeRenderer() {
    }

    public static boolean hasGoggles(@Nullable Player player) {
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GOGGLES_OF_REVEALING.get());
    }

    private static float[] renderPosition(AuraClientData.ClientNode node) {
        return AuraClientData.RENDER_POSITIONS.computeIfAbsent(node.key(), key -> new float[]{node.x(), node.y(), node.z()});
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            previousDimension = null;
            return;
        }
        if (previousDimension != level.dimension()) {
            AuraClientData.retainDimension(level.dimension());
            previousDimension = level.dimension();
        }
        Player player = minecraft.player;
        if (minecraft.screen != null || !hasGoggles(player)) {
            return;
        }
        RandomSource random = level.getRandom();
        int limit = 0;
        for (AuraClientData.ClientNode node : AuraClientData.NODES.values()) {
            if (node.flux() <= 0 || limit >= MAX_NODES || node.dimension() != level.dimension()) {
                continue;
            }
            limit++;
            float[] position = renderPosition(node);
            position[0] += (node.x() - position[0]) / 50.0F;
            position[1] += (node.y() - position[1]) / 50.0F;
            position[2] += (node.z() - position[2]) / 50.0F;
            if (random.nextInt(1000) < node.flux()) {
                ClientFx.bolt(
                    level,
                    position[0],
                    position[1],
                    position[2],
                    position[0] + (random.nextFloat() - random.nextFloat()) * 5.0F,
                    position[1] + (random.nextFloat() - random.nextFloat()) * 5.0F,
                    position[2] + (random.nextFloat() - random.nextFloat()) * 5.0F,
                    10,
                    2.0F,
                    5,
                    5
                );
            }
        }
    }

    @SubscribeEvent
    static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (level == null || !hasGoggles(player)) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        long time = level.getGameTime();
        List<Quad> quads = new ArrayList<>();
        int limit = 0;
        for (AuraClientData.ClientNode node : AuraClientData.NODES.values()) {
            if (limit >= MAX_NODES || node.dimension() != level.dimension() || player.distanceToSqr(node.x(), node.y(), node.z()) >= 4096.0) {
                continue;
            }
            limit++;
            float[] position = renderPosition(node);
            position[0] += (node.x() - position[0]) / 50.0F * partialTick;
            position[1] += (node.y() - position[1]) / 50.0F * partialTick;
            position[2] += (node.z() - position[2]) / 50.0F * partialTick;
            float x = position[0];
            float y = position[1];
            float z = position[2];
            float base = node.level() / 1000.0F;
            quads.add(new Quad(AURA_1, false, x, y, z, time % 500L / 500.0F * TAU, Mth.sin((time + x) / 14.0F) * base + base * 2.0F));
            quads.add(new Quad(AURA_2, false, x, y, z, time % 400L / -400.0F * TAU, Mth.sin((time + y) / 11.0F) * base + base * 2.0F));
            quads.add(new Quad(AURA_3, false, x, y, z, time % 300L / 300.0F * TAU, Mth.sin((time + z) / 9.0F) * base + base * 2.0F));
            switch (node.type()) {
                case NORMAL -> quads.add(new Quad(AURA_1, false, x, y, z, time % 200L / -200.0F * TAU, Mth.sin((time + x) / 7.0F) * base / 2.0F + base * 2.0F));
                case PURE -> quads.add(typeQuad(PURE, false, x, y, z, time, base));
                case DARK -> quads.add(typeQuad(VORTEX, true, x, y, z, time, base));
                case UNSTABLE -> quads.add(typeQuad(CHAOS, false, x, y, z, time, base));
            }
            if (node.locked()) {
                quads.add(new Quad(LOCK, false, x, y, z, 0.0F, base * 3.5F));
            }
        }
        if (quads.isEmpty()) {
            return;
        }
        Camera camera = event.getCamera();
        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Set<RenderType> used = new LinkedHashSet<>();
        for (Quad quad : quads) {
            RenderType renderType = quad.translucent() ? TcRenderTypes.translucent(quad.texture()) : TcRenderTypes.additive(quad.texture());
            used.add(renderType);
            facingQuad(pose, bufferSource.getBuffer(renderType), camera, quad.x(), quad.y(), quad.z(), quad.angle(), quad.scale(), 0xFFFFFFFF);
        }
        for (RenderType renderType : used) {
            bufferSource.endBatch(renderType);
        }
    }

    private static Quad typeQuad(ResourceLocation texture, boolean translucent, float x, float y, float z, long time, float base) {
        return new Quad(texture, translucent, x, y, z, time % 90L / -90.0F * TAU, Mth.sin((time + x) / 10.0F) * base / 4.0F + base * 1.75F);
    }

    public static void facingQuad(PoseStack.Pose pose, VertexConsumer buffer, Camera camera, float x, float y, float z, float angle, float scale,
                                  int color) {
        float yaw = camera.getYRot() * Mth.DEG_TO_RAD;
        float pitch = camera.getXRot() * Mth.DEG_TO_RAD;
        float arX = Mth.cos(yaw);
        float arZ = Mth.sin(yaw);
        float arYZ = -arZ * Mth.sin(pitch);
        float arXY = arX * Mth.sin(pitch);
        float arXZ = Mth.cos(pitch);
        Vector3f[] corners = {
            new Vector3f(-arX * scale - arYZ * scale, -arXZ * scale, -arZ * scale - arXY * scale),
            new Vector3f(-arX * scale + arYZ * scale, arXZ * scale, -arZ * scale + arXY * scale),
            new Vector3f(arX * scale + arYZ * scale, arXZ * scale, arZ * scale + arXY * scale),
            new Vector3f(arX * scale - arYZ * scale, -arXZ * scale, arZ * scale - arXY * scale)
        };
        Vec3 cameraPos = camera.getPosition();
        if (angle != 0.0F) {
            Vector3f axis = new Vector3f((float) (cameraPos.x - x), (float) (cameraPos.y - y), (float) (cameraPos.z - z)).normalize();
            Quaternionf rotation = new Quaternionf().rotateAxis(angle, axis);
            for (Vector3f corner : corners) {
                rotation.transform(corner);
            }
        }
        float rx = (float) (x - cameraPos.x);
        float ry = (float) (y - cameraPos.y);
        float rz = (float) (z - cameraPos.z);
        float[][] uvs = {{0.0F, 1.0F}, {1.0F, 1.0F}, {1.0F, 0.0F}, {0.0F, 0.0F}};
        for (int i = 0; i < 4; i++) {
            buffer.addVertex(pose, rx + corners[i].x, ry + corners[i].y, rz + corners[i].z)
                .setUv(uvs[i][0], uvs[i][1])
                .setColor(color)
                .setLight(0xF000F0);
        }
    }

    private record Quad(ResourceLocation texture, boolean translucent, float x, float y, float z, float angle, float scale) {
    }
}
