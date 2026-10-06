package thaumcraft.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.Thaumcraft;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.color.EssenceTint;
import thaumcraft.client.color.ResearchNoteTint;
import thaumcraft.client.extensions.InfusedStoneClientExtensions;
import thaumcraft.client.extensions.JarClientExtensions;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.client.gui.TcHud;
import thaumcraft.client.render.AlembicRenderer;
import thaumcraft.client.render.ArcaneBoreBaseRenderer;
import thaumcraft.client.render.ArcaneBoreRenderer;
import thaumcraft.client.render.BellowsRenderer;
import thaumcraft.client.render.CrucibleRenderer;
import thaumcraft.client.render.CrystalCapacitorRenderer;
import thaumcraft.client.render.CrystalClusterRenderer;
import thaumcraft.client.render.CrystalCoreRenderer;
import thaumcraft.client.render.HoleRenderer;
import thaumcraft.client.render.HoverHarnessLayer;
import thaumcraft.client.render.HungryChestRenderer;
import thaumcraft.client.render.JarRenderer;
import thaumcraft.client.render.MirrorRenderer;
import thaumcraft.client.render.WorkbenchWandRenderer;
import thaumcraft.client.render.entity.BrainyZombieRenderer;
import thaumcraft.client.render.entity.DartRenderer;
import thaumcraft.client.render.entity.FireBatRenderer;
import thaumcraft.client.render.entity.FrostShardRenderer;
import thaumcraft.client.render.entity.GiantBrainyZombieRenderer;
import thaumcraft.client.render.entity.GolemRenderer;
import thaumcraft.client.render.entity.SpecialItemRenderer;
import thaumcraft.client.render.entity.WispRenderer;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.client.research.ResearchBookScreen;
import thaumcraft.client.research.ResearchTexts;
import thaumcraft.client.screen.ArcaneBoreScreen;
import thaumcraft.client.screen.ArcaneWorkbenchScreen;
import thaumcraft.client.screen.GolemScreen;
import thaumcraft.client.screen.HandMirrorScreen;
import thaumcraft.client.screen.HoverHarnessScreen;
import thaumcraft.client.screen.InfusionWorkbenchScreen;
import thaumcraft.client.screen.ResearchTableScreen;
import thaumcraft.crafting.ConfigRecipes;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKind;
import thaumcraft.fx.Fx;
import thaumcraft.item.HandMirrorItem;
import thaumcraft.item.golem.GolemPlacerItem;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModMenus;
import thaumcraft.research.ResearchClientHooks;

