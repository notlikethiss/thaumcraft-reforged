package thaumcraft.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ClientPayloadHandlers;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AuraNodePayload.TYPE, AuraNodePayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.auraNode(payload, context));
        registrar.playToClient(BoreDigPayload.TYPE, BoreDigPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.boreDig(payload, context));
        registrar.playToClient(AuraTransferFxPayload.TYPE, AuraTransferFxPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.auraTransferFx(payload, context));
        registrar.playToClient(AuraDeletePayload.TYPE, AuraDeletePayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.auraDelete(payload, context));
        registrar.playToClient(NodeZapPayload.TYPE, NodeZapPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.nodeZap(payload, context));
        registrar.playToClient(WispZapPayload.TYPE, WispZapPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.wispZap(payload, context));
        registrar.playToClient(LightningWandPayload.TYPE, LightningWandPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.lightningWand(payload, context));
        registrar.playToClient(AspectTagsPayload.TYPE, AspectTagsPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.aspectTags(payload, context));
        registrar.playToClient(ResearchCompletePayload.TYPE, ResearchCompletePayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.researchComplete(payload, context));
        registrar.playToClient(BlockSparklePayload.TYPE, BlockSparklePayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.blockSparkle(payload, context));
        registrar.playToClient(BlockBoilPayload.TYPE, BlockBoilPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.blockBoil(payload, context));
        registrar.playToClient(BlockTagsPayload.TYPE, BlockTagsPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.blockTags(payload, context));
        registrar.playToClient(KnowledgeSyncPayload.TYPE, KnowledgeSyncPayload.STREAM_CODEC, (payload, context) -> ClientPayloadHandlers.knowledgeSync(payload, context));
        registrar.playToServer(HoverTogglePayload.TYPE, HoverTogglePayload.STREAM_CODEC, HoverTogglePayload::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    public static void sendToNear(ServerLevel level, ServerPlayer excluded, double x, double y, double z, double radius, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersNear(level, excluded, x, y, z, radius, payload);
    }

    public static void sendToTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload);
    }

    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
