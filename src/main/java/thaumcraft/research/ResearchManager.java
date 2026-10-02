package thaumcraft.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.ThaumcraftRecipes;
import thaumcraft.network.ResearchCompletePayload;
import thaumcraft.registry.ModAttachments;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModItems;

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

    public static boolean isCrucibleCreationSuccessful(Level level, ItemStack output, Player player) {
        CrucibleRecipe recipe = ThaumcraftRecipes.getCrucibleRecipe(output);
        if (recipe == null || isResearchComplete(player, recipe.key())) {
            return true;
        }
        String key = recipe.key();
        if (!doesPlayerHaveRequisites(player, key)) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int slot = getResearchSlot(player, key);
        ItemStack note = slot >= 0 ? inventory.getItem(slot) : createNoteFromSupplies(player, key);
        float chance = 0.0F;
        if (note == null) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("tc.thaumcraft.cantrecord"));
            }
            return false;
        }
        if (progressExperimentalResearch(level, key, note, Config.RESEARCH_EXP_CHANCE.getAsInt() + (int) Math.sqrt(output.getCount() * 8))) {
            chance = getData(note).getTotalProgress();
            if (chance == 1.0F) {
                note = toDiscovery(note);
            }
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("tc.thaumcraft.research_learned"));
            }
        }
        if (slot >= 0) {
            inventory.setItem(slot, note);
        } else if (!inventory.add(note)) {
            player.drop(note, false, Prediction.SERVER_ONLY);
        }
        player.inventoryMenu.broadcastChanges();
        return level.getRandom().nextFloat() < chance;
    }

    private static @Nullable ItemStack createNoteFromSupplies(Player player, String key) {
        if (consumeInk(player, false) && consumePaper(player)) {
            consumeInk(player, true);
            return createNote(new ItemStack(ModItems.RESEARCH_NOTES.get()), key);
        }
        return null;
    }

    private static boolean consumePaper(Player player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    public static boolean consumeInk(Player player, boolean doIt) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.SCRIBING_TOOLS.get()) && stack.getDamageValue() < stack.getMaxDamage()) {
                if (doIt && !player.hasInfiniteMaterials()) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
                return true;
            }
        }
        return false;
    }

    public static int getResearchSlot(Player player, String key) {
        List<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.is(ModItems.RESEARCH_NOTES.get()) && key.equals(getData(stack).key)) {
                return slot;
            }
        }
        return -1;
    }

    public static ItemStack createNote(ItemStack stack, String key) {
        if (!stack.has(ModDataComponents.RESEARCH_NOTE.get())) {
            stack.set(ModDataComponents.RESEARCH_NOTE.get(), ResearchNoteData.create(key));
        }
        return stack;
    }

    public static ResearchNoteData getData(ItemStack stack) {
        return ResearchNoteData.read(stack);
    }

    public static void updateData(ItemStack stack, ResearchNoteData data) {
        if (stack.has(ModDataComponents.RESEARCH_NOTE.get())) {
            stack.set(ModDataComponents.RESEARCH_NOTE.get(), data.toNote());
        }
    }

    public static ItemStack toDiscovery(ItemStack note) {
        return note.transmuteCopy(ModItems.DISCOVERY.get());
    }

    public static boolean progressExperimentalResearch(Level level, String key, ItemStack note, int baseChance) {
        boolean progressed = false;
        ResearchNoteData data = getData(note);
        ResearchItem research = ResearchList.getResearch(key);
        if (research != null) {
            for (Aspect tag : research.aspectOrder()) {
                if (level.getRandom().nextInt(100) <= baseChance) {
                    for (int q = 0; q < data.tags.length; q++) {
                        if (data.tags[q] == tag && data.progress[q] < research.tags.getAmount(tag)) {
                            data.progress[q]++;
                            progressed = true;
                            baseChance = (int) (baseChance * 0.9F);
                            break;
                        }
                    }
                }
            }
        }
        updateData(note, data);
        return progressed;
    }

    public static void completeResearch(Player player, String key) {
        completeResearch(player, key, true);
    }

    public static void completeResearch(Player player, String key, boolean notify) {
        PlayerKnowledge knowledge = knowledge(player);
        if (knowledge.complete(key)) {
            player.setData(ModAttachments.KNOWLEDGE, knowledge);
            if (notify && player instanceof ServerPlayer serverPlayer) {
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
