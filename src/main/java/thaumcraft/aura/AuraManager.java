package thaumcraft.aura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import thaumcraft.registry.ModAttachments;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.AABB;
import thaumcraft.network.ModNetwork;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.lib.Utils;
import thaumcraft.network.AuraDeletePayload;
import thaumcraft.network.AuraNodePayload;
import thaumcraft.network.AuraTransferFxPayload;
import thaumcraft.network.NodeZapPayload;
import thaumcraft.registry.ModSounds;
import thaumcraft.world.BiomeHandler;

public final class AuraManager {
    private record ChunkKey(ResourceKey<Level> dimension, int x, int z) {
    }

    record FluxEvent(ServerLevel level, AuraNode node, Aspect aspect, int severity) {
    }

    static final Map<Integer, AuraNode> NODES = new java.util.concurrent.ConcurrentHashMap<>();
    static final Map<ResourceKey<Level>, LinkedList<Integer>> UPDATE_LIST = new HashMap<>();
    static final Map<Integer, List<Integer>> NEIGHBOURS = new HashMap<>();
    private static final Map<ChunkKey, List<Integer>> NODE_CHUNKS = new HashMap<>();
    static final Map<Integer, Long> MARKED_FOR_TRANSMISSION = new HashMap<>();
    static final List<FluxEvent> FLUX_EVENTS = new ArrayList<>();

    private AuraManager() {
    }

    public static void clear() {
        NODES.clear();
        UPDATE_LIST.clear();
        NEIGHBOURS.clear();
        NODE_CHUNKS.clear();
        MARKED_FOR_TRANSMISSION.clear();
        FLUX_EVENTS.clear();
    }

    public static AuraChunkData chunkData(ChunkAccess chunk) {
        return chunk.getData(ModAttachments.AURA_CHUNK);
    }

    public static boolean hasChunkData(ChunkAccess chunk) {
        return chunk.hasData(ModAttachments.AURA_CHUNK);
    }

    public static int registerAuraNode(ServerLevel level, int auraLevel, NodeType type, BlockPos pos) {
        int key = AuraIdData.get(level.getServer()).nextId();
        AuraNode node = new AuraNode(key, auraLevel, type, level.dimension(), pos);
        addNode(node);
        markChunkUnsaved(level, node);
        return key;
    }

    static void addNode(AuraNode node) {
        NODES.put(node.key, node);
        addToAuraUpdateList(node);
        generateNodeNeighbours(node);
    }

    public static List<Integer> getNodeNeighbours(int key) {
        List<Integer> neighbours = NEIGHBOURS.get(key);
        return neighbours == null ? new ArrayList<>() : new ArrayList<>(neighbours);
    }

    public static @Nullable List<Integer> getNodeNeighboursNeighbours(int key) {
        List<Integer> neighbours = getNodeNeighbours(key);
        if (neighbours.isEmpty()) {
            return null;
        }
        List<Integer> result = new ArrayList<>(neighbours);
        for (Integer neighbour : neighbours) {
            for (Integer second : getNodeNeighbours(neighbour)) {
                if (!result.contains(second)) {
                    result.add(second);
                }
            }
        }
        return result;
    }

    public static void addToAuraUpdateList(AuraNode node) {
        LinkedList<Integer> list = UPDATE_LIST.computeIfAbsent(node.dimension, dimension -> new LinkedList<>());
        if (!list.contains(node.key)) {
            list.add(node.key);
        }
    }

    private static ChunkKey chunkOf(AuraNode node) {
        return new ChunkKey(
            node.dimension,
            SectionPos.blockToSectionCoord(node.x),
            SectionPos.blockToSectionCoord(node.z)
        );
    }

    private static void indexChunk(AuraNode node) {
        List<Integer> list = NODE_CHUNKS.computeIfAbsent(chunkOf(node), chunk -> new ArrayList<>());
        if (!list.contains(node.key)) {
            list.add(node.key);
        }
    }

