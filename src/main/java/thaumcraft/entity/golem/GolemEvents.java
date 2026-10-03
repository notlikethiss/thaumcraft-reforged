package thaumcraft.entity.golem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class GolemEvents {
    private GolemEvents() {
    }

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof IronGuardianGolem golem && golem.getOwnerId() != null && !golem.isOwner(event.getEntity())) {
            if (!event.getLevel().isClientSide()) {
                event.getEntity().sendSystemMessage(Component.translatable("tc.thaumcraft.golem.not_master"));
            }
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
