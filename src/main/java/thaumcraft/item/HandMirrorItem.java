package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import thaumcraft.aura.AuraManager;
import thaumcraft.blockentity.MirrorBlockEntity;
import thaumcraft.menu.HandMirrorMenu;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModSounds;

public class HandMirrorItem extends Item {
    public HandMirrorItem(Properties properties) {
        super(properties);
    }

    private static Component message(String key) {
        return Component.translatable("tc.thaumcraft." + key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null || !(level.getBlockEntity(pos) instanceof MirrorBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            stack.set(ModDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
            level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
            player.sendSystemMessage(message("handmirrorlinked"));
        }
        return InteractionResult.SUCCESS;
    }

    private static @Nullable ServerLevel targetLevel(Level level, GlobalPos link) {
        return level instanceof ServerLevel serverLevel ? serverLevel.getServer().getLevel(link.dimension()) : null;
    }

    private static void breakLink(ItemStack mirror, Player player, Level level) {
        mirror.remove(ModDataComponents.MIRROR_LINK.get());
        level.playSound(null, player.blockPosition(), ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(message("handmirrorerror"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        GlobalPos link = stack.get(ModDataComponents.MIRROR_LINK.get());
        if (level.isClientSide() || link == null) {
            return super.use(level, player, hand);
        }
        ServerLevel targetLevel = targetLevel(level, link);
        if (targetLevel == null) {
            return super.use(level, player, hand);
        }
        if (!(targetLevel.getBlockEntity(link.pos()) instanceof MirrorBlockEntity)) {
            breakLink(stack, player, level);
            return super.use(level, player, hand);
        }
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new HandMirrorMenu(id, inventory, hand), stack.getHoverName()));
        return InteractionResult.SUCCESS;
    }

    public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level level) {
        GlobalPos link = mirror.get(ModDataComponents.MIRROR_LINK.get());
        if (link == null) {
            return false;
        }
        ServerLevel targetLevel = targetLevel(level, link);
        if (targetLevel == null) {
            return false;
        }
        if (!(targetLevel.getBlockEntity(link.pos()) instanceof MirrorBlockEntity)) {
            breakLink(mirror, player, level);
            return false;
        }
        int cost = Math.max(1, items.getCount() / 8);
        if (!AuraManager.decreaseClosestAura(level, player.getX(), player.getY(), player.getZ(), cost, false)) {
            return false;
        }
        AuraManager.decreaseClosestAura(level, player.getX(), player.getY(), player.getZ(), cost);
        BlockState targetState = targetLevel.getBlockState(link.pos());
        Direction facing = targetState.hasProperty(DirectionalBlock.FACING) ? targetState.getValue(DirectionalBlock.FACING) : Direction.UP;
        MirrorBlockEntity.spawnAt(targetLevel, link.pos(), facing, items.copy());
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1F, 1.0F);
        targetLevel.blockEvent(link.pos(), targetState.getBlock(), MirrorBlockEntity.EVENT_TRANSPORT, 0);
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.MIRROR_LINK.get()) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        GlobalPos link = stack.get(ModDataComponents.MIRROR_LINK.get());
        if (link != null) {
            builder.accept(MirrorItem.linkText("tc.thaumcraft.hand_mirror_linked_to", link));
        }
    }
}
