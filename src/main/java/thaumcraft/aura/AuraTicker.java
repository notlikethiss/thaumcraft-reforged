package thaumcraft.aura;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.lib.Utils;
import thaumcraft.registry.ModAttachments;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AuraTicker {
    private static int age;

    private AuraTicker() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            tick(level);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        AuraChunkData data = chunk.getData(ModAttachments.AURA_CHUNK);
        for (AuraNode node : data.takePending()) {
            node.dimension = level.dimension();
            if (node.key < 0) {
                node.key = AuraIdData.get(level.getServer()).nextId();
                chunk.markUnsaved();
            }
            if (AuraManager.getNode(node.key) == null) {
                AuraManager.addNode(node);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraManager.unloadDimension(level.dimension());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        AuraManager.clear();
    }

    static void markDirty(AuraNode node) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ServerLevel level = server.getLevel(node.dimension);
        if (level == null) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(node.x), SectionPos.blockToSectionCoord(node.z));
        if (chunk != null) {
            chunk.markUnsaved();
        }
    }

    private static void tick(ServerLevel level) {
        long now = System.currentTimeMillis();
        age++;
        LinkedList<Integer> updateList = AuraManager.UPDATE_LIST.get(level.dimension());
        if (updateList == null || updateList.isEmpty()) {
            populateUpdateList(level);
            updateList = AuraManager.UPDATE_LIST.get(level.dimension());
        }
        if (updateList != null && updateList.size() >= 1 + age % 20) {
            int limit = Math.max(1, updateList.size() / 20);
            for (int i = 0; i < limit && !updateList.isEmpty(); i++) {
                int key = updateList.getFirst();
                AuraNode node = AuraManager.getNodeCopy(key);
                updateList.removeFirst();
                if (node == null) {
                    continue;
                }
                if (Utils.isChunkLoaded(level, node.x, node.z)) {
                    calculate(level, node);
                    Long time = AuraManager.MARKED_FOR_TRANSMISSION.get(node.key);
                    if (time == null) {
                        time = now + level.getRandom().nextInt(Config.NODE_REFRESH * 1000);
                        AuraManager.MARKED_FOR_TRANSMISSION.put(node.key, time);
                    }
                    if (time <= now) {
                        AuraNode current = AuraManager.getNode(node.key);
                        if (current != null) {
                            AuraManager.sendNodePacket(level, current);
                        }
                        AuraManager.MARKED_FOR_TRANSMISSION.put(node.key, now + Config.NODE_REFRESH * 1000L);
                    }
                }
                if (AuraManager.getNode(key) != null) {
                    updateList.addLast(key);
                }
            }
        }
        if (!AuraManager.FLUX_EVENTS.isEmpty()) {
            AuraManager.FluxEvent event = AuraManager.FLUX_EVENTS.removeFirst();
            switch (event.severity()) {
                case 0 -> AuraManager.spawnMinorFluxEvent(event.level(), event.node(), event.aspect());
                case 1 -> AuraManager.spawnModerateFluxEvent(event.level(), event.node(), event.aspect());
                default -> AuraManager.spawnMajorFluxEvent(event.level(), event.node(), event.aspect());
            }
        }
    }

    private static void populateUpdateList(ServerLevel level) {
        LinkedList<Integer> list = new LinkedList<>();
        for (AuraNode node : AuraManager.NODES.values()) {
            if (node.dimension == level.dimension()) {
                list.add(node.key);
            }
        }
        AuraManager.UPDATE_LIST.put(level.dimension(), list);
    }

    private static int averageFlux(AspectList flux) {
        if (flux.size() == 0) {
            return 0;
        }
        return flux.visSize() / flux.size();
    }

    private static void calculate(ServerLevel level, AuraNode node) {
        updateNode(level, node);
        checkFlux(level, node);
    }

    private static void updateNode(ServerLevel level, AuraNode node) {
        List<Integer> neighbours = AuraManager.getNodeNeighbours(node.key);
        if (neighbours.isEmpty()) {
            AuraNode real = AuraManager.getNode(node.key);
            if (real != null) {
                AuraManager.generateNodeNeighbours(real);
            }
        }
        int fluxTotal = averageFlux(node.flux);
        switch (node.type) {
            case PURE -> {
                if (level.getRandom().nextInt(20) == 7) {
                    AuraManager.removeRandomFlux(level, node, 1);
                }
            }
            case DARK -> {
                if (level.getRandom().nextInt(5 + fluxTotal) == 0) {
                    Aspect aspect = level.getRandom().nextBoolean() ? Aspect.EVIL : Aspect.DEATH;
                    AuraManager.queueNodeChanges(node.key, 0, 0, false, new AspectList().add(aspect, 1), 0, 0, 0);
                }
            }
            case UNSTABLE -> {
                if (level.getRandom().nextInt(1 + fluxTotal) == 0) {
                    AuraManager.addRandomFlux(level, node, 1);
                }
            }
            default -> {
            }
        }
        if (node.level > node.baseLevel && level.getRandom().nextFloat() > (float) node.baseLevel / node.level) {
            AuraManager.addRandomFlux(level, node, 1);
        }
        for (Integer key : neighbours) {
            if (AuraManager.getNode(node.key) == null) {
                return;
            }
            AuraNode target = AuraManager.getNodeCopy(key);
            if (target == null || !Utils.isChunkLoaded(level, target.x, target.z)) {
                continue;
            }
            boolean sendTransferFx = false;
            if (node.level < node.baseLevel && node.level < target.level && level.getRandom().nextFloat() > (float) node.level / target.level) {
                node.level++;
                target.level--;
                AuraManager.queueNodeChanges(node.key, 1, 0, false, null, 0, 0, 0);
                AuraManager.queueNodeChanges(target.key, -1, 0, false, null, 0, 0, 0);
                AuraManager.addRandomFlux(level, node, 1);
                sendTransferFx = true;
            }
            double xd = node.x - target.x;
            double yd = node.y - target.y;
            double zd = node.z - target.z;
            double distSq = xd * xd + yd * yd + zd * zd;
            if (distSq < (node.level + target.level) / 2 && distSq > 0.25) {
                float total = node.level + target.level;
                if (!node.locked) {
                    float mx = (float) (-xd / distSq / total * target.level);
                    float my = (float) (-yd / distSq / total * target.level);
                    float mz = (float) (-zd / distSq / total * target.level);
                    node.x += mx;
                    node.y += my;
                    node.z += mz;
                    AuraManager.queueNodeChanges(node.key, 0, 0, false, motionFlux(level), mx, my, mz);
                }
                if (!target.locked) {
                    float mx = (float) (xd / distSq / total * node.level);
                    float my = (float) (yd / distSq / total * node.level);
                    float mz = (float) (zd / distSq / total * node.level);
                    target.x += mx;
                    target.y += my;
                    target.z += mz;
                    AuraManager.queueNodeChanges(target.key, 0, 0, false, motionFlux(level), mx, my, mz);
                }
            } else if (distSq <= 0.3F) {
                if (node.baseLevel > target.baseLevel) {
                    merge(level, node, target, false);
                } else {
                    merge(level, target, node, true);
                }
            }
            if (sendTransferFx) {
                AuraManager.sendNodeTransferFxPacket(level, target, node, distSq);
            }
        }
    }

    private static AspectList motionFlux(ServerLevel level) {
        return level.getRandom().nextInt(25) == 0 ? new AspectList().add(Aspect.MOTION, 1) : null;
    }

    private static void merge(ServerLevel level, AuraNode survivor, AuraNode absorbed, boolean useSurvivorBase) {
        double ox = survivor.x;
        double oy = survivor.y;
        double oz = survivor.z;
        survivor.level = (short) (survivor.level + absorbed.level * 0.75F);
        survivor.baseLevel = (short) (survivor.baseLevel + absorbed.baseLevel * 0.33F);
        survivor.x = (survivor.x + absorbed.x) / 2.0;
        survivor.y = (survivor.y + absorbed.y) / 2.0;
        survivor.z = (survivor.z + absorbed.z) / 2.0;
        AspectList flux = new AspectList();
        for (Aspect aspect : absorbed.flux.getAspects()) {
            flux.add(aspect, absorbed.flux.getAmount(aspect));
        }
        int fluxBase = useSurvivorBase ? survivor.baseLevel : absorbed.baseLevel;
        flux.add(Aspect.EXCHANGE, (int) (fluxBase * 0.1F));
        AuraManager.addRandomFlux(level, survivor, (int) (fluxBase * 0.3F));
        AuraManager.queueNodeChanges(
            survivor.key,
            (int) (absorbed.level * 0.75F),
            (int) (absorbed.baseLevel * 0.33F),
            false,
            flux,
            (float) (survivor.x - ox),
            (float) (survivor.y - oy),
            (float) (survivor.z - oz)
        );
        AuraNode removed = AuraManager.getNode(absorbed.key);
        if (removed != null) {
            AuraManager.deleteNode(absorbed.key);
            AuraManager.sendNodeDeletionPacket(removed);
        }
    }

    private static void checkFlux(ServerLevel level, AuraNode node) {
        AuraNode current = AuraManager.getNode(node.key);
        if (current == null) {
            return;
        }
        for (Aspect aspect : node.flux.getAspects()) {
            int amount = node.flux.getAmount(aspect);
            int roll = level.getRandom().nextInt(2500);
            if (roll < 10 && amount >= 10) {
                AuraManager.FLUX_EVENTS.add(new AuraManager.FluxEvent(level, node, aspect, 0));
                break;
            }
            if (roll < 15 && amount >= 25) {
                AuraManager.FLUX_EVENTS.add(new AuraManager.FluxEvent(level, node, aspect, 1));
                break;
            }
            if (roll < 20 && amount >= 50) {
                AuraManager.FLUX_EVENTS.add(new AuraManager.FluxEvent(level, node, aspect, 2));
                break;
            }
        }
    }
}
