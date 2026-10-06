package thaumcraft.lib;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Utils {
    private Utils() {
    }

    public static boolean isChunkLoaded(Level level, double x, double z) {
        return level.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    public static int getFirstUncoveredBlockHeight(Level level, int x, int z) {
        int y = Math.max(10, level.getMinBuildHeight());
        while (y < level.getMaxY() && !level.isEmptyBlock(new BlockPos(x, y + 1, z))) {
            y++;
        }
        return y;
    }

    public static Entity getPointedEntity(Level level, Player player, double range, float padding) {
        return getPointedEntity(level, player, range, padding, false);
    }

    public static Entity getPointedEntity(Level level, Player player, double range, float padding, boolean nonCollide) {
        return getPointedEntity(level, player, range, padding, nonCollide, entity -> true);
    }

    public static Entity getPointedEntity(Level level, Player player, double range, Class<? extends Entity> excluded) {
        return getPointedEntity(level, player, range, 1.1F, false, entity -> !excluded.isInstance(entity));
    }

    public static Entity getPointedEntity(Level level, Player player, double range, float padding, boolean nonCollide, Predicate<Entity> filter) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);
        AABB area = player.getBoundingBox().expandTowards(look.x * range, look.y * range, look.z * range).inflate(padding);
        List<Entity> entities = level.getEntities(player, area, entity -> !entity.isSpectator() && filter.test(entity));
        Entity pointed = null;
        double closest = 0.0;
        for (Entity entity : entities) {
            if (!entity.canBeCollidedWith(player) && !nonCollide) {
                continue;
            }
            Vec3 target = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
            if (level.clip(new ClipContext(start, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) {
                continue;
            }
            float border = Math.max(0.8F, entity.getPickRadius());
            AABB box = entity.getBoundingBox().inflate(border);
            if (box.contains(start)) {
                pointed = entity;
                closest = 0.0;
            } else {
                Vec3 hit = box.clip(start, end).orElse(null);
                if (hit != null) {
                    double distance = start.distanceTo(hit);
                    if (distance < closest || closest == 0.0) {
                        pointed = entity;
                        closest = distance;
                    }
                }
            }
        }
        return pointed;
    }

    public static BlockHitResult getTargetBlock(Level level, Player player, boolean liquids) {
        return getTargetBlock(level, player, liquids, 10.0);
    }

    public static BlockHitResult getTargetBlock(Level level, Player player, boolean liquids, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        return getTargetBlock(level, player, start, start.add(look.x * range, look.y * range, look.z * range), liquids);
    }

    public static BlockHitResult getTargetBlock(Level level, Entity source, Vec3 start, Vec3 end, boolean liquids) {
        return level.clip(new ClipContext(
            start,
            end,
            liquids ? ClipContext.Block.OUTLINE : ClipContext.Block.COLLIDER,
            liquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
            source
        ));
    }

    public static boolean isBlockExposed(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!level.getBlockState(pos.relative(direction)).isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    public static boolean consumeInventoryItem(Player player, Item item) {
        return consumeInventoryItem(player, stack -> stack.is(item));
    }

    public static boolean consumeInventoryItem(Player player, Predicate<ItemStack> matcher) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && matcher.test(stack)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    public static boolean useBonemealAtLoc(Level level, BlockPos pos) {
        return BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, pos);
    }
}
