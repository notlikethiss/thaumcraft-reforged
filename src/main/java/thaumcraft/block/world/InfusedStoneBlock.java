package thaumcraft.block.world;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aura.AuraManager;
import thaumcraft.aura.AuraNode;
import thaumcraft.registry.ModBlocks;
import thaumcraft.world.BiomeHandler;

public class InfusedStoneBlock extends DropExperienceBlock {
    private final int type;

    public InfusedStoneBlock(int type, Properties properties) {
        super(UniformInt.of(3, 7), properties);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static Supplier<? extends Block> byType(int type) {
        return switch (type) {
            case 1 -> ModBlocks.AIR_INFUSED_STONE;
            case 2 -> ModBlocks.FIRE_INFUSED_STONE;
            case 3 -> ModBlocks.WATER_INFUSED_STONE;
            case 4 -> ModBlocks.EARTH_INFUSED_STONE;
            case 5 -> ModBlocks.VIS_INFUSED_STONE;
            default -> ModBlocks.DULL_INFUSED_STONE;
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(500) != 42) {
            return;
        }
        List<Integer> nodes = AuraManager.getAurasWithin(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
        if (nodes.isEmpty()) {
            return;
        }
        if (type == 6) {
            recharge(level, pos, random, nodes);
        } else {
            drain(level, pos, random, nodes);
        }
    }

    private void recharge(ServerLevel level, BlockPos pos, RandomSource random, List<Integer> nodes) {
        for (Integer key : nodes) {
            AuraNode node = AuraManager.getNode(key);
            if (node == null || node.level - 10 < node.baseLevel) {
                continue;
            }
            AuraManager.queueNodeChanges(node.key, -10, 0, false, null, 0, 0, 0);
            int newType = 0;
            for (int candidate = 1; candidate <= 5; candidate++) {
                if (isTouching(level, pos, byType(candidate).get())) {
                    newType = candidate;
                    break;
                }
            }
            if (newType == 0) {
                if (random.nextInt(3) == 0) {
                    newType = BiomeHandler.getRandomBiomeTag(level.getBiome(pos), random).element;
                } else {
                    newType = random.nextInt(5) + 1;
                }
            }
            level.setBlock(pos, byType(newType).get().defaultBlockState(), Block.UPDATE_NONE);
            return;
        }
    }

    private void drain(ServerLevel level, BlockPos pos, RandomSource random, List<Integer> nodes) {
        for (Integer key : nodes) {
            AuraNode node = AuraManager.getNode(key);
            if (node == null) {
                continue;
            }
            if (node.level + 10 <= node.baseLevel) {
                AuraManager.queueNodeChanges(node.key, 10, 0, false, null, 0, 0, 0);
                level.setBlock(pos, ModBlocks.DULL_INFUSED_STONE.get().defaultBlockState(), Block.UPDATE_NONE);
                return;
            }
            if (random.nextInt(50) == 42 && node.level - 100 >= node.baseLevel) {
                for (Direction direction : Direction.values()) {
                    BlockPos target = pos.relative(direction);
                    if (level.getBlockState(target).is(Blocks.STONE)) {
                        AuraManager.queueNodeChanges(node.key, -50, 0, false, null, 0, 0, 0);
                        level.setBlock(target, this.defaultBlockState(), Block.UPDATE_NONE);
                        return;
                    }
                }
            }
        }
    }

    private static boolean isTouching(BlockGetter level, BlockPos pos, Block block) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(block)) {
                return true;
            }
        }
        return false;
    }
}
