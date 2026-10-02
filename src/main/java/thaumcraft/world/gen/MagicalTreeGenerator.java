package thaumcraft.world.gen;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MagicalTreeGenerator {
    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

    protected final Random rand = new Random();
    protected final boolean notify;
    protected final BlockState log;
    protected final BlockState leaves;
    protected LevelAccessor level;
    protected int[] basePos = {0, 0, 0};
    protected int heightLimit;
    protected int height;
    protected double heightAttenuation = 0.618;
    protected double branchSlope;
    protected double scaleWidth;
    protected double leafDensity = 1.5;
    protected int trunkSize;
    protected int heightLimitLimit;
    protected int leafDistanceLimit = 4;
    protected int[][] leafNodes;

    public MagicalTreeGenerator(boolean notify, BlockState log, BlockState leaves, double branchSlope, double scaleWidth, int trunkSize, int heightLimitLimit) {
        this.notify = notify;
        this.log = log;
        this.leaves = leaves;
        this.branchSlope = branchSlope;
        this.scaleWidth = scaleWidth;
        this.trunkSize = trunkSize;
        this.heightLimitLimit = heightLimitLimit;
    }

    protected void setBlock(int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, notify ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS);
    }

    private boolean isAirOrLeaves(int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.isAir() || state.is(leaves.getBlock());
    }

    protected void generateLeafNodeList() {
        height = (int) (heightLimit * heightAttenuation);
        if (height >= heightLimit) {
            height = heightLimit - 1;
        }
        int perLayer = (int) (1.382 + Math.pow(leafDensity * heightLimit / 13.0, 2.0));
        if (perLayer < 1) {
            perLayer = 1;
        }
        int[][] nodes = new int[perLayer * heightLimit][4];
        int y = basePos[1] + heightLimit - leafDistanceLimit;
        int count = 1;
        int trunkTop = basePos[1] + height;
        int layer = y - basePos[1];
        nodes[0][0] = basePos[0];
        nodes[0][1] = y;
        nodes[0][2] = basePos[2];
        nodes[0][3] = trunkTop;
        y--;
        while (layer >= 0) {
            float size = layerSize(layer);
            if (size >= 0.0F) {
                for (int i = 0; i < perLayer; i++) {
                    double radius = scaleWidth * size * (rand.nextFloat() + 0.328);
                    double angle = rand.nextFloat() * 2.0 * Math.PI;
                    int nx = Mth.floor(radius * Math.sin(angle) + basePos[0] + 0.5);
                    int nz = Mth.floor(radius * Math.cos(angle) + basePos[2] + 0.5);
                    int[] start = {nx, y, nz};
                    int[] end = {nx, y + leafDistanceLimit, nz};
                    if (checkBlockLine(start, end) == -1) {
                        int[] branchBase = {basePos[0], basePos[1], basePos[2]};
                        double distance = Math.sqrt(Math.pow(Math.abs(basePos[0] - start[0]), 2.0) + Math.pow(Math.abs(basePos[2] - start[2]), 2.0));
                        double drop = distance * branchSlope;
                        if (start[1] - drop > trunkTop) {
                            branchBase[1] = trunkTop;
                        } else {
                            branchBase[1] = (int) (start[1] - drop);
                        }
                        if (checkBlockLine(branchBase, start) == -1) {
                            nodes[count][0] = nx;
                            nodes[count][1] = y;
                            nodes[count][2] = nz;
                            nodes[count][3] = branchBase[1];
                            count++;
                        }
                    }
                }
            }
            y--;
            layer--;
        }
        leafNodes = new int[count][4];
        System.arraycopy(nodes, 0, leafNodes, 0, count);
    }

    private void genTreeLayer(int x, int y, int z, float size, byte axis) {
        int range = (int) (size + 0.618);
        byte first = OTHER_COORD_PAIRS[axis];
        byte second = OTHER_COORD_PAIRS[axis + 3];
        int[] center = {x, y, z};
        int[] pos = {0, 0, 0};
        pos[axis] = center[axis];
        for (int a = -range; a <= range; a++) {
            pos[first] = center[first] + a;
            for (int b = -range; b <= range; b++) {
                double dist = Math.pow(Math.abs(a) + 0.5, 2.0) + Math.pow(Math.abs(b) + 0.5, 2.0);
                if (dist > size * size) {
                    continue;
                }
                pos[second] = center[second] + b;
                if (isAirOrLeaves(pos[0], pos[1], pos[2])) {
                    setBlock(pos[0], pos[1], pos[2], leaves);
                }
            }
        }
    }

    private float layerSize(int layer) {
        if (layer < heightLimit * 0.3) {
            return -1.618F;
        }
        float half = heightLimit / 2.0F;
        float offset = heightLimit / 2.0F - layer;
        float size;
        if (offset == 0.0F) {
            size = half;
        } else if (Math.abs(offset) >= half) {
            size = 0.0F;
        } else {
            size = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0) - Math.pow(Math.abs(offset), 2.0));
        }
        return size * 0.5F;
    }

    private float leafSize(int layer) {
        if (layer < 0 || layer >= leafDistanceLimit) {
            return -1.0F;
        }
        return layer != 0 && layer != leafDistanceLimit - 1 ? 3.0F : 2.0F;
    }

    private void generateLeafNode(int x, int y, int z) {
        for (int layer = y; layer < y + leafDistanceLimit; layer++) {
            genTreeLayer(x, layer, z, leafSize(layer - y), (byte) 1);
        }
    }

    private void placeBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        byte major = 0;
        for (byte i = 0; i < 3; i++) {
            delta[i] = to[i] - from[i];
            if (Math.abs(delta[i]) > Math.abs(delta[major])) {
                major = i;
            }
        }
        if (delta[major] == 0) {
            return;
        }
        byte first = OTHER_COORD_PAIRS[major];
        byte second = OTHER_COORD_PAIRS[major + 3];
        int step = delta[major] > 0 ? 1 : -1;
        double firstRatio = (double) delta[first] / delta[major];
        double secondRatio = (double) delta[second] / delta[major];
        int[] pos = {0, 0, 0};
        for (int i = 0, end = delta[major] + step; i != end; i += step) {
            pos[major] = Mth.floor(from[major] + i + 0.5);
            pos[first] = Mth.floor(from[first] + i * firstRatio + 0.5);
            pos[second] = Mth.floor(from[second] + i * secondRatio + 0.5);
            Direction.Axis axis = Direction.Axis.Y;
            int dx = Math.abs(pos[0] - from[0]);
            int dz = Math.abs(pos[2] - from[2]);
            int max = Math.max(dx, dz);
            if (max > 0) {
                if (dx == max) {
                    axis = Direction.Axis.X;
                } else if (dz == max) {
                    axis = Direction.Axis.Z;
                }
            }
            setBlock(pos[0], pos[1], pos[2], log.setValue(RotatedPillarBlock.AXIS, axis));
        }
    }

    protected void generateLeaves() {
        for (int[] node : leafNodes) {
            generateLeafNode(node[0], node[1], node[2]);
        }
    }

    protected void generateTrunk() {
        int[] from = {basePos[0], basePos[1], basePos[2]};
        int[] to = {basePos[0], basePos[1] + height, basePos[2]};
        placeBlockLine(from, to);
        if (trunkSize == 2) {
            from[0]++;
            to[0]++;
            placeBlockLine(from, to);
            from[2]++;
            to[2]++;
            placeBlockLine(from, to);
            from[0]--;
            to[0]--;
            placeBlockLine(from, to);
        }
    }

    protected void generateLeafNodeBases() {
        int[] base = {basePos[0], basePos[1], basePos[2]};
        for (int[] node : leafNodes) {
            int[] target = {node[0], node[1], node[2]};
            base[1] = node[3];
            if (base[1] - basePos[1] >= heightLimit * 0.2) {
                placeBlockLine(base, target);
            }
        }
    }

    private int checkBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        byte major = 0;
        for (byte i = 0; i < 3; i++) {
            delta[i] = to[i] - from[i];
            if (Math.abs(delta[i]) > Math.abs(delta[major])) {
                major = i;
            }
        }
        if (delta[major] == 0) {
            return -1;
        }
        byte first = OTHER_COORD_PAIRS[major];
        byte second = OTHER_COORD_PAIRS[major + 3];
        int step = delta[major] > 0 ? 1 : -1;
        double firstRatio = (double) delta[first] / delta[major];
        double secondRatio = (double) delta[second] / delta[major];
        int[] pos = {0, 0, 0};
        int i = 0;
        int end = delta[major] + step;
        for (; i != end; i += step) {
            pos[major] = from[major] + i;
            pos[first] = Mth.floor(from[first] + i * firstRatio);
            pos[second] = Mth.floor(from[second] + i * secondRatio);
            if (!isAirOrLeaves(pos[0], pos[1], pos[2])) {
                break;
            }
        }
        return i == end ? -1 : Math.abs(i);
    }

    private boolean validTreeLocation(int dx, int dz) {
        int[] from = {basePos[0] + dx, basePos[1], basePos[2] + dz};
        int[] to = {basePos[0] + dx, basePos[1] + heightLimit - 1, basePos[2] + dz};
        BlockState soil = level.getBlockState(new BlockPos(basePos[0] + dx, basePos[1] - 1, basePos[2] + dz));
        if (!soil.is(BlockTags.DIRT)) {
            return false;
        }
        int blocked = checkBlockLine(from, to);
        if (blocked == -1) {
            return true;
        }
        if (blocked < 6) {
            return false;
        }
        heightLimit = blocked;
        return true;
    }

    protected boolean prepare(LevelAccessor level, Random random, BlockPos pos) {
        this.level = level;
        rand.setSeed(random.nextLong());
        basePos = new int[]{pos.getX(), pos.getY(), pos.getZ()};
        if (heightLimit == 0) {
            heightLimit = heightLimitLimit + rand.nextInt(heightLimitLimit);
        }
        for (int dx = 0; dx < trunkSize; dx++) {
            for (int dz = 0; dz < trunkSize; dz++) {
                if (!validTreeLocation(dx, dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void generateSegment(int x, int y, int z) {
        basePos = new int[]{x, y, z};
        generateLeafNodeList();
        generateLeaves();
        generateTrunk();
        generateLeafNodeBases();
    }
}
