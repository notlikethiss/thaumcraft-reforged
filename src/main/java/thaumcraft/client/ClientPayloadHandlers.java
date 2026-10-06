package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.fx.Fx;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.network.AspectTagsPayload;
import thaumcraft.network.KnowledgeSyncPayload;
import thaumcraft.registry.ModAttachments;
import thaumcraft.network.BlockBoilPayload;
import thaumcraft.network.BlockSparklePayload;
import thaumcraft.network.BlockTagsPayload;
import thaumcraft.network.BoreDigPayload;
import thaumcraft.network.AuraDeletePayload;
import thaumcraft.network.AuraNodePayload;
import thaumcraft.network.AuraTransferFxPayload;
import thaumcraft.network.LightningWandPayload;
import thaumcraft.network.NodeZapPayload;
import thaumcraft.network.WispZapPayload;
import thaumcraft.network.ResearchCompletePayload;
import thaumcraft.client.research.ResearchToast;
import thaumcraft.registry.ModItems;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void auraNode(AuraNodePayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            AuraClientData.update(payload, minecraft.level.dimension());
        }
    }

    public static void aspectTags(AspectTagsPayload payload, IPayloadContext context) {
        AspectRegistry.setActive(payload.tags());
    }

    public static void auraDelete(AuraDeletePayload payload, IPayloadContext context) {
        AuraClientData.remove(payload.key());
    }

    public static void auraTransferFx(AuraTransferFxPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GOGGLES_OF_REVEALING.get())) {
            Fx.get().auraTransfer(
                player.level(),
                payload.from().x(),
                payload.from().y(),
                payload.from().z(),
                payload.to().x(),
                payload.to().y(),
                payload.to().z()
            );
        }
    }

    public static void wispZap(WispZapPayload payload, IPayloadContext context) {
        Player player = context.player();
        Entity source = player.level().getEntity(payload.sourceId());
        Entity target = player.level().getEntity(payload.targetId());
        if (source != null && target != null) {
            Fx.get().bolt(player.level(), source, target);
        }
    }

    public static void lightningWand(LightningWandPayload payload, IPayloadContext context) {
        Player player = context.player();
        Entity shooter = player.level().getEntity(payload.playerId());
        if (shooter != null) {
            Fx.get().wandLightning(
                player.level(), shooter, payload.endX(), payload.endY(), payload.endZ(),
                payload.hasBlock(), payload.blockX(), payload.blockY(), payload.blockZ(), payload.hasEntity()
            );
        }
    }

    public static void nodeZap(NodeZapPayload payload, IPayloadContext context) {
        Player player = context.player();
        Entity target = player.level().getEntity(payload.entityId());
        if (target != null) {
            Fx.get().nodeBolt(player.level(), payload.from().x(), payload.from().y(), payload.from().z(), target);
        }
    }

    public static void blockSparkle(BlockSparklePayload payload, IPayloadContext context) {
        Player player = context.player();
        Fx.get().blockSparkle(player.level(), payload.pos().getX(), payload.pos().getY(), payload.pos().getZ(), payload.color(), payload.count());
    }

    public static void blockBoil(BlockBoilPayload payload, IPayloadContext context) {
        Player player = context.player();
        Fx.get().blockBoil(player.level(), payload.pos().getX(), payload.pos().getY(), payload.pos().getZ(), payload.red(), payload.green(), payload.blue());
    }

    public static void blockTags(BlockTagsPayload payload, IPayloadContext context) {
        Player player = context.player();
        Fx.get().blockSparkle(player.level(), payload.pos().getX(), payload.pos().getY(), payload.pos().getZ(), 4, 4);
        Fx.get().blockTags(payload.pos(), payload.aspects(), payload.side());
    }

    public static void boreDig(BoreDigPayload payload, IPayloadContext context) {
        if (context.player().level().getBlockEntity(payload.pos()) instanceof ArcaneBoreBlockEntity bore) {
            bore.receiveDig(payload.target());
        }
    }

    public static void researchComplete(ResearchCompletePayload payload, IPayloadContext context) {
        Minecraft.getInstance().getToasts().addToast(new ResearchToast(payload.key()));
    }

    public static void knowledgeSync(KnowledgeSyncPayload payload, IPayloadContext context) {
        context.player().setData(ModAttachments.KNOWLEDGE, payload.knowledge());
    }
}
