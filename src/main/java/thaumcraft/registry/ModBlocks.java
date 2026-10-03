package thaumcraft.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.block.device.AlembicBlock;
import thaumcraft.block.device.ArcaneDoorBlock;
import thaumcraft.block.device.ArcaneEarBlock;
import thaumcraft.block.device.ArcanePressurePlateBlock;
import thaumcraft.block.bore.ArcaneBoreBaseBlock;
import thaumcraft.block.bore.ArcaneBoreBlock;
import thaumcraft.block.crystal.CrystalCapacitorBlock;
import thaumcraft.block.crystal.CrystalClusterBlock;
import thaumcraft.block.crystal.CrystalCoreBlock;
import thaumcraft.block.device.HoleBlock;
import thaumcraft.block.device.HungryChestBlock;
import thaumcraft.block.mirror.MirrorBlock;
import thaumcraft.block.device.LevitatorBlock;
import thaumcraft.block.device.ArcaneStoneBlock;
import thaumcraft.block.device.ArcaneWorktableBlock;
import thaumcraft.block.device.ResearchTableBlock;
import thaumcraft.block.device.TableBlock;
import thaumcraft.block.device.BellowsBlock;
import thaumcraft.block.device.CandleBlock;
import thaumcraft.block.device.InfernalFurnaceBlock;
import thaumcraft.block.device.CrucibleBlock;
import thaumcraft.block.device.JarBlock;
import thaumcraft.block.ward.WardedBlock;
import thaumcraft.block.ward.WardedGlassBlock;
import thaumcraft.crafting.ConfigRecipes;
import thaumcraft.block.device.NitorBlock;
import thaumcraft.block.world.AmberBlock;
import thaumcraft.block.world.InfusedStoneBlock;
import thaumcraft.block.world.MagicalFlowerBlock;
import thaumcraft.block.world.MagicalLeavesBlock;
import thaumcraft.block.world.MagicalSaplingBlock;
import thaumcraft.block.world.ObsidianTotemBlock;
import thaumcraft.block.world.TravelPavingStoneBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Thaumcraft.MODID);

    public static final DeferredBlock<Block> CINNABAR_ORE = BLOCKS.registerSimpleBlock("cinnabar_ore", ModBlocks::stone);
    public static final DeferredBlock<InfusedStoneBlock> AIR_INFUSED_STONE = infusedStone("air_infused_stone", 1);
    public static final DeferredBlock<InfusedStoneBlock> FIRE_INFUSED_STONE = infusedStone("fire_infused_stone", 2);
    public static final DeferredBlock<InfusedStoneBlock> WATER_INFUSED_STONE = infusedStone("water_infused_stone", 3);
    public static final DeferredBlock<InfusedStoneBlock> EARTH_INFUSED_STONE = infusedStone("earth_infused_stone", 4);
    public static final DeferredBlock<InfusedStoneBlock> VIS_INFUSED_STONE = infusedStone("vis_infused_stone", 5);
    public static final DeferredBlock<InfusedStoneBlock> DULL_INFUSED_STONE = infusedStone("dull_infused_stone", 6);
    public static final DeferredBlock<Block> AMBER_ORE = BLOCKS.registerSimpleBlock("amber_ore", ModBlocks::stone);
    public static final DeferredBlock<AmberBlock> AMBER_BLOCK = BLOCKS.registerBlock("amber_block", AmberBlock::new, ModBlocks::amber);
    public static final DeferredBlock<AmberBlock> AMBER_BRICKS = BLOCKS.registerBlock("amber_bricks", AmberBlock::new, ModBlocks::amber);
    public static final DeferredBlock<ObsidianTotemBlock> OBSIDIAN_TOTEM = BLOCKS.registerBlock(
        "obsidian_totem",
        ObsidianTotemBlock::new,
        properties -> obsidian(properties)
    );
    public static final DeferredBlock<Block> OBSIDIAN_TILE = BLOCKS.registerSimpleBlock("obsidian_tile", ModBlocks::obsidian);
    public static final DeferredBlock<TravelPavingStoneBlock> TRAVEL_PAVING_STONE = BLOCKS.registerBlock(
        "travel_paving_stone",
        TravelPavingStoneBlock::new,
        properties -> stone(properties).lightLevel(state -> 9)
    );
    public static final DeferredBlock<RotatedPillarBlock> GREATWOOD_LOG = BLOCKS.registerBlock("greatwood_log", RotatedPillarBlock::new, ModBlocks::log);
    public static final DeferredBlock<RotatedPillarBlock> SILVERWOOD_LOG = BLOCKS.registerBlock("silverwood_log", RotatedPillarBlock::new, ModBlocks::log);
    public static final DeferredBlock<MagicalLeavesBlock> GREATWOOD_LEAVES = BLOCKS.registerBlock("greatwood_leaves", MagicalLeavesBlock::new, ModBlocks::leaves);
    public static final DeferredBlock<MagicalLeavesBlock> SILVERWOOD_LEAVES = BLOCKS.registerBlock(
        "silverwood_leaves",
        MagicalLeavesBlock::new,
        properties -> leaves(properties).lightLevel(state -> 7)
    );
    public static final DeferredBlock<MagicalSaplingBlock> GREATWOOD_SAPLING = BLOCKS.registerBlock(
        "greatwood_sapling",
        properties -> new MagicalSaplingBlock(false, properties),
        ModBlocks::plant
    );
    public static final DeferredBlock<MagicalSaplingBlock> SILVERWOOD_SAPLING = BLOCKS.registerBlock(
        "silverwood_sapling",
        properties -> new MagicalSaplingBlock(true, properties),
        properties -> plant(properties).lightLevel(state -> 7)
    );
    public static final DeferredBlock<MagicalFlowerBlock> SHIMMERLEAF = BLOCKS.registerBlock(
        "shimmerleaf",
        properties -> new MagicalFlowerBlock(false, properties),
        properties -> plant(properties).lightLevel(state -> 7)
    );
    public static final DeferredBlock<MagicalFlowerBlock> CINDERPEARL = BLOCKS.registerBlock(
        "cinderpearl",
        properties -> new MagicalFlowerBlock(true, properties),
        properties -> plant(properties).lightLevel(state -> 7)
    );
    public static final DeferredBlock<NitorBlock> NITOR = BLOCKS.registerBlock(
        "nitor",
        NitorBlock::new,
        properties -> properties.noCollision().instabreak().sound(SoundType.WOOL).lightLevel(state -> 15).pushReaction(PushReaction.POPPED)
    );

    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.registerBlock("crucible", CrucibleBlock::new, ModBlocks::metalDevice);
    public static final DeferredBlock<AlembicBlock> ALEMBIC = BLOCKS.registerBlock("alembic", AlembicBlock::new, ModBlocks::metalDevice);
    public static final DeferredBlock<TableBlock> TABLE = BLOCKS.registerBlock("table", TableBlock::new, ModBlocks::table);
    public static final DeferredBlock<ResearchTableBlock> RESEARCH_TABLE = BLOCKS.registerBlock(
        "research_table",
        ResearchTableBlock::new,
        properties -> table(properties).pushReaction(PushReaction.IMMOVEABLE)
    );
    public static final DeferredBlock<ArcaneWorktableBlock> ARCANE_WORKTABLE = BLOCKS.registerBlock("arcane_worktable", ArcaneWorktableBlock::new, ModBlocks::table);
    public static final DeferredBlock<ArcaneStoneBlock> ARCANE_STONE = BLOCKS.registerBlock(
        "arcane_stone",
        ArcaneStoneBlock::new,
        properties -> stone(properties).strength(4.0F, 100.0F).noOcclusion()
    );
    public static final DeferredBlock<Block> ARCANE_WOOD = BLOCKS.registerSimpleBlock(
        "arcane_wood",
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 6.0F).sound(SoundType.WOOD)
    );
    public static final Map<String, DeferredBlock<WardedBlock>> WARDED_STONES = colored(
        ConfigRecipes.DYE_COLORS,
        "_warded_stone",
        WardedBlock::new,
        properties -> stone(properties).strength(10.0F, 599.0F).pushReaction(PushReaction.IMMOVEABLE).overrideDescription("block.thaumcraft.warded_stone")
    );
    public static final DeferredBlock<WardedGlassBlock> WARDED_GLASS = BLOCKS.registerBlock(
        "warded_glass",
        WardedGlassBlock::new,
        properties -> properties
            .mapColor(MapColor.NONE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0F, 999.0F)
            .sound(SoundType.STONE)
            .noOcclusion()
            .pushReaction(PushReaction.IMMOVEABLE)
            .isValidSpawn((state, level, pos, entity) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, aabb) -> false)
    );
    public static final Map<String, DeferredBlock<CandleBlock>> CANDLES = colored(
        ConfigRecipes.WOOL_COLORS,
        "_tallow_candle",
        CandleBlock::new,
        properties -> properties
            .mapColor(MapColor.WOOL)
            .noCollision()
            .strength(0.1F)
            .sound(SoundType.WOOL)
            .lightLevel(state -> 14)
            .pushReaction(PushReaction.POPPED)
    );
    public static final Map<String, DeferredBlock<Block>> MARKERS = colored(
        ConfigRecipes.WOOL_COLORS,
        "_marker",
        Block::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 6.0F).sound(SoundType.WOOD)
    );
    public static final DeferredBlock<JarBlock> WARDED_JAR = BLOCKS.registerBlock("warded_jar", properties -> new JarBlock(false, properties), ModBlocks::jar);
    public static final DeferredBlock<JarBlock> BRAIN_JAR = BLOCKS.registerBlock("brain_jar", properties -> new JarBlock(true, properties), ModBlocks::jar);
    public static final DeferredBlock<BellowsBlock> ARCANE_BELLOWS = BLOCKS.registerBlock(
        "arcane_bellows",
        BellowsBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 6.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.POPPED)
    );
    public static final DeferredBlock<InfernalFurnaceBlock> INFERNAL_FURNACE = BLOCKS.registerBlock(
        "infernal_furnace",
        InfernalFurnaceBlock::new,
        properties -> properties
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(10.0F, 599.0F)
            .lightLevel(InfernalFurnaceBlock::lightLevel)
            .pushReaction(PushReaction.IMMOVEABLE)
            .noLootTable()
    );
    public static final DeferredBlock<ArcaneEarBlock> ARCANE_EAR = BLOCKS.registerBlock(
        "arcane_ear",
        ArcaneEarBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 10.0F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredBlock<ArcanePressurePlateBlock> ARCANE_PRESSURE_PLATE = BLOCKS.registerBlock(
        "arcane_pressure_plate",
        ArcanePressurePlateBlock::new,
        properties -> properties
            .mapColor(MapColor.WOOD)
            .instrument(NoteBlockInstrument.BASS)
            .noCollision()
            .strength(2.0F, 999.0F)
            .sound(SoundType.WOOD)
            .pushReaction(PushReaction.IMMOVEABLE)
    );
    public static final DeferredBlock<ArcaneDoorBlock> ARCANE_DOOR = BLOCKS.registerBlock(
        "arcane_door",
        ArcaneDoorBlock::new,
        properties -> properties.mapColor(MapColor.METAL).strength(15.0F, 999.0F).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
    );
    public static final DeferredBlock<LevitatorBlock> ARCANE_LEVITATOR = BLOCKS.registerBlock(
        "arcane_levitator",
        LevitatorBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 15.0F).sound(SoundType.WOOD)
    );
    public static final DeferredBlock<HungryChestBlock> HUNGRY_CHEST = BLOCKS.registerBlock(
        "hungry_chest",
        HungryChestBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredBlock<CrystalClusterBlock> AIR_CRYSTAL_CLUSTER = crystal("air_crystal_cluster", 0);
    public static final DeferredBlock<CrystalClusterBlock> FIRE_CRYSTAL_CLUSTER = crystal("fire_crystal_cluster", 1);
    public static final DeferredBlock<CrystalClusterBlock> WATER_CRYSTAL_CLUSTER = crystal("water_crystal_cluster", 2);
    public static final DeferredBlock<CrystalClusterBlock> EARTH_CRYSTAL_CLUSTER = crystal("earth_crystal_cluster", 3);
    public static final DeferredBlock<CrystalClusterBlock> VIS_CRYSTAL_CLUSTER = crystal("vis_crystal_cluster", 4);
    public static final DeferredBlock<CrystalClusterBlock> MIXED_CRYSTAL_CLUSTER = crystal("mixed_crystal_cluster", 5);
    public static final DeferredBlock<CrystalCoreBlock> CRYSTAL_CORE = BLOCKS.registerBlock("crystal_core", CrystalCoreBlock::new, ModBlocks::crystal);
    public static final DeferredBlock<CrystalCapacitorBlock> CRYSTAL_CAPACITOR = BLOCKS.registerBlock("crystal_capacitor", CrystalCapacitorBlock::new, ModBlocks::crystal);
    public static final DeferredBlock<MirrorBlock> MAGIC_MIRROR = BLOCKS.registerBlock(
        "magic_mirror",
        MirrorBlock::new,
        properties -> properties.mapColor(MapColor.NONE).strength(1.0F, 10.0F).sound(ModSounds.MIRROR_SOUND).noOcclusion().noCollision()
    );
    public static final DeferredBlock<HoleBlock> HOLE = BLOCKS.registerBlock(
        "hole",
        HoleBlock::new,
        properties -> properties
            .mapColor(MapColor.NONE)
            .strength(-1.0F, 6000000.0F)
            .sound(SoundType.STONE)
            .lightLevel(state -> 10)
            .noCollision()
            .noOcclusion()
            .noLootTable()
            .pushReaction(PushReaction.IMMOVEABLE)
            .isValidSpawn((state, level, pos, entity) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, aabb) -> false)
    );
    public static final DeferredBlock<ArcaneBoreBaseBlock> ARCANE_BORE_BASE = BLOCKS.registerBlock(
        "arcane_bore_base",
        ArcaneBoreBaseBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 10.0F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredBlock<ArcaneBoreBlock> ARCANE_BORE = BLOCKS.registerBlock(
        "arcane_bore",
        ArcaneBoreBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F, 10.0F).sound(SoundType.WOOD).noOcclusion()
    );

    private ModBlocks() {
    }

    private static <B extends Block> Map<String, DeferredBlock<B>> colored(
        String[] colors,
        String suffix,
        Function<BlockBehaviour.Properties, B> factory,
        UnaryOperator<BlockBehaviour.Properties> properties
    ) {
        Map<String, DeferredBlock<B>> blocks = new LinkedHashMap<>();
        for (String color : colors) {
            blocks.put(color, BLOCKS.registerBlock(color + suffix, factory, properties));
        }
        return blocks;
    }

    private static DeferredBlock<CrystalClusterBlock> crystal(String name, int type) {
        return BLOCKS.registerBlock(name, properties -> new CrystalClusterBlock(type, properties), ModBlocks::crystal);
    }

    private static BlockBehaviour.Properties crystal(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.NONE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.7F, 1.0F)
            .lightLevel(state -> 6)
            .sound(ModSounds.CRYSTAL_SOUND)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, aabb) -> false);
    }

    private static DeferredBlock<InfusedStoneBlock> infusedStone(String name, int type) {
        return BLOCKS.registerBlock(name, properties -> new InfusedStoneBlock(type, properties), ModBlocks::stone);
    }

    static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(1.5F, 3.0F)
            .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties metalDevice(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(3.0F, 17.0F)
            .sound(SoundType.METAL)
            .noOcclusion();
    }

    private static BlockBehaviour.Properties jar(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.NONE)
            .strength(0.3F)
            .sound(ModSounds.JAR_SOUND)
            .lightLevel(state -> 9)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, aabb) -> false);
    }

    private static BlockBehaviour.Properties table(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).noOcclusion();
    }

    private static BlockBehaviour.Properties amber(BlockBehaviour.Properties properties) {
        return stone(properties).mapColor(MapColor.COLOR_ORANGE).noOcclusion();
    }

    private static BlockBehaviour.Properties obsidian(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(35.0F, 999.0F)
            .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties log(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties leaves(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.PLANT)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isSuffocating((state, level, pos) -> false)
            .pushReaction(PushReaction.POPPED)
            .isRedstoneConductor((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties plant(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.PLANT)
            .noCollision()
            .instabreak()
            .randomTicks()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.NONE)
            .pushReaction(PushReaction.POPPED);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
