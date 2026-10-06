package thaumcraft.item.golem;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.blockentity.MagicWorkbenchBlockEntity;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemInventories;
import thaumcraft.entity.golem.GolemKind;
import org.jspecify.annotations.Nullable;
import thaumcraft.registry.ModDataComponents;

public class GolemPlacerItem extends Item {
    private static final String[] CORE_NAMES = {"", "fast", "smart", "perceptive", "strong"};
    private static final String[][] DECORATIONS = {
        {"H", "top_hat"}, {"G", "spectacles"}, {"B", "bowtie"}, {"F", "fez"}, {"R", "dart_launcher"}, {"V", "visor"}, {"P", "iron_plating"},
    };

    private final GolemKind kind;

    public GolemPlacerItem(GolemKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public GolemKind kind() {
        return kind;
    }

    public static int getCore(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.GOLEM_CORE.get(), 0);
    }

    public static boolean hasCore(ItemStack stack) {
        return stack.has(ModDataComponents.GOLEM_CORE.get());
    }

    public static void setCore(ItemStack stack, int core) {
        stack.set(ModDataComponents.GOLEM_CORE.get(), core);
    }

    public static @Nullable String getDecoration(ItemStack stack) {
        return stack.get(ModDataComponents.GOLEM_DECORATION.get());
    }

    public static void setDecoration(ItemStack stack, String decoration) {
        stack.set(ModDataComponents.GOLEM_DECORATION.get(), decoration);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int core = getCore(stack);
        if (core > 0 && core < CORE_NAMES.length) {
            builder.accept(Component.translatable("tc.thaumcraft.golem." + CORE_NAMES[core]).withStyle(ChatFormatting.GRAY));
        }
        String decoration = getDecoration(stack);
        if (decoration != null && !decoration.isEmpty()) {
            MutableComponent line = Component.empty().withStyle(ChatFormatting.DARK_GREEN);
            for (String[] entry : DECORATIONS) {
                if (decoration.contains(entry[0])) {
                    line.append(GolemDecorationItem.decorationName(entry[1])).append(" ");
                }
            }
            builder.accept(line);
        }
    }

    private boolean isValidHome(Level level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return switch (kind) {
            case STRAW, IRON_GUARDIAN -> true;
            case TALLOW -> blockEntity instanceof CrucibleBlockEntity || blockEntity instanceof AlembicBlockEntity;
            case DECANTING -> blockEntity instanceof AlembicBlockEntity || GolemInventories.hasFluids(level, pos, side);
            default -> blockEntity != null
                && GolemInventories.hasItems(level, pos, side)
                && !(blockEntity instanceof MagicWorkbenchBlockEntity)
                && !(blockEntity instanceof AbstractFurnaceBlockEntity && side.getAxis().isHorizontal() && kind != GolemKind.STONE && kind != GolemKind.ADVANCED_STONE);
        };
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        if (!isValidHome(level, pos, side)) {
            if (!level.isClientSide() && player != null) {
                boolean tallow = kind == GolemKind.TALLOW || kind == GolemKind.DECANTING;
                player.sendSystemMessage(Component.translatable(tallow ? "tc.thaumcraft.golem.cannot_place" : "tc.thaumcraft.golem.invalid_inventory"));
            }
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel serverLevel) {
            boolean fence = side == Direction.UP && level.getBlockState(pos).is(BlockTags.WOODEN_FENCES) || level.getBlockState(pos).is(Blocks.NETHER_BRICK_FENCE);
            BlockPos spawn = pos.relative(side);
            if (spawnGolem(serverLevel, spawn.getX() + 0.5, spawn.getY() + (fence ? 0.5 : 0.0), spawn.getZ() + 0.5, side, stack, player)
                && (player == null || !player.getAbilities().instabuild)) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private boolean spawnGolem(ServerLevel level, double x, double y, double z, Direction side, ItemStack stack, Player player) {
        if (!(kind.entityType().create(level, EntitySpawnReason.SPAWN_ITEM_USE) instanceof GolemBase golem)) {
            return false;
        }
        golem.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        golem.setHomeTo(BlockPos.containing(x, y, z), 32);
        golem.setup(getCore(stack), -1, side);
        String decoration = getDecoration(stack);
        if (decoration != null) {
            golem.setDecoration(decoration);
        }
        golem.setHealth(golem.getMaxHealth());
        if (player != null) {
            golem.setOwner(player.getUUID());
        }
        level.addFreshEntity(golem);
        golem.playAmbientSound();
        return true;
    }
}