    private static void unindexChunk(AuraNode node) {
        List<Integer> list = NODE_CHUNKS.get(chunkOf(node));
        if (list != null) {
            list.remove(Integer.valueOf(node.key));
        }
    }

    public static void generateNodeNeighbours(AuraNode node) {
        List<Integer> neighbours = new ArrayList<>();
        LinkedList<Integer> updateList = UPDATE_LIST.get(node.dimension);
        if (updateList != null) {
            for (Integer key : updateList) {
                if (key == node.key) {
                    continue;
                }
                AuraNode target = getNode(key);
                if (target != null && isInfluenced(node, target)) {
                    neighbours.add(target.key);
                }
            }
        }
        NEIGHBOURS.put(node.key, neighbours);
        indexChunk(node);
    }

    private static boolean isInfluenced(AuraNode node, AuraNode target) {
        float influence = Math.max(node.baseLevel / 4.0F, target.baseLevel / 4.0F);
        return influence * influence >= node.distanceSq(target.x, target.y, target.z);
    }

    public static void updateNodeNeighbours(AuraNode node) {
        List<Integer> candidates = getNodeNeighboursNeighbours(node.key);
        if (candidates == null || candidates.isEmpty()) {
            generateNodeNeighbours(node);
            return;
        }
        List<Integer> newNeighbours = new ArrayList<>();
        for (Integer key : candidates) {
            AuraNode target = getNode(key);
            if (target == null || key == node.key) {
                continue;
            }
            List<Integer> targetNeighbours = NEIGHBOURS.computeIfAbsent(target.key, k -> new ArrayList<>());
            if (isInfluenced(node, target)) {
                newNeighbours.add(target.key);
                if (!targetNeighbours.contains(node.key)) {
                    targetNeighbours.add(node.key);
                }
            } else {
                targetNeighbours.remove(Integer.valueOf(node.key));
            }
        }
        NEIGHBOURS.put(node.key, newNeighbours);
    }

