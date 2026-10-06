package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.block.device.ArcaneDoorBlock;
import thaumcraft.block.device.ArcanePressurePlateBlock;
import thaumcraft.blockentity.OwnedBlockEntity;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModSounds;

public class ArcaneKeyItem extends Item {
    private final int accessLevel;

    public ArcaneKeyItem(int accessLevel, Properties properties) {
        super(properties);
        this.accessLevel = accessLevel;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        int type;
        BlockPos target;
        if (state.getBlock() instanceof ArcaneDoorBlock) {
            type = KeyLink.DOOR;
            target = ArcaneDoorBlock.lowerHalf(state, pos);
        } else if (state.getBlock() instanceof ArcanePressurePlateBlock) {
            type = KeyLink.PLATE;
            target = pos;
        } else {
            return InteractionResult.PASS;
        }
        if (player == null || !(level.getBlockEntity(target) instanceof OwnedBlockEntity owned)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        KeyLink link = getLink(stack);
        if (link == null) {
            if (owned.isOwner(player) || owned.hasAccess(player, OwnedBlockEntity.ACCESS_GRANT) && accessLevel == OwnedBlockEntity.ACCESS_USE) {
                ItemStack key = new ItemStack(this);
                setLink(key, new KeyLink(target, type));
                if (!player.getInventory().add(key)) {
                    player.drop(key, false, Prediction.SERVER_ONLY);
                }
                stack.consume(1, player);
                player.sendSystemMessage(message(type == KeyLink.DOOR ? "key1" : "key2"));
                level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.PLAYERS, 1.0F, 0.9F);
            }
        } else if (!owned.isOwner(player)
            && !owned.hasAccess(player, accessLevel)
            && !owned.hasAccess(player, OwnedBlockEntity.ACCESS_GRANT)
            && link.pos().equals(target)) {
            owned.addAccess(player, accessLevel);
            if (type == KeyLink.DOOR && level.getBlockEntity(target.above()) instanceof OwnedBlockEntity upper) {
                upper.addAccess(player, accessLevel);
            }
            MutableComponent text = message(type == KeyLink.DOOR ? "key3" : "key5");
            if (accessLevel == OwnedBlockEntity.ACCESS_GRANT) {
                text.append(Component.translatable(type == KeyLink.DOOR ? "tc.thaumcraft.key4" : "tc.thaumcraft.key6"));
            }
            player.sendSystemMessage(text);
            level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.PLAYERS, 1.0F, 1.1F);
            stack.consume(1, player);
        } else {
            player.sendSystemMessage(message(link.pos().equals(target) ? "key8" : "key7"));
        }
        return InteractionResult.SUCCESS;
    }

    private static MutableComponent message(String key) {
        return Component.translatable("tc.thaumcraft." + key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
    }

    public static @Nullable KeyLink getLink(ItemStack stack) {
        return stack.get(ModDataComponents.KEY_LINK.get());
    }

    public static boolean hasLink(ItemStack stack) {
        return stack.has(ModDataComponents.KEY_LINK.get());
    }

    public static void setLink(ItemStack stack, KeyLink link) {
        stack.set(ModDataComponents.KEY_LINK.get(), link);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasLink(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        KeyLink link = getLink(stack);
        if (link == null) {
            return;
        }
        BlockPos pos = link.pos();
        builder.accept(message("key9"));
        builder.accept(message(link.type() == KeyLink.DOOR ? "key10" : "key11"));
        builder.accept(Component.literal("x " + pos.getX() + ", z " + pos.getZ() + ", y " + pos.getY()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
