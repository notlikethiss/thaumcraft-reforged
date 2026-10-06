package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.HoverHarnessLayer;
import thaumcraft.client.render.model.TcModelLayers;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKind;
import thaumcraft.registry.ModEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EntityRenderRegistration {
    private EntityRenderRegistration() {
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
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
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        TcModelLayers.register(event);
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new HoverHarnessLayer(renderer));
            }
        }
    }
}
