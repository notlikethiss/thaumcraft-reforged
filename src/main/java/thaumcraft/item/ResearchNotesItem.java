package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchClientHooks;
import thaumcraft.research.ResearchNoteData;

public class ResearchNotesItem extends Item {
    public ResearchNotesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResearchNoteData data = ResearchManager.getData(stack);
        if (data.key == null || data.getTotalProgress() != 1.0F || ResearchManager.isResearchComplete(player, data.key)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (ResearchManager.doesPlayerHaveRequisites(player, data.key)) {
                ResearchManager.completeResearch(player, data.key);
                ResearchItem research = ResearchList.getResearch(data.key);
                if (research != null && research.siblings != null) {
                    for (ResearchItem sibling : research.siblings) {
                        if (!ResearchManager.isResearchComplete(player, sibling.key)
                            && ResearchManager.doesPlayerHaveRequisites(player, sibling.key)) {
                            ResearchManager.completeResearch(player, sibling.key);
                        }
                    }
                }
                stack.shrink(1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            } else {
                player.sendSystemMessage(Component.translatable("tc.thaumcraft.discoveryerror"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        ResearchNoteData data = ResearchManager.getData(stack);
        float progress = data.getTotalProgress();
        if (progress >= 0.2F && data.key != null) {
            builder.accept(Component.literal(ResearchClientHooks.name(data.key)));
        } else {
            builder.accept(Component.translatable("tc.thaumcraft.discoveryunknown"));
        }
        if (stack.is(ModItems.RESEARCH_NOTES.get()) && progress > 0.333332F) {
            builder.accept(Component.literal(String.valueOf((int) (progress * 100.0F))).append(Component.translatable("tc.thaumcraft.discoveryprogress")));
        }
    }
}