    public static int getClosestAuraWithinRange(Level level, double x, double y, double z, double range) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return -1;
        }
        int cx = SectionPos.blockToSectionCoord(x);
        int cz = SectionPos.blockToSectionCoord(z);
        int closest = -1;
        double closestRange = Double.MAX_VALUE;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                List<Integer> keys = NODE_CHUNKS.get(new ChunkKey(level.dimension(), cx + dx, cz + dz));
                if (keys == null) {
                    continue;
                }
                for (Integer key : keys) {
                    AuraNode node = getNode(key);
                    if (node != null && !node.locked && Utils.isChunkLoaded(serverLevel, node.x, node.z)) {
                        double distSq = node.distanceSq(x, y, z);
                        if (range * range >= distSq && distSq < closestRange) {
                            closest = key;
                            closestRange = distSq;
                        }
                    }
                }
            }
        }
        return closest;
    }

    public static List<Integer> getAurasWithin(Level level, double x, double y, double z) {
        List<Integer> result = new ArrayList<>();
        if (!(level instanceof ServerLevel serverLevel)) {
            return result;
        }
        int cx = SectionPos.blockToSectionCoord(x);
        int cz = SectionPos.blockToSectionCoord(z);
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                List<Integer> keys = NODE_CHUNKS.get(new ChunkKey(level.dimension(), cx + dx, cz + dz));
                if (keys == null) {
                    continue;
                }
                for (Integer key : keys) {
                    AuraNode node = getNode(key);
                    if (node != null && Utils.isChunkLoaded(serverLevel, node.x, node.z)) {
                        float influence = node.baseLevel;
                        if (influence * influence >= node.distanceSq(x, y, z)) {
                            result.add(key);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean decreaseClosestAura(Level level, double x, double y, double z, int amount) {
        return amount == 0 || decreaseClosestAura(level, x, y, z, amount, true);
    }

    public static boolean decreaseClosestAura(Level level, double x, double y, double z, int amount, boolean doit) {
        if (level.isClientSide()) {
            return false;
        }
        List<double[]> sorted = new ArrayList<>();
        for (Integer key : getAurasWithin(level, x, y, z)) {
            AuraNode node = getNode(key);
            if (node == null || node.level <= 0) {
                continue;
            }
            float influence = node.baseLevel / 4.0F;
            double distSq = node.distanceSq(x, y, z);
            if (influence * influence >= distSq) {
                int index = 0;
                while (index < sorted.size() && sorted.get(index)[0] <= distSq) {
                    index++;
                }
                sorted.add(index, new double[]{distSq, node.key});
            }
        }
        if (sorted.isEmpty()) {
            return false;
        }
        int total = 0;
        List<int[]> drains = new ArrayList<>();
        for (double[] entry : sorted) {
            AuraNode node = getNode((int) entry[1]);
            if (node != null && node.level > 0) {
                if (node.level >= amount - total) {
                    drains.add(new int[]{amount - total, node.key});
                    total = amount;
                    break;
                }
                drains.add(new int[]{node.level, node.key});
                total += node.level;
            }
            if (amount - total == 0) {
                break;
            }
        }
        if (total != amount) {
            return false;
        }
        if (doit) {
            for (int[] drain : drains) {
                if (getNode(drain[1]) != null) {
                    queueNodeChanges(drain[1], -drain[0], 0, false, null, 0, 0, 0);
                }
            }
        }
        return true;
    }

    public static boolean increaseLowestAura(Level level, double x, double y, double z, int amount) {
        return increaseLowestAuraWithLimit(level, x, y, z, amount, Float.MAX_VALUE);
    }

    public static boolean increaseLowestAuraWithLimit(Level level, double x, double y, double z, int amount, float limit) {
        if (level.isClientSide()) {
            return false;
        }
        int lowestLevel = Integer.MAX_VALUE;
        AuraNode lowest = null;
        for (Integer key : getAurasWithin(level, x, y, z)) {
            AuraNode node = getNode(key);
            if (node != null && node.level < lowestLevel && node.level < node.baseLevel * limit) {
                float influence = node.baseLevel / 4.0F;
                if (influence * influence >= node.distanceSq(x, y, z)) {
                    lowest = node;
                    lowestLevel = node.level;
                }
            }
        }
        if (lowest == null) {
            return false;
        }
        queueNodeChanges(lowest.key, amount, 0, false, null, 0, 0, 0);
        return true;
    }

    public static boolean auraNearby(ResourceKey<Level> dimension, int x, int y, int z, int range) {
        for (AuraNode node : NODES.values()) {
            if (node.dimension == dimension && withinRange(node, x, y, z, range)) {
                return true;
            }
        }
        return false;
    }

    public static boolean specificAuraTypeNearby(ResourceKey<Level> dimension, int x, int y, int z, NodeType type, int range) {
        for (AuraNode node : NODES.values()) {
            if (node.dimension == dimension && node.type == type && withinRange(node, x, y, z, range)) {
                return true;
            }
        }
        return false;
    }

    private static boolean withinRange(AuraNode node, int x, int y, int z, int range) {
        double dx = (float) node.x - x + 0.5F;
        double dy = (float) node.y - y + 0.5F;
        double dz = (float) node.z - z + 0.5F;
        return dx * dx + dy * dy + dz * dz < range * range;
    }

    public static void sendNodePacket(ServerLevel level, AuraNode node) {
        ModNetwork.sendToNear(level, null, node.x, node.y, node.z, Math.max(32.0F, node.baseLevel / 4.0F), AuraNodePayload.of(node));
    }

    static void sendNodeTransferFxPacket(ServerLevel level, AuraNode node, AuraNode target, double distanceSq) {
        double x = (node.x + target.x) / 2.0;
        double y = (node.y + target.y) / 2.0;
        double z = (node.z + target.z) / 2.0;
        ModNetwork.sendToNear(
            level,
            null,
            x,
            y,
            z,
            Math.sqrt(distanceSq) + 32.0,
            new AuraTransferFxPayload(
                new Vector3f((float) node.x, (float) node.y, (float) node.z),
                new Vector3f((float) target.x, (float) target.y, (float) target.z)
            )
        );
    }

    static void sendNodeDeletionPacket(AuraNode node) {
        ModNetwork.sendToAll(new AuraDeletePayload(node.key));
    }

    public static void addFluxToClosest(Level level, float x, float y, float z, AspectList tags) {
        if (level.isClientSide()) {
            return;
        }
        double closestDistance = Double.MAX_VALUE;
        int closestKey = -1;
        for (Integer key : getAurasWithin(level, x, y, z)) {
            AuraNode node = getNode(key);
            if (node != null) {
                float influence = node.baseLevel / 4.0F;
                double distSq = node.distanceSq(x, y, z);
                if (influence * influence >= distSq && distSq < closestDistance) {
                    closestDistance = distSq;
                    closestKey = key;
                }
            }
        }
        if (closestKey < 0 || getNode(closestKey) == null) {
            return;
        }
        AspectList flux = new AspectList();
        for (Aspect aspect : tags.getAspects()) {
            if (tags.getAmount(aspect) > 0) {
                flux.add(aspect, tags.getAmount(aspect));
            }
        }
        if (flux.size() > 0) {
            queueNodeChanges(closestKey, 0, 0, false, flux, 0, 0, 0);
        }
    }

    public static void removeRandomFlux(ServerLevel level, AuraNode node, int amount) {
        AspectList flux = new AspectList();
        for (int i = 0; i < amount; i++) {
            List<Aspect> aspects = node.flux.getAspects();
            if (!aspects.isEmpty()) {
                Aspect aspect = aspects.getFirst();
                if (level.getRandom().nextInt(5) == 0 && -flux.getAmount(aspect) < node.flux.getAmount(aspect)) {
                    flux.add(aspect, -1);
                }
            }
        }
        if (flux.size() > 0) {
            queueNodeChanges(node.key, 0, 0, false, flux, 0, 0, 0);
        }
    }

    public static void addRandomFlux(ServerLevel level, AuraNode node, int amount) {
        AspectList flux = new AspectList();
        for (int i = 0; i < amount; i++) {
            if (level.getRandom().nextInt(5) != 0) {
                continue;
            }
            switch (level.getRandom().nextInt(3)) {
                case 0 -> {
                    Holder<net.minecraft.world.level.biome.Biome> biome = level.getBiome(BlockPos.containing(node.x, node.y, node.z));
                    flux.add(BiomeHandler.getRandomBiomeTag(biome, level.getRandom()), 1);
                }
                case 1 -> flux.add(randomCommonFlux(level), 1);
                default -> flux.add(Aspect.FLUX, 1);
            }
        }
        if (flux.size() > 0) {
            queueNodeChanges(node.key, 0, 0, false, flux, 0, 0, 0);
        }
    }

    private static Aspect randomCommonFlux(ServerLevel level) {
        return switch (level.getRandom().nextInt(20)) {
            case 0, 1 -> Aspect.WIND;
            case 2 -> Aspect.MOTION;
            case 3, 4 -> Aspect.FIRE;
            case 5 -> Aspect.POWER;
            case 6, 7 -> Aspect.WATER;
            case 8 -> Aspect.COLD;
            case 9, 10 -> Aspect.EARTH;
            case 11 -> Aspect.ROCK;
            case 12 -> Aspect.POISON;
            case 13 -> Aspect.PLANT;
            case 14 -> Aspect.WOOD;
            case 15, 16 -> Aspect.MAGIC;
            case 17 -> Aspect.BEAST;
            case 18 -> Aspect.DEATH;
            default -> Aspect.WEATHER;
        };
    }

    public static boolean spawnMajorFluxEvent(ServerLevel level, AuraNode node, Aspect aspect) {
        boolean success = aspect == Aspect.PURE;
        if (success) {
            queueNodeChanges(node.key, 0, 0, false, new AspectList().add(aspect, -50), 0, 0, 0);
        }
        return success;
    }

    public static boolean spawnModerateFluxEvent(ServerLevel level, AuraNode node, Aspect aspect) {
        boolean success = switch (aspect) {
            case PURE -> true;
            case DEATH -> spawnGiant(level, node);
            default -> false;
        };
        if (success) {
            queueNodeChanges(node.key, 0, 0, false, new AspectList().add(aspect, -25), 0, 0, 0);
        }
        return success;
    }

    public static boolean spawnMinorFluxEvent(ServerLevel level, AuraNode node, Aspect aspect) {
        boolean success;
        if (level.getRandom().nextInt(3) == 0) {
            success = spawnWisp(level, node, aspect);
        } else {
            success = switch (aspect) {
                case PURE -> true;
                case DEATH -> spawnDeath(level, node);
                case POWER, DESTRUCTION -> spawnLightning(level, node);
                case POISON, INSECT -> poisonCreature(level, node, MobEffects.POISON);
                case DARK, VOID -> poisonCreature(level, node, MobEffects.BLINDNESS);
                case ARMOR -> poisonCreature(level, node, MobEffects.RESISTANCE);
                case MOTION -> poisonCreature(level, node, MobEffects.SPEED);
                case FLIGHT -> poisonCreature(level, node, MobEffects.JUMP_BOOST);
                case TOOL -> poisonCreature(level, node, MobEffects.HASTE);
                case ROCK -> poisonCreature(level, node, MobEffects.MINING_FATIGUE);
                case COLD -> poisonCreature(level, node, MobEffects.SLOWNESS);
                case SOUND, KNOWLEDGE, FUNGUS -> poisonCreature(level, node, MobEffects.NAUSEA);
                case EVIL -> spawnEvil(level, node);
                case FIRE -> spawnFire(level, node);
                case CROP, PLANT, WOOD -> promoteGrowth(level, node);
                default -> false;
            };
        }
        if (success) {
            queueNodeChanges(node.key, 0, 0, false, new AspectList().add(aspect, -10), 0, 0, 0);
        }
        return success;
    }

    private static int fuzz(ServerLevel level, int fuzz) {
        return fuzz <= 0 ? 0 : level.getRandom().nextInt(fuzz) - level.getRandom().nextInt(fuzz);
    }

    private static boolean promoteGrowth(ServerLevel level, AuraNode node) {
        int fuzz = (int) (node.baseLevel / 8.0F);
        double x = node.x + fuzz(level, fuzz);
        double z = node.z + fuzz(level, fuzz);
        int y = Utils.getFirstUncoveredBlockHeight(level, (int) x, (int) z);
        if (!Utils.isChunkLoaded(level, x, z)) {
            return false;
        }
        return BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, BlockPos.containing(x, y, z));
    }

    private static boolean poisonCreature(ServerLevel level, AuraNode node, Holder<MobEffect> effect) {
        float range = node.baseLevel / 4.0F;
        AABB box = new AABB(node.x - 1, node.y - 1, node.z - 1, node.x + 1, node.y + 1, node.z + 1).inflate(range);
        List<LivingEntity> entities = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, box));
        boolean did = false;
        for (int i = 0; i < 3 && !entities.isEmpty(); i++) {
            int index = level.getRandom().nextInt(entities.size());
            LivingEntity entity = entities.get(index);
            if (Utils.isChunkLoaded(level, entity.getX(), entity.getZ())) {
                entity.addEffect(new MobEffectInstance(effect, 100 + level.getRandom().nextInt(200), 0));
                if (entity instanceof Player player) {
                    player.sendSystemMessage(Component.translatable("tc.thaumcraft.flux.strange_energies")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN).withItalic(true)));
                }
                did = true;
                entities.remove(index);
            }
        }
        return did;
    }

    private static boolean spawnLightning(ServerLevel level, AuraNode node) {
        float range = node.baseLevel / 4.0F / 2.0F;
        AABB box = new AABB(node.x - 1, node.y - 1, node.z - 1, node.x + 1, node.y + 1, node.z + 1).inflate(range);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (Utils.isChunkLoaded(level, entity.getX(), entity.getZ())) {
                ModNetwork.sendToNear(
                    level,
                    null,
                    node.x,
                    node.y,
                    node.z,
                    64.0,
                    new NodeZapPayload(new Vector3f((float) node.x, (float) node.y, (float) node.z), entity.getId())
                );
                level.playSound(null, node.x, node.y, node.z, ModSounds.ZAP.get(), SoundSource.AMBIENT, 1.0F, 1.1F);
                entity.hurtServer(level, level.damageSources().magic(), 5);
                return true;
            }
        }
        return false;
    }

    private static @Nullable Mob createMob(ServerLevel level, String id) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Thaumcraft.id(id)).orElse(null);
        if (type == null) {
            return null;
        }
        Entity entity = type.create(level, EntitySpawnReason.EVENT);
        return entity instanceof Mob mob ? mob : null;
    }

    private static boolean trySpawn(ServerLevel level, Mob mob, double x, double y, double z) {
        mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        return mob.checkSpawnRules(level, EntitySpawnReason.EVENT) && level.addFreshEntity(mob);
    }

    private static boolean spawnGiant(ServerLevel level, AuraNode node) {
        int fuzz = (int) (node.baseLevel / 4.0F) / 3;
        double x = node.x + fuzz(level, fuzz);
        double z = node.z + fuzz(level, fuzz);
        double y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z) + 5;
        if (!Utils.isChunkLoaded(level, x, z)) {
            return false;
        }
        Mob zombie = createMob(level, "giant_brainy_zombie");
        if (zombie == null) {
            return false;
        }
        boolean spawned = trySpawn(level, zombie, x, y, z);
        if (spawned) {
            Component message = Component.translatable("tc.thaumcraft.flux.foul_node")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true));
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(x, y, z) < 64.0 * 64.0) {
                    player.sendSystemMessage(message);
                }
            }
        }
        return spawned;
    }

    private static boolean spawnFire(ServerLevel level, AuraNode node) {
        int fuzz = (int) (node.baseLevel / 4.0F) / 3;
        double x = node.x + fuzz(level, fuzz);
        double y = node.y + fuzz(level, fuzz);
        double z = node.z + fuzz(level, fuzz);
        if (!Utils.isChunkLoaded(level, x, z)) {
            return false;
        }
        Mob bat = createMob(level, "fire_bat");
        if (bat == null) {
            return false;
        }
        bat.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 32000, 0));
        return trySpawn(level, bat, x, y, z);
    }

    private static boolean spawnDeath(ServerLevel level, AuraNode node) {
        int fuzz = (int) (node.baseLevel / 4.0F) / 3;
        double x = node.x + fuzz(level, fuzz);
        double y = node.y + fuzz(level, fuzz);
        double z = node.z + fuzz(level, fuzz);
        if (!Utils.isChunkLoaded(level, x, z)) {
            return false;
        }
        Mob zombie = createMob(level, "brainy_zombie");
        if (zombie == null) {
            return false;
        }
        while (level.isEmptyBlock(BlockPos.containing(x, y - 2, z)) && y > 10.0) {
            y--;
        }
        zombie.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 32000, 0));
        return trySpawn(level, zombie, x, y, z);
    }

    private static boolean spawnEvil(ServerLevel level, AuraNode node) {
        int fuzz = (int) (node.baseLevel / 4.0F) / 3;
        double x = node.x + fuzz(level, fuzz);
        double y = node.y + fuzz(level, fuzz);
        double z = node.z + fuzz(level, fuzz);
        if (!Utils.isChunkLoaded(level, x, z)) {
            return false;
        }
        Mob witch = EntityTypes.WITCH.create(level, EntitySpawnReason.EVENT);
        if (witch == null) {
            return false;
        }
        while (level.isEmptyBlock(BlockPos.containing(x, y - 2, z)) && y > 10.0) {
            y--;
        }
        return trySpawn(level, witch, x, y + 0.5, z);
    }

    private static boolean spawnWisp(ServerLevel level, AuraNode node, Aspect aspect) {
        if (!Utils.isChunkLoaded(level, node.x, node.z)) {
            return false;
        }
        Mob wisp = createMob(level, "wisp");
        if (wisp == null) {
            return false;
        }
        if (wisp instanceof AspectTyped typed) {
            typed.setAspect(aspect);
        }
        wisp.playAmbientSound();
        return trySpawn(level, wisp, node.x, node.y, node.z);
    }

    public static @Nullable AuraNode getNode(int key) {
        return NODES.get(key);
    }

    public static @Nullable AuraNode getNodeCopy(int key) {
        AuraNode node = NODES.get(key);
        return node == null ? null : node.copy();
    }

    public static List<AuraNode> nodesInChunk(ResourceKey<Level> dimension, ChunkPos pos) {
        List<AuraNode> nodes = new ArrayList<>();
        List<Integer> keys = NODE_CHUNKS.get(new ChunkKey(dimension, pos.x(), pos.z()));
        if (keys != null) {
            for (Integer key : keys) {
                AuraNode node = NODES.get(key);
                if (node != null) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    public static void queueNodeChanges(int key, int levelMod, int baseMod, boolean toggleLock, @Nullable AspectList flux, float mx, float my, float mz) {
        AuraNode node = NODES.get(key);
        if (node == null) {
            return;
        }
        node.level = (short) (node.level + levelMod);
        node.baseLevel = (short) (node.baseLevel + baseMod);
        if (toggleLock) {
            node.locked = !node.locked;
        }
        if (node.level < 0) {
            node.level = 0;
        }
        if (node.baseLevel < 0) {
            node.baseLevel = 0;
        }
        if (flux != null) {
            for (Aspect aspect : flux.getAspects()) {
                int amount = flux.getAmount(aspect);
                if (amount > 0) {
                    node.flux.add(aspect, amount);
                } else {
                    node.flux.reduceAmount(aspect, -amount);
                }
            }
        }
        for (Aspect aspect : node.flux.getAspects()) {
            int amount = node.flux.getAmount(aspect);
            if (amount <= 0) {
                node.flux.removeAll(aspect);
            } else if (amount > 100) {
                node.flux.reduceAmount(aspect, amount - 100);
            }
        }
        boolean moved = mx != 0.0F || my != 0.0F || mz != 0.0F;
        if (moved) {
            unindexChunk(node);
        }
        node.x += mx;
        node.y += my;
        node.z += mz;
        if (moved) {
            updateNodeNeighbours(node);
            indexChunk(node);
        }
        MARKED_FOR_TRANSMISSION.put(node.key, 0L);
        AuraTicker.markDirty(node);
    }

    public static void deleteNode(int key) {
        AuraNode node = NODES.remove(key);
        if (node == null) {
            return;
        }
        NEIGHBOURS.remove(key);
        unindexChunk(node);
        LinkedList<Integer> list = UPDATE_LIST.get(node.dimension);
        if (list != null) {
            list.remove(Integer.valueOf(key));
        }
        AuraTicker.markDirty(node);
    }

    public static void unloadDimension(ResourceKey<Level> dimension) {
        NODES.values().removeIf(node -> {
            if (node.dimension == dimension) {
                NEIGHBOURS.remove(node.key);
                return true;
            }
            return false;
        });
        NODE_CHUNKS.keySet().removeIf(chunk -> chunk.dimension() == dimension);
        UPDATE_LIST.remove(dimension);
    }

    static void markChunkUnsaved(ServerLevel level, AuraNode node) {
        AuraTicker.markDirty(node);
    }

    public interface AspectTyped {
        void setAspect(Aspect aspect);
    }
}
