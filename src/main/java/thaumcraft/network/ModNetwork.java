package thaumcraft.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AuraNodePayload.TYPE, AuraNodePayload.STREAM_CODEC);
        registrar.playToClient(BoreDigPayload.TYPE, BoreDigPayload.STREAM_CODEC);
        registrar.playToClient(AuraTransferFxPayload.TYPE, AuraTransferFxPayload.STREAM_CODEC);
        registrar.playToClient(AuraDeletePayload.TYPE, AuraDeletePayload.STREAM_CODEC);
        registrar.playToClient(NodeZapPayload.TYPE, NodeZapPayload.STREAM_CODEC);
        registrar.playToClient(WispZapPayload.TYPE, WispZapPayload.STREAM_CODEC);
        registrar.playToClient(LightningWandPayload.TYPE, LightningWandPayload.STREAM_CODEC);
        registrar.playToClient(AspectTagsPayload.TYPE, AspectTagsPayload.STREAM_CODEC);
        registrar.playToClient(ResearchCompletePayload.TYPE, ResearchCompletePayload.STREAM_CODEC);
        registrar.playToClient(BlockSparklePayload.TYPE, BlockSparklePayload.STREAM_CODEC);
        registrar.playToClient(BlockBoilPayload.TYPE, BlockBoilPayload.STREAM_CODEC);
        registrar.playToClient(BlockTagsPayload.TYPE, BlockTagsPayload.STREAM_CODEC);
        registrar.playToServer(HoverTogglePayload.TYPE, HoverTogglePayload.STREAM_CODEC, HoverTogglePayload::handle);
    }
}
