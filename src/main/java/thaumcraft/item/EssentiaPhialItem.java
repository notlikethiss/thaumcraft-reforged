package thaumcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.EssentiaContainer;
import thaumcraft.registry.ModItems;

public class EssentiaPhialItem extends Item {
    public static final int PORTION = 8;

    public EssentiaPhialItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(pos) instanceof EssentiaContainer container)) {
            return InteractionResult.PASS;
        }
        Aspect aspect = container.getContainedAspect();
        if (aspect == null || container.getContainedAmount() < PORTION) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && container.takeFromSource(aspect, PORTION)) {
            context.getItemInHand().shrink(1);
            ItemStack essence = new ItemStack(ModItems.ESSENCE.get());
            EssenceItem.setAspect(essence, aspect);
            if (!player.getInventory().add(essence)) {
                Block.popResource(level, pos, essence);
            }
            playFillSound(level, player);
        }
        return InteractionResult.SUCCESS;
    }

    public static void playFillSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 0.25F, 1.0F);
    }
}
