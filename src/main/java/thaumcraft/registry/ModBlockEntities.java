package thaumcraft.registry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.ArcaneEarBlockEntity;
import thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;
import thaumcraft.blockentity.ArcaneBoreBlockEntity;
import thaumcraft.blockentity.CrystalCapacitorBlockEntity;
import thaumcraft.blockentity.CrystalClusterBlockEntity;
import thaumcraft.blockentity.CrystalCoreBlockEntity;
import thaumcraft.blockentity.HoleBlockEntity;
import thaumcraft.blockentity.HungryChestBlockEntity;
import thaumcraft.blockentity.MirrorBlockEntity;
import thaumcraft.blockentity.LevitatorBlockEntity;
import thaumcraft.blockentity.BellowsBlockEntity;
import thaumcraft.blockentity.InfernalFurnaceBlockEntity;
import thaumcraft.blockentity.BrainJarBlockEntity;
import thaumcraft.blockentity.JarBlockEntity;
import thaumcraft.blockentity.ArcaneWorktableBlockEntity;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.blockentity.InfusionWorkbenchBlockEntity;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.blockentity.NitorBlockEntity;
import thaumcraft.blockentity.OwnedBlockEntity;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Thaumcraft.MODID);

    public static final Supplier<BlockEntityType<NitorBlockEntity>> NITOR = BLOCK_ENTITIES.register(
        "nitor",
        () -> BlockEntityType.Builder.of(NitorBlockEntity::new, ModBlocks.NITOR.get()).build(null)
    );

    public static final Supplier<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = BLOCK_ENTITIES.register(
        "crucible",
        () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<AlembicBlockEntity>> ALEMBIC = BLOCK_ENTITIES.register(
        "alembic",
        () -> BlockEntityType.Builder.of(AlembicBlockEntity::new, ModBlocks.ALEMBIC.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneWorktableBlockEntity>> ARCANE_WORKTABLE = BLOCK_ENTITIES.register(
        "arcane_worktable",
        () -> BlockEntityType.Builder.of(ArcaneWorktableBlockEntity::new, ModBlocks.ARCANE_WORKTABLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE = BLOCK_ENTITIES.register(
        "research_table",
        () -> BlockEntityType.Builder.of(ResearchTableBlockEntity::new, ModBlocks.RESEARCH_TABLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfusionWorkbenchBlockEntity>> INFUSION_WORKBENCH = BLOCK_ENTITIES.register(
        "infusion_workbench",
        () -> BlockEntityType.Builder.of(InfusionWorkbenchBlockEntity::new, ModBlocks.ARCANE_STONE.get()).build(null)
    );

    public static final Supplier<BlockEntityType<JarBlockEntity>> WARDED_JAR = BLOCK_ENTITIES.register(
        "warded_jar",
        () -> BlockEntityType.Builder.of(JarBlockEntity::new, ModBlocks.WARDED_JAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<BrainJarBlockEntity>> BRAIN_JAR = BLOCK_ENTITIES.register(
        "brain_jar",
        () -> BlockEntityType.Builder.of(BrainJarBlockEntity::new, ModBlocks.BRAIN_JAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<BellowsBlockEntity>> BELLOWS = BLOCK_ENTITIES.register(
        "arcane_bellows",
        () -> BlockEntityType.Builder.of(BellowsBlockEntity::new, ModBlocks.ARCANE_BELLOWS.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfernalFurnaceBlockEntity>> INFERNAL_FURNACE = BLOCK_ENTITIES.register(
        "infernal_furnace",
        () -> BlockEntityType.Builder.of(InfernalFurnaceBlockEntity::new, ModBlocks.INFERNAL_FURNACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneEarBlockEntity>> ARCANE_EAR = BLOCK_ENTITIES.register(
        "arcane_ear",
        () -> BlockEntityType.Builder.of(ArcaneEarBlockEntity::new, ModBlocks.ARCANE_EAR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<LevitatorBlockEntity>> LEVITATOR = BLOCK_ENTITIES.register(
        "arcane_levitator",
        () -> BlockEntityType.Builder.of(LevitatorBlockEntity::new, ModBlocks.ARCANE_LEVITATOR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<HungryChestBlockEntity>> HUNGRY_CHEST = BLOCK_ENTITIES.register(
        "hungry_chest",
        () -> BlockEntityType.Builder.of(HungryChestBlockEntity::new, ModBlocks.HUNGRY_CHEST.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CrystalClusterBlockEntity>> CRYSTAL_CLUSTER = BLOCK_ENTITIES.register(
        "crystal_cluster",
        () -> BlockEntityType.Builder.of(
            CrystalClusterBlockEntity::new,
            ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
            ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
            ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
            ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
            ModBlocks.VIS_CRYSTAL_CLUSTER.get(),
            ModBlocks.MIXED_CRYSTAL_CLUSTER.get()
        ).build(null)
    );
    public static final Supplier<BlockEntityType<CrystalCoreBlockEntity>> CRYSTAL_CORE = BLOCK_ENTITIES.register(
        "crystal_core",
        () -> BlockEntityType.Builder.of(CrystalCoreBlockEntity::new, ModBlocks.CRYSTAL_CORE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CrystalCapacitorBlockEntity>> CRYSTAL_CAPACITOR = BLOCK_ENTITIES.register(
        "crystal_capacitor",
        () -> BlockEntityType.Builder.of(CrystalCapacitorBlockEntity::new, ModBlocks.CRYSTAL_CAPACITOR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<MirrorBlockEntity>> MIRROR = BLOCK_ENTITIES.register(
        "magic_mirror",
        () -> BlockEntityType.Builder.of(MirrorBlockEntity::new, ModBlocks.MAGIC_MIRROR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<HoleBlockEntity>> HOLE = BLOCK_ENTITIES.register(
        "hole",
        () -> BlockEntityType.Builder.of(HoleBlockEntity::new, ModBlocks.HOLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneBoreBaseBlockEntity>> ARCANE_BORE_BASE = BLOCK_ENTITIES.register(
        "arcane_bore_base",
        () -> BlockEntityType.Builder.of(ArcaneBoreBaseBlockEntity::new, ModBlocks.ARCANE_BORE_BASE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<ArcaneBoreBlockEntity>> ARCANE_BORE = BLOCK_ENTITIES.register(
        "arcane_bore",
        () -> BlockEntityType.Builder.of(ArcaneBoreBlockEntity::new, ModBlocks.ARCANE_BORE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<OwnedBlockEntity>> OWNED = BLOCK_ENTITIES.register(
        "owned",
        () -> BlockEntityType.Builder.of(OwnedBlockEntity::new, owned().toArray(new Block[0])).build(null)
    );

    private ModBlockEntities() {
    }

    private static Set<Block> owned() {
        Set<Block> blocks = new HashSet<>();
        ModBlocks.WARDED_STONES.values().forEach(block -> blocks.add(block.get()));
        blocks.add(ModBlocks.WARDED_GLASS.get());
        blocks.add(ModBlocks.ARCANE_PRESSURE_PLATE.get());
        blocks.add(ModBlocks.ARCANE_DOOR.get());
        return blocks;
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
