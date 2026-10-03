package thaumcraft.blockentity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.item.ItemEntity;
import thaumcraft.entity.SpecialItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.aura.AuraManager;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModSounds;
import thaumcraft.registry.ModTags;

public class CrucibleBlockEntity extends TcBlockEntity implements AspectSource {
    public static final int MAX_TAGS = 500;
    public static final int BOIL_HEAT = 150;

    private short heat;
    private boolean liquid;
    private AspectList tags = new AspectList();
    private int bellows = -1;
    private boolean spillNextTick;
    private int contactDelay;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public int getHeat() {
        return heat;
    }

    public boolean hasLiquid() {
        return liquid;
    }

    public boolean isBoiling() {
        return liquid && heat > BOIL_HEAT;
    }

    public AspectList getTags() {
        return tags;
    }

    public void setTags(AspectList tags) {
        this.tags = tags;
        sync();
    }

    public void fill() {
        liquid = true;
        sync();
    }

    public boolean tickContactDelay() {
        if (++contactDelay < 10) {
            return false;
        }
        contactDelay = 0;
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        short previousHeat = crucible.heat;
        if (crucible.bellows < 0) {
            crucible.updateBellows();
        }
        if (crucible.spillNextTick) {
            crucible.spillRemnants();
            crucible.spillNextTick = false;
            crucible.updateBellows();
        }
        if (crucible.liquid) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.is(ModTags.CRUCIBLE_HEATERS)) {
                if (crucible.heat > 0) {
                    crucible.heat--;
                    if (crucible.heat == BOIL_HEAT - 1) {
                        crucible.sync();
                    }
                }
            } else if (crucible.heat < 200) {
                crucible.heat += (short) (1 + crucible.bellows * 2);
                if (previousHeat < BOIL_HEAT + 1 && crucible.heat >= BOIL_HEAT + 1) {
                    crucible.sync();
                }
            }
        }
        if (crucible.tags.visSize() > MAX_TAGS) {
            AspectList taken = crucible.takeRandomFromSource();
            AuraManager.addFluxToClosest(level, pos.getX(), pos.getY(), pos.getZ(), taken);
            if (crucible.tags.visSize() <= MAX_TAGS) {
                crucible.sync();
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (crucible.liquid) {
            crucible.drawEffects(level, pos);
        }
    }

    private void drawEffects(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (heat > BOIL_HEAT) {
            Fx.get().crucibleFroth(level, x + 0.2F + random.nextFloat() * 0.6F, y + getFluidHeight(), z + 0.2F + random.nextFloat() * 0.6F);
            if (tags.visSize() > MAX_TAGS) {
                for (int i = 0; i < 2; i++) {
                    Fx.get().crucibleFrothDown(level, x, y + 1, z + random.nextFloat());
                    Fx.get().crucibleFrothDown(level, x + 1, y + 1, z + random.nextFloat());
                    Fx.get().crucibleFrothDown(level, x + random.nextFloat(), y + 1, z);
                    Fx.get().crucibleFrothDown(level, x + random.nextFloat(), y + 1, z + 1);
                }
            }
        }
        if (random.nextInt(6) == 0 && !tags.isEmpty()) {
            List<Aspect> aspects = tags.getAspects();
            int color = aspects.get(random.nextInt(aspects.size())).color;
            int px = 5 + random.nextInt(22);
            int pz = 5 + random.nextInt(22);
            Fx.get().crucibleBubble(
                level,
                x + px / 32.0F + 1.0F / 64.0F,
                y + 0.05F + getFluidHeight(),
                z + pz / 32.0F + 1.0F / 64.0F,
                (color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F
            );
        }
    }

    public void updateBellows() {
        if (level == null) {
            return;
        }
        bellows = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(worldPosition.relative(direction)).is(ModTags.CRUCIBLE_BELLOWS)) {
                bellows++;
            }
        }
    }

    public float getFluidHeight() {
        float base = 0.7F;
        float out = base + tags.visSize() / (float) MAX_TAGS * 0.3F;
        if (out > 1.0F) {
            out = 1.001F;
        }
        if (out == 1.0F) {
            out = 0.9999F;
        }
        return out;
    }

    public void attemptSmelt(ItemEntity entity) {
        if (level == null) {
            return;
        }
        ItemStack stack = entity.getItem();
        AspectList aspects = AspectHelper.getObjectTagsWithBonus(stack);
        RandomSource random = level.getRandom();
        if (aspects == null || aspects.isEmpty()) {
            entity.setDeltaMovement((random.nextFloat() - random.nextFloat()) * 0.2F, 0.35F, (random.nextFloat() - random.nextFloat()) * 0.2F);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.2F, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
            return;
        }
        int count = stack.getCount();
        for (Aspect aspect : aspects.getAspects()) {
            tags.add(aspect, aspects.getAmount(aspect) * count);
        }
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.BUBBLE.value(), SoundSource.NEUTRAL, 0.2F, 1.0F + random.nextFloat() * 0.4F);
        entity.discard();
        sync();
        level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 1);
    }

    public void spillRemnants() {
        if (level == null) {
            return;
        }
        liquid = false;
        heat = 0;
        releaseTags();
        sync();
        level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 5);
    }

    private void releaseTags() {
        if (level == null) {
            return;
        }
        for (int pass = 0; pass < 2; pass++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(direction));
                if (!(neighbour instanceof AlembicBlockEntity alembic)) {
                    continue;
                }
                for (Aspect aspect : tags.getAspectsSortedAmount()) {
                    if (aspect == Aspect.UNKNOWN || (pass == 0 && alembic.sourceContains(aspect) <= 0)) {
                        continue;
                    }
                    int amount = tags.getAmount(aspect);
                    int left = alembic.addToSource(aspect, amount);
                    if (left != amount) {
                        tags.reduceAmount(aspect, amount - left);
                    }
                }
            }
        }
        AuraManager.addFluxToClosest(level, worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F, tags);
        tags = new AspectList();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            releaseTags();
        }
    }

    public void ejectItem(ItemStack items) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack remaining = items.copy();
        boolean first = true;
        while (!remaining.isEmpty()) {
            ItemStack part = remaining.split(remaining.getMaxStackSize());
            SpecialItem entity = new SpecialItem(serverLevel, worldPosition.getX() + 0.5, worldPosition.getY() + 0.71, worldPosition.getZ() + 0.5, part);
            RandomSource random = serverLevel.getRandom();
            double motionX = first ? 0.0 : (random.nextFloat() - random.nextFloat()) * 0.01F;
            double motionZ = first ? 0.0 : (random.nextFloat() - random.nextFloat()) * 0.01F;
            entity.setDeltaMovement(new Vec3(motionX, 0.1F, motionZ));
            entity.setGravity(0.0);
            serverLevel.addFreshEntity(entity);
            first = false;
        }
    }

    public AspectList takeRandomFromSource() {
        AspectList result = new AspectList();
        if (level != null && !tags.isEmpty()) {
            List<Aspect> aspects = tags.getAspects();
            Aspect aspect = aspects.get(level.getRandom().nextInt(aspects.size()));
            result.add(aspect, 1);
            tags.reduceAmount(aspect, 1);
        }
        return result;
    }

    @Override
    public boolean triggerEvent(int type, int param) {
        if (level == null) {
            return false;
        }
        if (type == 1) {
            if (level.isClientSide()) {
                Fx.get().blockSparkle(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), param, 5);
            }
            return true;
        }
        if (type == 2) {
            if (level.isClientSide()) {
                level.playLocalSound(worldPosition, ModSounds.SPILL.value(), SoundSource.BLOCKS, 0.2F, 1.0F, false);
                int[] colors = tags.getAspects().stream().mapToInt(aspect -> aspect.color).toArray();
                for (int i = 0; i < 10; i++) {
                    Fx.get().crucibleBoil(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), getFluidHeight(), colors, param);
                }
            }
            return true;
        }
        return super.triggerEvent(type, param);
    }

    @Override
    public int addToSource(Aspect aspect, int amount) {
        tags.add(aspect, amount);
        sync();
        return 0;
    }

    @Override
    public boolean takeFromSource(Aspect aspect, int amount) {
        if (tags.getAmount(aspect) >= amount) {
            tags.reduceAmount(aspect, amount);
            spillNextTick = true;
            sync();
            return true;
        }
        return false;
    }

    @Override
    public boolean takeFromSource(AspectList aspects) {
        if (!doesSourceContain(aspects)) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            tags.reduceAmount(aspect, aspects.getAmount(aspect));
        }
        spillNextTick = true;
        sync();
        return true;
    }

    @Override
    public boolean doesSourceContainAmount(Aspect aspect, int amount) {
        return tags.getAmount(aspect) >= amount;
    }

    @Override
    public boolean doesSourceContain(AspectList aspects) {
        for (Aspect aspect : tags.getAspects()) {
            if (tags.getAmount(aspect) <= aspects.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int sourceContains(Aspect aspect) {
        return tags.getAmount(aspect);
    }

    @Override
    public AspectList getSourceTags() {
        return tags;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heat = (short) input.getShortOr("Heat", (short) 0);
        liquid = input.getBooleanOr("Liquid", false);
        tags = input.read("Tags", AspectList.CODEC).orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("Heat", heat);
        output.putBoolean("Liquid", liquid);
        output.store("Tags", AspectList.CODEC, tags);
    }
}
