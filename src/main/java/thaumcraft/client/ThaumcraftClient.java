package thaumcraft.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thaumcraft.client.extensions.InfusedStoneClientExtensions;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.client.render.AlembicRenderer;
import thaumcraft.client.color.EssenceTint;
import thaumcraft.client.render.CrucibleRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.client.fx.ModRenderPipelines;
import thaumcraft.fx.Fx;
import thaumcraft.network.AspectTagsPayload;
import thaumcraft.network.BlockSparklePayload;
import thaumcraft.network.AuraDeletePayload;
import thaumcraft.network.AuraNodePayload;
import thaumcraft.network.AuraTransferFxPayload;
import thaumcraft.network.NodeZapPayload;
import thaumcraft.network.ResearchCompletePayload;
import thaumcraft.client.research.ResearchTexts;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@Mod(value = Thaumcraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ThaumcraftClient {
    public ThaumcraftClient(IEventBus modEventBus, ModContainer container) {
        Fx.set(new ClientFx());
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ModRenderPipelines.ADDITIVE_PARTICLE);
        event.registerPipeline(ModRenderPipelines.TRANSLUCENT_PARTICLE);
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        int[] colors = {0xFFFF7E, 0xFF5A01, 0x0090FF, 0x00A000, 0xAA33FC, 0xB0B0BC};
        event.register(List.of(BlockTintSources.constant(colors[0])), ModBlocks.AIR_INFUSED_STONE.get());
        event.register(List.of(BlockTintSources.constant(colors[1])), ModBlocks.FIRE_INFUSED_STONE.get());
        event.register(List.of(BlockTintSources.constant(colors[2])), ModBlocks.WATER_INFUSED_STONE.get());
        event.register(List.of(BlockTintSources.constant(colors[3])), ModBlocks.EARTH_INFUSED_STONE.get());
        event.register(List.of(BlockTintSources.constant(colors[4])), ModBlocks.VIS_INFUSED_STONE.get());
        event.register(List.of(BlockTintSources.constant(colors[5])), ModBlocks.DULL_INFUSED_STONE.get());
        event.register(List.of(BlockTintSources.foliage()), ModBlocks.GREATWOOD_LEAVES.get());
        event.register(List.of(BlockTintSources.constant(0x8899AA)), ModBlocks.SILVERWOOD_LEAVES.get());
    }

    @SubscribeEvent
    static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Thaumcraft.id("essence"), EssenceTint.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(
            new InfusedStoneClientExtensions(),
            ModBlocks.AIR_INFUSED_STONE.get(),
            ModBlocks.FIRE_INFUSED_STONE.get(),
            ModBlocks.WATER_INFUSED_STONE.get(),
            ModBlocks.EARTH_INFUSED_STONE.get(),
            ModBlocks.VIS_INFUSED_STONE.get()
        );
    }

    @SubscribeEvent
    static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(AuraNodePayload.TYPE, ClientPayloadHandlers::auraNode);
        event.register(AuraDeletePayload.TYPE, ClientPayloadHandlers::auraDelete);
        event.register(AuraTransferFxPayload.TYPE, ClientPayloadHandlers::auraTransferFx);
        event.register(NodeZapPayload.TYPE, ClientPayloadHandlers::nodeZap);
        event.register(AspectTagsPayload.TYPE, ClientPayloadHandlers::aspectTags);
        event.register(ResearchCompletePayload.TYPE, ClientPayloadHandlers::researchComplete);
        event.register(BlockSparklePayload.TYPE, ClientPayloadHandlers::blockSparkle);
    }

    @SubscribeEvent
    static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Thaumcraft.id("research_texts"), ResearchTexts.INSTANCE);
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        AuraClientData.clear();
    }
}
