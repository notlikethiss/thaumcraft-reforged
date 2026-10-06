package thaumcraft.item.wand;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.entity.projectile.FrostShard;
import thaumcraft.registry.ModSounds;

public class FrostWandItem extends ElementalWandItem {
    public FrostWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            FrostShard shard = new FrostShard(serverLevel, player, 1.0F + getPotency(level, stack) / 2.0F);
            if (serverLevel.addFreshEntity(shard)) {
                damageWand(stack, player, hand, 1);
            }
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ICE.value(), SoundSource.PLAYERS, 0.4F, 1.0F + level.getRandom().nextFloat() * 0.1F);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
