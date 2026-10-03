package thaumcraft.block.crystal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.CrystalCoreBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModItems;

public class CrystalCoreBlock extends Block implements EntityBlock {
    public CrystalCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        RandomSource random = params.getLevel().getRandom();
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Items.NETHER_STAR));
        List<ItemStack> shards = List.of(
            new ItemStack(ModItems.AIR_SHARD.get()),
            new ItemStack(ModItems.FIRE_SHARD.get()),
            new ItemStack(ModItems.WATER_SHARD.get()),
            new ItemStack(ModItems.EARTH_SHARD.get()),
            new ItemStack(ModItems.VIS_SHARD.get())
        );
        int total = 0;
        for (ItemStack shard : shards) {
            int count = 2 + random.nextInt(3);
            total += count;
            drops.add(shard.copyWithCount(count));
        }
        if (20 - total > 0) {
            drops.add(new ItemStack(ModItems.DULL_SHARD.get(), 20 - total));
        }
        return drops;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) {
            return;
        }
        float mod = 0.2F;
        if (level.getBlockEntity(pos) instanceof CrystalCoreBlockEntity core) {
            mod += core.getSpeed();
        }
        Fx.get().crystalSparkle(level, pos.getX() + 0.2F + random.nextFloat() * 0.6F, pos.getY() + mod + random.nextFloat() * 0.6F,
            pos.getZ() + 0.2F + random.nextFloat() * 0.6F, random.nextInt(5));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalCoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.CRYSTAL_CORE.get()) {
            return null;
        }
        BlockEntityTicker<CrystalCoreBlockEntity> ticker = CrystalCoreBlockEntity::tick;
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> result = (BlockEntityTicker<T>) ticker;
        return result;
    }
}
