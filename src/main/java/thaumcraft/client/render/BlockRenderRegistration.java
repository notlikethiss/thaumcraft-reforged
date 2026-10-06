package thaumcraft.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModItems;

@EventBusSubscriber(modid = Thaumcraft.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BlockRenderRegistration {
    private static TcItemRenderer itemRenderer;

    private BlockRenderRegistration() {
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
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_BORE.get(), ArcaneBoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_BORE_BASE.get(), ArcaneBoreBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CLUSTER.get(), CrystalClusterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CORE.get(), CrystalCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_CAPACITOR.get(), CrystalCapacitorRenderer::new);
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (itemRenderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    itemRenderer = new TcItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return itemRenderer;
            }
        };
        event.registerItem(
            extensions,
            ModItems.WARDED_JAR.get(),
            ModItems.BRAIN_JAR.get(),
            ModItems.FILLED_JAR.get(),
            ModItems.ARCANE_BELLOWS.get(),
            ModItems.HUNGRY_CHEST.get(),
            ModItems.ARCANE_BORE_BASE.get(),
            ModItems.ARCANE_BORE.get(),
            ModItems.CRYSTAL_CORE.get(),
            ModItems.CRYSTAL_CAPACITOR.get(),
            ModItems.AIR_CRYSTAL_CLUSTER.get(),
            ModItems.FIRE_CRYSTAL_CLUSTER.get(),
            ModItems.WATER_CRYSTAL_CLUSTER.get(),
            ModItems.EARTH_CRYSTAL_CLUSTER.get(),
            ModItems.VIS_CRYSTAL_CLUSTER.get(),
            ModItems.MIXED_CRYSTAL_CLUSTER.get()
        );
    }
}
