package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModDataComponents;

public class FilledJarItem extends Item {
    public FilledJarItem(Properties properties) {
        super(properties);
    }

    public static @Nullable JarContents getContents(ItemStack stack) {
        return stack.get(ModDataComponents.JAR_CONTENTS.get());
    }

    public static void setContents(ItemStack stack, JarContents contents) {
        stack.set(ModDataComponents.JAR_CONTENTS.get(), contents);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!placeContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = placeContext.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Block block = ModBlocks.WARDED_JAR.get();
        BlockState state = block.getStateForPlacement(placeContext);
        if (state == null
            || !state.canSurvive(level, pos)
            || !level.isUnobstructed(state, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player))) {
            return InteractionResult.FAIL;
        }
        if (!level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return InteractionResult.FAIL;
        }
        if (level.getBlockEntity(pos) instanceof JarBlockEntity jar) {
            jar.applyComponentsFromItemStack(stack);
            jar.sync();
        }
        block.setPlacedBy(level, pos, state, player, stack);
        SoundType sound = state.getSoundType(level, pos, player);
        level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        JarContents contents = getContents(stack);
        return contents == null
            ? super.getName(stack)
            : Component.translatable("tc.thaumcraft.jarname").append(" ").append(contents.aspect().getDisplayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        JarContents contents = getContents(stack);
        if (contents != null) {
            builder.accept(contents.aspect().getMeaning());
            builder.accept(Component.translatable("tc.thaumcraft.jaressentia", contents.amount()));
        }
    }
}
