package thaumcraft.blockentity;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aura.AuraManager;
import thaumcraft.block.device.BellowsBlock;
import thaumcraft.block.device.InfernalFurnaceBlock;
import thaumcraft.crafting.ThaumcraftRecipes;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;

public class InfernalFurnaceBlockEntity extends BlockEntity {
    private static final TagKey<Item> BONUS_EXCLUDED = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dusts"));

    private final NonNullList<ItemStack> items = NonNullList.withSize(32, ItemStack.EMPTY);
    private int cookTime;
    private int maxCookTime;

    public InfernalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFERNAL_FURNACE.get(), pos, state);
    }

    private Direction facing() {
        return getBlockState().getValue(HorizontalDirectionalBlock.FACING);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceBlockEntity furnace) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (furnace.maxCookTime == 0) {
            furnace.maxCookTime = furnace.calcCookTime();
        }
        if (furnace.cookTime > furnace.maxCookTime) {
            furnace.cookTime = furnace.maxCookTime;
        }
        boolean cooked = false;
        if (furnace.cookTime > 0) {
            furnace.cookTime--;
            cooked = true;
        }
        if (furnace.cookTime == 0 && cooked) {
            furnace.smeltNext(serverLevel);
        }
        if (furnace.cookTime == 0 && !cooked) {
            for (ItemStack stack : furnace.items) {
                if (!stack.isEmpty() && furnace.smeltingRecipe(serverLevel, stack).isPresent()) {
                    furnace.maxCookTime = furnace.calcCookTime();
                    furnace.cookTime = furnace.maxCookTime;
                    break;
                }
            }
        }
    }

    private void smeltNext(ServerLevel level) {
        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }
            Optional<RecipeHolder<SmeltingRecipe>> recipe = smeltingRecipe(level, stack);
            if (recipe.isPresent()
                && AuraManager.decreaseClosestAura(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1)) {
                ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(stack), level.registryAccess());
                ejectItem(level, result, recipe.get().value().getExperience(), stack.is(BONUS_EXCLUDED));
                level.blockEvent(worldPosition, getBlockState().getBlock(), 3, 0);
                stack.shrink(1);
                setChanged();
                break;
            }
        }
    }

    private Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe(ServerLevel level, ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
    }

    private int bellows() {
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level != null && level.getBlockState(worldPosition.relative(direction, 2)).getBlock() instanceof BellowsBlock) {
                count++;
            }
        }
        return Math.min(3, count);
    }

    private int calcCookTime() {
        return 80 - 20 * bellows();
    }

    public boolean addItems(ItemStack added) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, added) && stack.getCount() + added.getCount() <= added.getMaxStackSize()) {
                stack.grow(added.getCount());
                checkSmeltable(serverLevel, slot);
                setChanged();
                return true;
            }
            if (stack.isEmpty()) {
                items.set(slot, added.copy());
                checkSmeltable(serverLevel, slot);
                setChanged();
                return true;
            }
        }
        return false;
    }

    private void checkSmeltable(ServerLevel level, int slot) {
        if (smeltingRecipe(level, items.get(slot)).isPresent()) {
            return;
        }
        items.set(slot, ItemStack.EMPTY);
        RandomSource random = level.getRandom();
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
    }

    private void ejectItem(ServerLevel level, ItemStack result, float experience, boolean excluded) {
        RandomSource random = level.getRandom();
        Direction facing = facing();
        int facingX = facing.getStepX();
        int facingZ = facing.getStepZ();
        int bellows = bellows();
        double x = worldPosition.getX() + 0.5 + facingX * 1.2;
        double y = worldPosition.getY() + 0.4;
        double z = worldPosition.getZ() + 0.5 + facingZ * 1.2;
        spawn(level, new ItemEntity(level, x, y, z, result.copy()), facingX, facingZ, 0.03F, random);
        if (!excluded) {
            String bonusId = ThaumcraftRecipes.smeltingBonus().get(BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            if (bonusId != null) {
                int count = 0;
                if (bellows == 0) {
                    if (random.nextInt(4) == 0) {
                        count++;
                    }
                } else {
                    for (int index = 0; index < bellows; index++) {
                        if (random.nextBoolean()) {
                            count++;
                        }
                    }
                }
                Item bonus = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(bonusId)).orElse(null);
                if (count > 0 && bonus != null) {
                    spawn(level, new ItemEntity(level, x, y, z, new ItemStack(bonus, count)), facingX, facingZ, 0.03F, random);
                }
            }
        }
        if (random.nextInt(15 + bellows * 5) == 0) {
            AuraManager.addFluxToClosest(
                level,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                new AspectList().add(random.nextBoolean() ? Aspect.FIRE : Aspect.EVIL, 1)
            );
        }
        int xp = result.getCount();
        if (experience == 0.0F) {
            xp = 0;
        } else if (experience < 1.0F) {
            int floor = Mth.floor(xp * experience);
            if (floor < Mth.ceil(xp * experience) && random.nextFloat() < xp * experience - floor) {
                floor++;
            }
            xp = floor;
        }
        while (xp > 0) {
            int value = ExperienceOrb.getExperienceValue(xp);
            xp -= value;
            spawn(level, new ExperienceOrb(level, x, y, z, value), facingX, facingZ, 0.025F, random);
        }
    }

    private static void spawn(ServerLevel level, Entity entity, int facingX, int facingZ, float spread, RandomSource random) {
        double motionX = facingX == 0 ? (random.nextFloat() - random.nextFloat()) * spread : facingX * 0.13;
        double motionZ = facingZ == 0 ? (random.nextFloat() - random.nextFloat()) * spread : facingZ * 0.13;
        entity.setDeltaMovement(motionX, 0.0, motionZ);
        level.addFreshEntity(entity);
    }

    @Override
    public boolean triggerEvent(int type, int param) {
        if (type == 3) {
            if (level != null && level.isClientSide()) {
                Direction facing = facing();
                RandomSource random = level.getRandom();
                for (int index = 0; index < 5; index++) {
                    Fx.get().furnaceLava(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), facing.getStepX(), facing.getStepZ());
                    level.playLocalSound(worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.15F, false);
                }
            }
            return true;
        }
        return super.triggerEvent(type, param);
    }

    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, items);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        cookTime = ValueInput.of(tag, registries).getShortOr("CookTime", (short) 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        ValueOutput.of(tag, registries).putShort("CookTime", (short) cookTime);
    }
}
