package thaumcraft.registry;

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
import thaumcraft.block.device.CrucibleBlock;
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

    private ModBlocks() {
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