@Mod(value = Thaumcraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ThaumcraftClient {
    private static final int[] WOOL_TINTS = {
        0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
        0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B,
    };
    private static final int[] LEVITATOR_TINTS = {0x00A000, 0xFFFF7E, 0xAA33FC};
    private static final String[] SHARD_NAMES = {"air_shard", "fire_shard", "water_shard", "earth_shard", "vis_shard", "dull_shard"};
    private static final int[] SHARD_TINTS = {0xFFFF7E, 0xFF5A01, 0x0090FF, 0x00A000, 0xAA33FC, 0xB0B0BC};

    public ThaumcraftClient(IEventBus modEventBus, ModContainer container) {
        Fx.set(new ClientFx());
        ResearchClientHooks.set(ResearchTexts::name, ResearchBookScreen::open);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ThaumcraftClient::registerItemProperties);
    }

    private static void registerItemProperties() {
        for (DeferredItem<GolemPlacerItem> golem : ModItems.GOLEMS.values()) {
            ItemProperties.register(golem.get(), Thaumcraft.id("golem_core"), (stack, level, entity, seed) -> GolemPlacerItem.getCore(stack));
        }
        ItemProperties.register(ModItems.MAGIC_MIRROR.get(), Thaumcraft.id("linked"), (stack, level, entity, seed) -> HandMirrorItem.hasLink(stack) ? 1.0F : 0.0F);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Thaumcraft.id("hud"), TcHud::render);
    }

    private static BlockColor constantBlock(int color) {
        return (state, level, pos, tintIndex) -> color;
    }

    private static ItemColor constantItem(int color) {
        return (stack, tintIndex) -> tintIndex == 0 ? FastColor.ARGB32.opaque(color) : -1;
    }

    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        int[] colors = {0xFFFF7E, 0xFF5A01, 0x0090FF, 0x00A000, 0xAA33FC, 0xB0B0BC};
        event.register(constantBlock(colors[0]), ModBlocks.AIR_INFUSED_STONE.get());
        event.register(constantBlock(colors[1]), ModBlocks.FIRE_INFUSED_STONE.get());
        event.register(constantBlock(colors[2]), ModBlocks.WATER_INFUSED_STONE.get());
        event.register(constantBlock(colors[3]), ModBlocks.EARTH_INFUSED_STONE.get());
        event.register(constantBlock(colors[4]), ModBlocks.VIS_INFUSED_STONE.get());
        event.register(constantBlock(colors[5]), ModBlocks.DULL_INFUSED_STONE.get());
        event.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor(),
            ModBlocks.GREATWOOD_LEAVES.get()
        );
        event.register(constantBlock(0x8899AA), ModBlocks.SILVERWOOD_LEAVES.get());
        event.register(constantBlock(0xC000C0), ModBlocks.ARCANE_STONE.get());
        event.register(
            (state, level, pos, tintIndex) -> tintIndex >= 0 && tintIndex < LEVITATOR_TINTS.length ? LEVITATOR_TINTS[tintIndex] : -1,
            ModBlocks.ARCANE_LEVITATOR.get()
        );
        for (int index = 0; index < ConfigRecipes.WOOL_COLORS.length; index++) {
            String color = ConfigRecipes.WOOL_COLORS[index];
            event.register(
                constantBlock(WOOL_TINTS[index]),
                ModBlocks.CANDLES.get(color).get(),
                ModBlocks.MARKERS.get(color).get(),
                ModBlocks.WARDED_STONES.get(color).get()
            );
        }
    }

    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        int[] colors = {0xFFFF7E, 0xFF5A01, 0x0090FF, 0x00A000, 0xAA33FC, 0xB0B0BC};
        event.register(constantItem(colors[0]), ModBlocks.AIR_INFUSED_STONE.get());
        event.register(constantItem(colors[1]), ModBlocks.FIRE_INFUSED_STONE.get());
        event.register(constantItem(colors[2]), ModBlocks.WATER_INFUSED_STONE.get());
        event.register(constantItem(colors[3]), ModBlocks.EARTH_INFUSED_STONE.get());
        event.register(constantItem(colors[4]), ModBlocks.VIS_INFUSED_STONE.get());
        event.register(constantItem(colors[5]), ModBlocks.DULL_INFUSED_STONE.get());
        event.register(constantItem(0x48B518), ModBlocks.GREATWOOD_LEAVES.get());
        event.register(constantItem(0x8899AA), ModBlocks.SILVERWOOD_LEAVES.get());
        event.register(
            (stack, tintIndex) -> tintIndex >= 0 && tintIndex < LEVITATOR_TINTS.length ? FastColor.ARGB32.opaque(LEVITATOR_TINTS[tintIndex]) : -1,
            ModBlocks.ARCANE_LEVITATOR.get()
        );
        for (int index = 0; index < ConfigRecipes.WOOL_COLORS.length; index++) {
            String color = ConfigRecipes.WOOL_COLORS[index];
            event.register(
                constantItem(WOOL_TINTS[index]),
                ModBlocks.CANDLES.get(color).get(),
                ModBlocks.MARKERS.get(color).get(),
                ModBlocks.WARDED_STONES.get(color).get()
            );
        }
        for (int index = 0; index < SHARD_NAMES.length; index++) {
            Item shard = BuiltInRegistries.ITEM.get(Thaumcraft.id(SHARD_NAMES[index]));
            event.register(constantItem(SHARD_TINTS[index]), shard);
        }
        event.register(new ResearchNoteTint(1), ModItems.RESEARCH_NOTES.get(), ModItems.DISCOVERY.get());
        event.register(new EssenceTint(1), ModItems.ESSENCE.get());
        event.register(new EssenceTint(0), ModItems.WISP_ESSENCE.get());
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
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
        event.registerEntityRenderer(ModEntities.BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.GIANT_BRAINY_ZOMBIE.get(), GiantBrainyZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_BAT.get(), FireBatRenderer::new);
        event.registerEntityRenderer(ModEntities.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(ModEntities.ALUMENTUM.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.DART.get(), DartRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_SHARD.get(), FrostShardRenderer::new);
        event.registerEntityRenderer(ModEntities.SPECIAL_ITEM.get(), context -> new SpecialItemRenderer(context, true));
        event.registerEntityRenderer(ModEntities.FOLLOWING_ITEM.get(), context -> new SpecialItemRenderer(context, false));
        for (GolemKind kind : GolemKind.values()) {
            EntityType<GolemBase> type = (EntityType<GolemBase>) kind.entityType();
            event.registerEntityRenderer(type, context -> new GolemRenderer(context, kind.advanced()));
        }
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<? extends Player> renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new HoverHarnessLayer(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        TcModelLayers.register(event);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(ModMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
        event.register(ModMenus.GOLEM.get(), GolemScreen::new);
        event.register(ModMenus.HOVER_HARNESS.get(), HoverHarnessScreen::new);
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
    static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ResearchTexts.INSTANCE);
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        AuraClientData.clear();
    }
}
