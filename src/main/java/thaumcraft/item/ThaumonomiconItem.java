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
import thaumcraft.Config;
import thaumcraft.registry.ModAttachments;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchClientHooks;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchManager;

public class ThaumonomiconItem extends Item {
    private final boolean cheatSheet;

    public ThaumonomiconItem(boolean cheatSheet, Properties properties) {
        super(properties);
        this.cheatSheet = cheatSheet;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (cheatSheet && Config.ALLOW_CHEAT_SHEET.getAsBoolean()) {
                PlayerKnowledge knowledge = ResearchManager.knowledge(player);
                for (ResearchItem research : ResearchList.RESEARCH.values()) {
                    knowledge.complete(research.key);
                }
                player.setData(ModAttachments.KNOWLEDGE, knowledge);
            }
        } else {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.PAGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            ResearchClientHooks.openBook();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (cheatSheet) {
            builder.accept(Component.translatable("tc.thaumcraft.cheat_sheet"));
        }
    }
}
