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
import thaumcraft.blockentity.CrystalCapacitorBlockEntity;
import thaumcraft.blockentity.CrystalClusterBlockEntity;
import thaumcraft.blockentity.CrystalCoreBlockEntity;
import thaumcraft.blockentity.HungryChestBlockEntity;
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
        () -> new BlockEntityType<>(NitorBlockEntity::new, ModBlocks.NITOR.get())
    );

    public static final Supplier<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = BLOCK_ENTITIES.register(
        "crucible",
        () -> new BlockEntityType<>(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get())
    );
    public static final Supplier<BlockEntityType<AlembicBlockEntity>> ALEMBIC = BLOCK_ENTITIES.register(
        "alembic",
        () -> new BlockEntityType<>(AlembicBlockEntity::new, ModBlocks.ALEMBIC.get())
    );
    public static final Supplier<BlockEntityType<ArcaneWorktableBlockEntity>> ARCANE_WORKTABLE = BLOCK_ENTITIES.register(
        "arcane_worktable",
        () -> new BlockEntityType<>(ArcaneWorktableBlockEntity::new, ModBlocks.ARCANE_WORKTABLE.get())
    );
    public static final Supplier<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE = BLOCK_ENTITIES.register(
        "research_table",
        () -> new BlockEntityType<>(ResearchTableBlockEntity::new, ModBlocks.RESEARCH_TABLE.get())
    );
    public static final Supplier<BlockEntityType<InfusionWorkbenchBlockEntity>> INFUSION_WORKBENCH = BLOCK_ENTITIES.register(
        "infusion_workbench",
        () -> new BlockEntityType<>(InfusionWorkbenchBlockEntity::new, ModBlocks.ARCANE_STONE.get())
    );

    public static final Supplier<BlockEntityType<JarBlockEntity>> WARDED_JAR = BLOCK_ENTITIES.register(
        "warded_jar",
        () -> new BlockEntityType<>(JarBlockEntity::new, ModBlocks.WARDED_JAR.get())
    );
    public static final Supplier<BlockEntityType<BrainJarBlockEntity>> BRAIN_JAR = BLOCK_ENTITIES.register(
        "brain_jar",
        () -> new BlockEntityType<>(BrainJarBlockEntity::new, ModBlocks.BRAIN_JAR.get())
    );
    public static final Supplier<BlockEntityType<BellowsBlockEntity>> BELLOWS = BLOCK_ENTITIES.register(
        "arcane_bellows",
        () -> new BlockEntityType<>(BellowsBlockEntity::new, ModBlocks.ARCANE_BELLOWS.get())
    );
    public static final Supplier<BlockEntityType<InfernalFurnaceBlockEntity>> INFERNAL_FURNACE = BLOCK_ENTITIES.register(
        "infernal_furnace",
        () -> new BlockEntityType<>(InfernalFurnaceBlockEntity::new, ModBlocks.INFERNAL_FURNACE.get())
    );
    public static final Supplier<BlockEntityType<ArcaneEarBlockEntity>> ARCANE_EAR = BLOCK_ENTITIES.register(
        "arcane_ear",
        () -> new BlockEntityType<>(ArcaneEarBlockEntity::new, ModBlocks.ARCANE_EAR.get())
    );
    public static final Supplier<BlockEntityType<LevitatorBlockEntity>> LEVITATOR = BLOCK_ENTITIES.register(
        "arcane_levitator",
        () -> new BlockEntityType<>(LevitatorBlockEntity::new, ModBlocks.ARCANE_LEVITATOR.get())
    );
    public static final Supplier<BlockEntityType<HungryChestBlockEntity>> HUNGRY_CHEST = BLOCK_ENTITIES.register(
        "hungry_chest",
        () -> new BlockEntityType<>(HungryChestBlockEntity::new, ModBlocks.HUNGRY_CHEST.get())
    );
    public static final Supplier<BlockEntityType<CrystalClusterBlockEntity>> CRYSTAL_CLUSTER = BLOCK_ENTITIES.register(
        "crystal_cluster",
        () -> new BlockEntityType<>(
            CrystalClusterBlockEntity::new,
            ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
            ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
            ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
            ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
            ModBlocks.VIS_CRYSTAL_CLUSTER.get(),
            ModBlocks.MIXED_CRYSTAL_CLUSTER.get()
        )
    );
    public static final Supplier<BlockEntityType<CrystalCoreBlockEntity>> CRYSTAL_CORE = BLOCK_ENTITIES.register(
        "crystal_core",
        () -> new BlockEntityType<>(CrystalCoreBlockEntity::new, ModBlocks.CRYSTAL_CORE.get())
    );
    public static final Supplier<BlockEntityType<CrystalCapacitorBlockEntity>> CRYSTAL_CAPACITOR = BLOCK_ENTITIES.register(
        "crystal_capacitor",
        () -> new BlockEntityType<>(CrystalCapacitorBlockEntity::new, ModBlocks.CRYSTAL_CAPACITOR.get())
    );
    public static final Supplier<BlockEntityType<OwnedBlockEntity>> OWNED = BLOCK_ENTITIES.register(
        "owned",
        () -> new BlockEntityType<>(OwnedBlockEntity::new, owned())
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
