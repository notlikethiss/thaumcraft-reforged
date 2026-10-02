package thaumcraft.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.network.ResearchCompletePayload;
import thaumcraft.registry.ModAttachments;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ResearchManager {
    private ResearchManager() {
    }

    public static PlayerKnowledge knowledge(Player player) {
        return player.getData(ModAttachments.KNOWLEDGE);
    }

    public static boolean isResearchComplete(Player player, String key) {
        if (ResearchList.getResearch(key) == null) {
            return true;
        }
        return knowledge(player).isComplete(key);
    }

    public static boolean doesPlayerHaveRequisites(Player player, String key) {
        ResearchItem research = ResearchList.getResearch(key);
        if (research == null) {
            return true;
        }
        PlayerKnowledge knowledge = knowledge(player);
        boolean result = true;
        for (ResearchItem[] parents : new ResearchItem[][]{research.parents, research.parentsHidden}) {
            if (parents == null || parents.length == 0) {
                continue;
            }
            result = false;
            if (!knowledge.research().isEmpty()) {
                result = true;
                for (ResearchItem parent : parents) {
                    if (!knowledge.isComplete(parent.key)) {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    public static void completeResearch(Player player, String key) {
        PlayerKnowledge knowledge = knowledge(player);
        if (knowledge.complete(key)) {
            player.setData(ModAttachments.KNOWLEDGE, knowledge);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new ResearchCompletePayload(key));
            }
        }
    }

    public static @Nullable String findLostResearch(Player player) {
        List<String> choices = new ArrayList<>();
        for (ResearchItem research : ResearchList.RESEARCH.values()) {
            if (!research.getStub()
                && !research.getAlternate()
                && research.getLost()
                && !isResearchComplete(player, research.key)
                && doesPlayerHaveRequisites(player, research.key)) {
                choices.add(research.key);
            }
        }
        return choices.isEmpty() ? null : choices.get(player.getRandom().nextInt(choices.size()));
    }

    public static @Nullable String findMatchingResearch(Player player, Aspect[] tags, int[] amounts) {
        int bestMatch = 0;
        String best = null;
        for (ResearchItem research : ResearchList.RESEARCH.values()) {
            if (research.getStub() || research.getAlternate() || research.getLost()
                || isResearchComplete(player, research.key)
                || !doesPlayerHaveRequisites(player, research.key)) {
                continue;
            }
            int match = 0;
            for (int i = 0; i < 5 && i < tags.length; i++) {
                if (tags[i] != null && amounts[i] > 0 && research.tags.getAmount(tags[i]) > 0) {
                    match++;
                }
            }
            if (match > 0 && match > bestMatch) {
                bestMatch = match;
                best = research.key;
            }
        }
        return best;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerKnowledge knowledge = knowledge(event.getEntity());
        boolean changed = false;
        for (ResearchItem research : ResearchList.RESEARCH.values()) {
            if (research.getAutoUnlock()) {
                changed |= knowledge.complete(research.key);
            }
        }
        if (changed) {
            event.getEntity().setData(ModAttachments.KNOWLEDGE, knowledge);
        }
    }
}
