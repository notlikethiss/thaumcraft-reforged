package thaumcraft.world.gen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModBlocks;

public class GreatwoodTreeGenerator extends MagicalTreeGenerator {
    public static final ResourceKey<LootTable> SPIDER_NEST_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Thaumcraft.id("chests/greatwood_spider_nest"));

    public GreatwoodTreeGenerator(boolean notify) {
        super(
            notify,
            ModBlocks.GREATWOOD_LOG.get().defaultBlockState(),
            ModBlocks.GREATWOOD_LEAVES.get().defaultBlockState(),
            0.38,
            1.25,
            2,
            8
        );
    }

    public boolean generate(LevelAccessor level, Random random, BlockPos pos, boolean spiders) {
        if (!prepare(level, random, pos)) {
            return false;
        }
        generateSegment(pos.getX(), pos.getY(), pos.getZ());
        scaleWidth = 1.66;
        generateSegment(pos.getX(), pos.getY() + height, pos.getZ());
        if (spiders) {
            placeSpiderNest(level, random, pos);
        }
        return true;
    }

    private void placeSpiderNest(LevelAccessor level, Random random, BlockPos pos) {
        BlockPos spawnerPos = pos.below();
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner)) {
            return;
        }
        spawner.setEntityId(EntityTypes.CAVE_SPIDER, level.getRandom());
        for (int i = 0; i < 50; i++) {
            BlockPos web = new BlockPos(
                pos.getX() - 7 + random.nextInt(14),
                pos.getY() + random.nextInt(10),
                pos.getZ() - 7 + random.nextInt(14)
            );
            if (level.isEmptyBlock(web) && (isTouching(level, web, ModBlocks.GREATWOOD_LEAVES.get()) || isTouching(level, web, ModBlocks.GREATWOOD_LOG.get()))) {
                level.setBlock(web, Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        BlockPos chestPos = pos.below(2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(SPIDER_NEST_LOOT, rand.nextLong());
        }
    }

    private static boolean isTouching(LevelAccessor level, BlockPos pos, Block block) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(block)) {
                return true;
            }
        }
        return false;
    }
}
