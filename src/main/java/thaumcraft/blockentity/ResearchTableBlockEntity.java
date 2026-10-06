package thaumcraft.blockentity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.Config;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.menu.ResearchTableMenu;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchManager;

public class ResearchTableBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int INPUT_SLOTS = 5;
    public static final int NOTE_SLOT = 5;
    public static final int PAPER_SLOT = 6;
    public static final int DATA_COUNT = 16;
    private static final float MAX_TAG_BONUS = 5.0F;

    private final NonNullList<ItemStack> items = NonNullList.withSize(7, ItemStack.EMPTY);
    private final Aspect[] tags = new Aspect[INPUT_SLOTS];
    private final int[] tagAmounts = new int[INPUT_SLOTS];
    private final float[] tagBonus = new float[INPUT_SLOTS];
    private boolean safe = true;
    private int baseChance;
    private int baseLoss;
    private boolean recalc;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 5) {
                return tags[index] == null ? -1 : tags[index].getId();
            }
            if (index < 10) {
                return tagAmounts[index - 5];
            }
            if (index < 15) {
                return Math.round(tagBonus[index - 10]);
            }
            return safe ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity table) {
        if (table.recalc) {
            table.recalculateTags();
            table.recalcBaseChance();
            table.recalc = false;
        }
    }

    public ContainerData getData() {
        return data;
    }

    public static boolean canResearch(Container container, ContainerData data) {
        if (data.get(0) <= -1) {
            return false;
        }
        ItemStack note = container.getItem(NOTE_SLOT);
        if (note.isEmpty()) {
            return !container.getItem(PAPER_SLOT).isEmpty();
        }
        return note.is(ModItems.RESEARCH_NOTES.get());
    }

    public void startResearch(Player researcher) {
        if (level == null || level.isClientSide()) {
            return;
        }
        level.playSound(null, worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.15F, 0.8F);
        doResearch(researcher);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (!items.get(slot).isEmpty() && level.getRandom().nextInt(100) < baseLoss) {
                items.get(slot).shrink(1);
            }
        }
        onContentsChanged();
    }

    private void doResearch(Player researcher) {
        int[] amounts = tagAmounts;
        for (int a = 0; a < INPUT_SLOTS; a++) {
            amounts[a] += Math.round(tagBonus[a]);
        }
        ItemStack note = items.get(NOTE_SLOT);
        if (note.isEmpty()) {
            String key = ResearchManager.findMatchingResearch(researcher, tags, amounts);
            if (key != null) {
                ResearchManager.createResearchNoteForTable(this, key);
                if (!items.get(NOTE_SLOT).isEmpty()) {
                    progress(researcher, amounts);
                }
            }
        } else if (note.is(ModItems.RESEARCH_NOTES.get())) {
            progress(researcher, amounts);
        }
    }

    private void progress(Player researcher, int[] amounts) {
        ResearchManager.progressTableResearch(level, researcher, this, items.get(NOTE_SLOT), baseChance, baseLoss, tags, amounts);
        ItemStack note = items.get(NOTE_SLOT);
        if (note.is(ModItems.RESEARCH_NOTES.get()) && ResearchManager.getData(note).getTotalProgress() == 1.0F) {
            items.set(NOTE_SLOT, ResearchManager.toDiscovery(note));
        }
    }

    public void toggleSafe() {
        safe = !safe;
        recalcBaseChance();
        setChanged();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.WOOD_STEP, SoundSource.BLOCKS, 0.3F, 1.2F);
        }
    }

    private void recalcBaseChance() {
        baseChance = safe ? Config.RESEARCH_SAFE_CHANCE.getAsInt() : Config.RESEARCH_THOROUGH_CHANCE.getAsInt();
        baseLoss = safe ? Config.RESEARCH_SAFE_LOSS.getAsInt() : Config.RESEARCH_THOROUGH_LOSS.getAsInt();
    }

    private void recalculateTags() {
        AspectList merged = new AspectList();
        for (int a = 0; a < INPUT_SLOTS; a++) {
            tags[a] = null;
            tagAmounts[a] = 0;
            tagBonus[a] = 0.0F;
            AspectList stackTags = AspectHelper.getObjectTagsWithBonus(items.get(a));
            if (stackTags != null) {
                for (Aspect aspect : stackTags.getAspects()) {
                    merged.merge(aspect, stackTags.getAmount(aspect));
                }
            }
        }
        if (merged.isEmpty()) {
            return;
        }
        List<Aspect> sorted = merged.getAspectsSortedAmount();
        for (int a = 0; a < INPUT_SLOTS && a < sorted.size(); a++) {
            tags[a] = sorted.get(a);
            tagAmounts[a] = merged.getAmount(sorted.get(a));
        }
        recalculateBonus();
    }

    private void recalculateBonus() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos above = worldPosition.above();
        int light = level.getMaxLocalRawBrightness(above);
        boolean sky = level.canSeeSky(above);
        if (!level.isBrightOutside() && light < 4 && !sky) {
            bonus(1.0F, Aspect.DARK);
        }
        if (level.isBrightOutside() && light > 11 && sky) {
            bonus(1.0F, Aspect.LIGHT);
        }
        int logicalHeight = level.dimensionType().logicalHeight();
        int relativeY = worldPosition.getY() - level.getMinBuildHeight();
        if (relativeY > logicalHeight * 0.5F) {
            bonus(1.0F, Aspect.WIND);
        }
        if (relativeY > logicalHeight * 0.66F) {
            bonus(1.0F, Aspect.WIND);
        }
        if (relativeY > logicalHeight * 0.75F) {
            bonus(1.0F, Aspect.WIND);
        }
        if (level.isRaining()) {
            bonus(1.0F, Aspect.WEATHER);
        }
        if (level.isThundering()) {
            bonus(1.0F, Aspect.WEATHER, Aspect.POWER, Aspect.WIND);
        }
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(15.0))) {
            entityBonus(entity);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                for (int y = -10; y <= 10; y++) {
                    int worldY = worldPosition.getY() + y;
                    if (worldY > level.getMinBuildHeight() && worldY < level.getMinBuildHeight() + logicalHeight) {
                        cursor.set(worldPosition.getX() + x, worldY, worldPosition.getZ() + z);
                        if (serverLevel.isLoaded(cursor)) {
                            blockBonus(level.getBlockState(cursor));
                        }
                    }
                }
            }
        }
    }

    private void entityBonus(LivingEntity entity) {
        if (!entity.onGround()) {
            bonus(0.5F, Aspect.FLIGHT);
        }
        if (entity.getMaxHealth() - entity.getHealth() > 0.0F) {
            bonus(0.5F, Aspect.FLESH);
        }
        if (entity.isInvertedHealAndHarm()) {
            bonus(0.3F, Aspect.DEATH, Aspect.EVIL);
            bonus(0.2F, Aspect.SPIRIT);
        }
        if (entity.hasEffect(MobEffects.POISON)) {
            bonus(0.5F, Aspect.POISON);
        }
        if (entity.hasEffect(MobEffects.REGENERATION)) {
            bonus(0.5F, Aspect.HEAL);
        }
        if (entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            bonus(0.5F, Aspect.ARMOR);
        }
        if (entity.hasEffect(MobEffects.NIGHT_VISION)) {
            bonus(0.5F, Aspect.VISION);
        }
        if (entity.hasEffect(MobEffects.CONFUSION)) {
            bonus(0.5F, Aspect.FLUX);
        }
        if (entity instanceof Spider) {
            bonus(0.3F, Aspect.INSECT);
        }
        if (entity instanceof Animal) {
            bonus(0.2F, Aspect.BEAST);
        }
        if (entity instanceof Sheep) {
            bonus(0.2F, Aspect.CLOTH);
        }
        if (entity instanceof Player) {
            bonus(0.4F, Aspect.CONTROL);
        }
        if (entity instanceof Villager) {
            bonus(0.3F, Aspect.CONTROL);
            bonus(0.5F, Aspect.EXCHANGE);
        }
        if (entity instanceof IronGolem || entity instanceof SnowGolem) {
            bonus(0.5F, Aspect.ARMOR, Aspect.WEAPON);
        }
        if (entity instanceof Blaze) {
            bonus(0.3F, Aspect.FIRE);
        }
        if (entity instanceof Creeper) {
            bonus(0.3F, Aspect.DESTRUCTION, Aspect.FLUX);
        }
    }

    private void blockBonus(BlockState state) {
        String id = state.getBlock().builtInRegistryHolder().key().location().toString();
        if (state.is(Blocks.JUKEBOX)) {
            bonus(0.5F, Aspect.SOUND);
        } else if (state.is(Blocks.BEACON)) {
            bonus(1.0F, Aspect.CONTROL, Aspect.LIGHT);
        } else if (state.getBlock() instanceof AbstractSkullBlock) {
            bonus(0.5F, Aspect.KNOWLEDGE, Aspect.EVIL);
        } else if (state.is(BlockTags.ANVIL)) {
            bonus(0.5F, Aspect.CRAFT, Aspect.TOOL);
        } else if (state.is(Blocks.DIAMOND_BLOCK)) {
            bonus(1.0F, Aspect.CRYSTAL, Aspect.VALUABLE);
        } else if (state.is(Blocks.GLASS_PANE)) {
            bonus(0.1F, Aspect.VISION);
        } else if (state.is(Blocks.GLASS)) {
            bonus(0.1F, Aspect.CRYSTAL);
        } else if (state.is(BlockTags.SMALL_FLOWERS)) {
            bonus(0.2F, Aspect.FLOWER);
        } else if (id.equals("thaumcraft:silverwood_log")) {
            bonus(0.15F, Aspect.PURE);
        } else if (id.equals("thaumcraft:greatwood_log")) {
            return;
        } else if (state.is(BlockTags.LOGS)) {
            bonus(0.1F, Aspect.WOOD);
        } else if (state.is(Blocks.CRAFTING_TABLE)) {
            bonus(0.2F, Aspect.CRAFT, Aspect.TOOL);
        } else if (state.is(BlockTags.STONE_PRESSURE_PLATES) || state.is(BlockTags.WOODEN_PRESSURE_PLATES)) {
            bonus(0.2F, Aspect.TRAP);
        } else if (id.equals("thaumcraft:brain_jar")) {
            bonus(0.5F, Aspect.KNOWLEDGE, Aspect.EVIL);
        } else if (id.equals("thaumcraft:warded_jar")) {
            bonus(0.33F, Aspect.TRAP);
        } else if (state.is(Blocks.IRON_BLOCK)) {
            bonus(0.75F, Aspect.METAL);
        } else if (state.is(Blocks.GOLD_BLOCK)) {
            bonus(0.75F, Aspect.VALUABLE);
        } else if (state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK)
            || state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM)) {
            bonus(0.2F, Aspect.FUNGUS);
        } else if (state.is(Blocks.BOOKSHELF)) {
            bonus(0.5F, Aspect.KNOWLEDGE);
        } else if (state.is(Blocks.SPAWNER)) {
            bonus(1.0F, Aspect.BEAST, Aspect.CRAFT, Aspect.TRAP);
        } else if (state.is(Blocks.CHEST)) {
            bonus(0.5F, Aspect.VOID);
        } else if (state.is(Blocks.FARMLAND)) {
            bonus(0.2F, Aspect.CROP);
        } else if (state.is(Blocks.END_PORTAL_FRAME)) {
            bonus(0.5F, Aspect.ELDRITCH);
        } else if (state.is(Blocks.ENDER_CHEST)) {
            bonus(1.0F, Aspect.VOID);
        } else if (state.is(Blocks.LAVA)) {
            bonus(0.2F, Aspect.FIRE, Aspect.ROCK);
        } else if (state.is(BlockTags.FIRE)) {
            bonus(0.5F, Aspect.FIRE);
        } else if (state.is(Blocks.BEDROCK)) {
            bonus(0.1F, Aspect.DARK, Aspect.ROCK, Aspect.EARTH);
        } else if (state.is(Blocks.DEAD_BUSH)) {
            bonus(0.5F, Aspect.DEATH);
        } else if (state.is(Blocks.ENCHANTING_TABLE)) {
            bonus(1.0F, Aspect.MAGIC);
        } else if (state.is(Blocks.WATER)) {
            bonus(0.2F, Aspect.WATER);
        } else if (state.is(Blocks.REDSTONE_WIRE)) {
            bonus(0.15F, Aspect.POWER, Aspect.MECHANISM);
        } else if (state.is(Blocks.REPEATER)) {
            bonus(0.25F, Aspect.POWER, Aspect.MECHANISM);
        } else if (state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL)) {
            bonus(0.5F, Aspect.ELDRITCH, Aspect.VOID);
        } else if (state.is(Blocks.CAKE)) {
            bonus(0.25F, Aspect.LIFE);
        } else if (state.is(Blocks.COBWEB)) {
            bonus(0.2F, Aspect.INSECT);
        } else if (state.is(Blocks.TNT)) {
            bonus(0.25F, Aspect.DESTRUCTION, Aspect.FIRE);
        } else if (state.is(Blocks.PISTON)) {
            bonus(0.3F, Aspect.MECHANISM, Aspect.MOTION);
        } else if (state.is(Blocks.EMERALD_BLOCK)) {
            bonus(1.0F, Aspect.MAGIC);
        } else if (state.is(Blocks.DRAGON_EGG)) {
            bonus(2.0F, Aspect.ELDRITCH, Aspect.EVIL, Aspect.MAGIC);
        } else if (state.is(BlockTags.ICE)) {
            bonus(0.2F, Aspect.COLD);
        } else if (state.getBlock() instanceof BushBlock) {
            bonus(0.2F, Aspect.PLANT);
        } else if (state.is(Blocks.REDSTONE_LAMP)) {
            bonus(0.25F, Aspect.LIGHT);
        } else if (state.is(Blocks.BREWING_STAND)) {
            bonus(0.5F, Aspect.MAGIC);
        } else if (state.is(BlockTags.CAULDRONS)) {
            bonus(0.5F, Aspect.WATER);
        }
    }

    private void bonus(float amount, Aspect... aspects) {
        for (int a = 0; a < INPUT_SLOTS; a++) {
            if (tags[a] == null) {
                continue;
            }
            for (Aspect aspect : aspects) {
                if (aspect == tags[a] && tagBonus[a] + amount <= MAX_TAG_BONUS) {
                    tagBonus[a] += amount;
                    break;
                }
            }
        }
    }

    public void markRecalc() {
        recalc = true;
    }

    private void onContentsChanged() {
        setChanged();
        recalc = true;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            onContentsChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        onContentsChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == NOTE_SLOT) {
            return stack.is(ModItems.RESEARCH_NOTES.get());
        }
        if (slot == PAPER_SLOT) {
            return stack.is(Items.PAPER);
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void startOpen(ContainerUser user) {
        recalc = true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropContents(level, pos, items);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumcraft.research_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResearchTableMenu(containerId, inventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        safe = input.getBooleanOr("Safe", true);
        recalc = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Safe", safe);
        ContainerHelper.saveAllItems(output, items, true);
    }
}
