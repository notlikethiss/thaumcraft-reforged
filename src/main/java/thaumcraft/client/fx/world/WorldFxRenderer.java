package thaumcraft.client.fx.world;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class WorldFxRenderer {
    private static final ContextKey<Frame> DATA_KEY = new ContextKey<>(Thaumcraft.id("world_fx"));
    private static final List<WorldFx> EFFECTS = new ArrayList<>();

    private WorldFxRenderer() {
    }

    public static void add(WorldFx fx) {
        EFFECTS.add(fx);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            EFFECTS.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        EFFECTS.removeIf(fx -> {
            fx.tick();
            return fx.isDead();
        });
    }

    @SubscribeEvent
    static void onExtract(ExtractLevelRenderStateEvent event) {
        if (!EFFECTS.isEmpty()) {
            event.getRenderState().setRenderData(DATA_KEY, new Frame(List.copyOf(EFFECTS), event.getDeltaTracker().getGameTimeDeltaPartialTick(false)));
        }
    }

    @SubscribeEvent
    static void onSubmit(SubmitCustomGeometryEvent event) {
        Frame frame = event.getLevelRenderState().getRenderData(DATA_KEY);
        if (frame == null) {
            return;
        }
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        for (WorldFx fx : frame.effects()) {
            for (int pass = 0; pass < fx.passes(); pass++) {
                int current = pass;
                collector.submitCustomGeometry(poseStack, fx.renderType(pass), (pose, buffer) -> fx.render(current, pose, buffer, frame.partialTick(), camera));
            }
        }
    }

    private record Frame(List<WorldFx> effects, float partialTick) {
    }
}
