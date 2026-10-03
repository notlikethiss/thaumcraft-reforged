package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.blockentity.MirrorBlockEntity;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModSounds;

public class MirrorItem extends BlockItem {
    public MirrorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(context.getClickedPos()) instanceof MirrorBlockEntity mirror)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!mirror.isLinkValid()) {
            ItemStack linked = stack.copyWithCount(1);
            linked.set(ModDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), context.getClickedPos()));
            level.playSound(null, context.getClickedPos(), ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
            stack.consume(1, player);
            if (!player.getInventory().add(linked)) {
                player.drop(linked, false, Prediction.SERVER_ONLY);
            }
        } else {
            player.sendSystemMessage(Component.translatable("tc.thaumcraft.mirror_already_linked").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
        return InteractionResult.SUCCESS;
    }

    public static Component linkText(String key, GlobalPos link) {
        return Component.translatable(key, link.pos().getX(), link.pos().getY(), link.pos().getZ(), link.dimension().identifier().toString());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        GlobalPos link = stack.get(ModDataComponents.MIRROR_LINK.get());
        if (link != null) {
            builder.accept(linkText("tc.thaumcraft.mirror_linked_to", link));
        }
    }
}
