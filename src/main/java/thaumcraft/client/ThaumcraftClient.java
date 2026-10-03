package thaumcraft.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import thaumcraft.crafting.ConfigRecipes;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thaumcraft.client.extensions.InfusedStoneClientExtensions;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.client.render.AlembicRenderer;
import thaumcraft.client.color.EssenceTint;
import thaumcraft.client.color.ResearchNoteTint;
import thaumcraft.research.ResearchClientHooks;
import thaumcraft.client.research.ResearchBookScreen;
import thaumcraft.client.render.CrucibleRenderer;
import thaumcraft.client.render.BellowsRenderer;
import thaumcraft.client.render.ArcaneBoreBaseRenderer;
import thaumcraft.client.render.ArcaneBoreRenderer;
import thaumcraft.client.render.BoreSpecialRenderer;
import thaumcraft.client.render.CrystalCapacitorRenderer;
import thaumcraft.client.render.CrystalClusterRenderer;
import thaumcraft.client.render.CrystalCoreRenderer;
import thaumcraft.client.render.CrystalSpecialRenderer;
import thaumcraft.client.render.HoleRenderer;
import thaumcraft.client.render.HungryChestRenderer;
import thaumcraft.client.render.MirrorRenderer;
import thaumcraft.client.screen.ArcaneBoreScreen;
import thaumcraft.client.screen.HandMirrorScreen;
import thaumcraft.client.render.HungryChestSpecialRenderer;
import thaumcraft.client.render.BellowsSpecialRenderer;
import thaumcraft.client.render.JarRenderer;
import thaumcraft.client.render.JarSpecialRenderer;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.client.extensions.JarClientExtensions;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import thaumcraft.client.render.WorkbenchWandRenderer;
import thaumcraft.client.screen.ArcaneWorkbenchScreen;
import thaumcraft.client.screen.InfusionWorkbenchScreen;
import thaumcraft.client.screen.ResearchTableScreen;
import thaumcraft.registry.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumcraft.client.gui.TcHud;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.client.fx.ModRenderPipelines;
import thaumcraft.fx.Fx;
import thaumcraft.network.AspectTagsPayload;
import thaumcraft.network.BlockSparklePayload;
import thaumcraft.network.BoreDigPayload;
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
    private static final int[] WOOL_TINTS = {
        0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
        0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B,
    };

    public ThaumcraftClient(IEventBus modEventBus, ModContainer container) {
        Fx.set(new ClientFx());
        ResearchClientHooks.set(ResearchTexts::name, ResearchBookScreen::open);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ModRenderPipelines.ADDITIVE_PARTICLE);
        event.registerPipeline(ModRenderPipelines.TRANSLUCENT_PARTICLE);
        event.registerPipeline(ModRenderPipelines.GUI_ADDITIVE);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Thaumcraft.id("hud"), TcHud::render);
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
        event.register(List.of(BlockTintSources.constant(0xC000C0)), ModBlocks.ARCANE_STONE.get());
        event.register(
            List.of(BlockTintSources.constant(0x00A000), BlockTintSources.constant(0xFFFF7E), BlockTintSources.constant(0xAA33FC)),
            ModBlocks.ARCANE_LEVITATOR.get()
        );
        for (int index = 0; index < ConfigRecipes.WOOL_COLORS.length; index++) {
            List<BlockTintSource> tint = List.of(BlockTintSources.constant(WOOL_TINTS[index]));
            String color = ConfigRecipes.WOOL_COLORS[index];
            event.register(tint, ModBlocks.CANDLES.get(color).get(), ModBlocks.MARKERS.get(color).get(), ModBlocks.WARDED_STONES.get(color).get());
        }
    }

    @SubscribeEvent
    static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Thaumcraft.id("essence"), EssenceTint.MAP_CODEC);
        event.register(Thaumcraft.id("research_note"), ResearchNoteTint.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_WORKTABLE.get(), context -> new WorkbenchWandRenderer<>(context, false));
        event.registerBlockEntityRenderer(ModBlockEntities.INFUSION_WORKBENCH.get(), context -> new WorkbenchWandRenderer<>(context, true));
        event.registerBlockEntityRenderer(ModBlockEntities.WARDED_JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BRAIN_JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BELLOWS.get(), BellowsRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HUNGRY_CHEST.get(), HungryChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIRROR.get(), MirrorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HOLE.get(), HoleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_BORE.get(), ArcaneBoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_BORE_BASE.get(), ArcaneBoreBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CLUSTER.get(), CrystalClusterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CORE.get(), CrystalCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CAPACITOR.get(), CrystalCapacitorRenderer::new);
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        TcModelLayers.register(event);
    }

    @SubscribeEvent
    static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Thaumcraft.id("jar"), JarSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(Thaumcraft.id("bellows"), BellowsSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(Thaumcraft.id("hungry_chest"), HungryChestSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(Thaumcraft.id("crystal"), CrystalSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(Thaumcraft.id("bore"), BoreSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(ModMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
        event.register(ModMenus.ARCANE_BORE.get(), ArcaneBoreScreen::new);
        event.register(ModMenus.INFUSION_WORKBENCH.get(), InfusionWorkbenchScreen::new);
        event.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
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
        event.registerBlock(new JarClientExtensions(), ModBlocks.WARDED_JAR.get(), ModBlocks.BRAIN_JAR.get());
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
        event.register(BoreDigPayload.TYPE, ClientPayloadHandlers::boreDig);
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
