package thaumcraft.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.item.AlumentumItem;
import thaumcraft.item.ArcaneKeyItem;
import thaumcraft.item.CrystalCapacitorItem;
import thaumcraft.item.HandMirrorItem;
import thaumcraft.item.MirrorItem;
import thaumcraft.item.PortableHoleItem;
import thaumcraft.item.EssenceItem;
import thaumcraft.item.EssentiaPhialItem;
import thaumcraft.item.FilledJarItem;
import thaumcraft.item.ModMaterials;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.item.ThaumonomiconItem;
import thaumcraft.item.WispEssenceItem;
import thaumcraft.item.golem.GolemCoreItem;
import thaumcraft.item.golem.GolemDecorationItem;
import thaumcraft.item.golem.GolemPlacerItem;
import thaumcraft.entity.golem.GolemKind;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.item.wand.ExcavationWandItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Thaumcraft.MODID);

    public static final DeferredItem<BlockItem> CINNABAR_ORE = block(ModBlocks.CINNABAR_ORE);
    public static final DeferredItem<BlockItem> AIR_INFUSED_STONE = block(ModBlocks.AIR_INFUSED_STONE);
    public static final DeferredItem<BlockItem> FIRE_INFUSED_STONE = block(ModBlocks.FIRE_INFUSED_STONE);
    public static final DeferredItem<BlockItem> WATER_INFUSED_STONE = block(ModBlocks.WATER_INFUSED_STONE);
    public static final DeferredItem<BlockItem> EARTH_INFUSED_STONE = block(ModBlocks.EARTH_INFUSED_STONE);
    public static final DeferredItem<BlockItem> VIS_INFUSED_STONE = block(ModBlocks.VIS_INFUSED_STONE);
    public static final DeferredItem<BlockItem> DULL_INFUSED_STONE = block(ModBlocks.DULL_INFUSED_STONE);
    public static final DeferredItem<BlockItem> AMBER_ORE = block(ModBlocks.AMBER_ORE);
    public static final DeferredItem<BlockItem> AMBER_BLOCK = block(ModBlocks.AMBER_BLOCK);
    public static final DeferredItem<BlockItem> AMBER_BRICKS = block(ModBlocks.AMBER_BRICKS);
    public static final DeferredItem<BlockItem> OBSIDIAN_TOTEM = block(ModBlocks.OBSIDIAN_TOTEM);
    public static final DeferredItem<BlockItem> OBSIDIAN_TILE = block(ModBlocks.OBSIDIAN_TILE);
    public static final DeferredItem<BlockItem> TRAVEL_PAVING_STONE = block(ModBlocks.TRAVEL_PAVING_STONE);
    public static final DeferredItem<BlockItem> GREATWOOD_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.GREATWOOD_LOG, properties -> properties.cookingFuel(fuel("magical_log")));
    public static final DeferredItem<BlockItem> SILVERWOOD_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.SILVERWOOD_LOG, properties -> properties.cookingFuel(fuel("magical_log")));
    public static final DeferredItem<BlockItem> GREATWOOD_LEAVES = block(ModBlocks.GREATWOOD_LEAVES);
    public static final DeferredItem<BlockItem> SILVERWOOD_LEAVES = block(ModBlocks.SILVERWOOD_LEAVES);
    public static final DeferredItem<BlockItem> GREATWOOD_SAPLING = block(ModBlocks.GREATWOOD_SAPLING);
    public static final DeferredItem<BlockItem> SILVERWOOD_SAPLING = block(ModBlocks.SILVERWOOD_SAPLING);
    public static final DeferredItem<BlockItem> SHIMMERLEAF = block(ModBlocks.SHIMMERLEAF);
    public static final DeferredItem<BlockItem> CINDERPEARL = block(ModBlocks.CINDERPEARL);

    public static final DeferredItem<AlumentumItem> ALUMENTUM = ITEMS.registerItem("alumentum", AlumentumItem::new, properties -> properties.cookingFuel(fuel("alumentum")));
    public static final DeferredItem<BlockItem> NITOR = block(ModBlocks.NITOR);
    public static final DeferredItem<Item> THAUMIUM_INGOT = ITEMS.registerSimpleItem("thaumium_ingot");
    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> MAGIC_TALLOW = ITEMS.registerSimpleItem("magic_tallow");
    public static final DeferredItem<Item> ZOMBIE_BRAIN = ITEMS.registerSimpleItem("zombie_brain");
    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber");
    public static final DeferredItem<Item> ENCHANTED_FABRIC = ITEMS.registerSimpleItem("enchanted_fabric");
    public static final DeferredItem<Item> FLUX_FILTER = ITEMS.registerSimpleItem("flux_filter");
    public static final DeferredItem<Item> KNOWLEDGE_FRAGMENT = ITEMS.registerSimpleItem("knowledge_fragment");
    public static final DeferredItem<Item> MIRRORED_GLASS = ITEMS.registerSimpleItem("mirrored_glass");

    public static final DeferredItem<Item> AIR_SHARD = ITEMS.registerSimpleItem("air_shard");
    public static final DeferredItem<Item> FIRE_SHARD = ITEMS.registerSimpleItem("fire_shard");
    public static final DeferredItem<Item> WATER_SHARD = ITEMS.registerSimpleItem("water_shard");
    public static final DeferredItem<Item> EARTH_SHARD = ITEMS.registerSimpleItem("earth_shard");
    public static final DeferredItem<Item> VIS_SHARD = ITEMS.registerSimpleItem("vis_shard");
    public static final DeferredItem<Item> DULL_SHARD = ITEMS.registerSimpleItem("dull_shard");

    public static final DeferredItem<Item> TIN_NUGGET = ITEMS.registerSimpleItem("tin_nugget");
    public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.registerSimpleItem("silver_nugget");
    public static final DeferredItem<Item> LEAD_NUGGET = ITEMS.registerSimpleItem("lead_nugget");
    public static final DeferredItem<Item> QUICKSILVER_DROP = ITEMS.registerSimpleItem("quicksilver_drop");
    public static final DeferredItem<Item> NATIVE_IRON_CLUSTER = ITEMS.registerSimpleItem("native_iron_cluster");
    public static final DeferredItem<Item> NATIVE_COPPER_CLUSTER = ITEMS.registerSimpleItem("native_copper_cluster");
    public static final DeferredItem<Item> NATIVE_TIN_CLUSTER = ITEMS.registerSimpleItem("native_tin_cluster");
    public static final DeferredItem<Item> NATIVE_SILVER_CLUSTER = ITEMS.registerSimpleItem("native_silver_cluster");
    public static final DeferredItem<Item> NATIVE_LEAD_CLUSTER = ITEMS.registerSimpleItem("native_lead_cluster");
    public static final DeferredItem<Item> NATIVE_GOLD_CLUSTER = ITEMS.registerSimpleItem("native_gold_cluster");

    public static final DeferredItem<Item> CHICKEN_NUGGET = nugget("chicken_nugget");
    public static final DeferredItem<Item> BEEF_NUGGET = nugget("beef_nugget");
    public static final DeferredItem<Item> PORK_NUGGET = nugget("pork_nugget");
    public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = ITEMS.registerSimpleItem(
        "triple_meat_treat",
        properties -> properties.food(
            new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).alwaysEdible().build(),
            Consumables.defaultFood()
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66F))
                .build()
        )
    );

    public static final DeferredItem<Item> GOGGLES_OF_REVEALING = armor("goggles_of_revealing", ModMaterials.GOGGLES_ARMOR, ArmorType.HELMET, 5);
    public static final DeferredItem<Item> THAUMIUM_HELMET = armor("thaumium_helmet", ModMaterials.THAUMIUM_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<Item> THAUMIUM_CHESTPLATE = armor("thaumium_chestplate", ModMaterials.THAUMIUM_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> THAUMIUM_LEGGINGS = armor("thaumium_leggings", ModMaterials.THAUMIUM_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> THAUMIUM_BOOTS = armor("thaumium_boots", ModMaterials.THAUMIUM_ARMOR, ArmorType.BOOTS);
    public static final DeferredItem<Item> ROBE_CHESTPLATE = armor("robe_chestplate", ModMaterials.ROBE_ARMOR, ArmorType.CHESTPLATE, 2);
    public static final DeferredItem<Item> ROBE_LEGGINGS = armor("robe_leggings", ModMaterials.ROBE_ARMOR, ArmorType.LEGGINGS, 2);
    public static final DeferredItem<Item> ROBE_BOOTS = armor("robe_boots", ModMaterials.ROBE_ARMOR, ArmorType.BOOTS, 1);
    public static final DeferredItem<Item> BOOTS_TRAVELLER = armor("boots_traveller", ModMaterials.TRAVELLER_ARMOR, ArmorType.BOOTS);

    public static final DeferredItem<Item> THAUMIUM_SWORD = ITEMS.registerSimpleItem("thaumium_sword", properties -> properties.sword(ModMaterials.THAUMIUM_TOOL, 3.0F, -2.4F));
    public static final DeferredItem<Item> THAUMIUM_PICKAXE = ITEMS.registerSimpleItem("thaumium_pickaxe", properties -> properties.pickaxe(ModMaterials.THAUMIUM_TOOL, 1.0F, -2.8F));
    public static final DeferredItem<Item> THAUMIUM_AXE = ITEMS.registerSimpleItem("thaumium_axe", properties -> properties.axe(ModMaterials.THAUMIUM_TOOL, 2.0F, -3.0F));
    public static final DeferredItem<Item> THAUMIUM_SHOVEL = ITEMS.registerSimpleItem("thaumium_shovel", properties -> properties.shovel(ModMaterials.THAUMIUM_TOOL, 0.0F, -3.0F));
    public static final DeferredItem<Item> THAUMIUM_HOE = ITEMS.registerSimpleItem("thaumium_hoe", properties -> properties.hoe(ModMaterials.THAUMIUM_TOOL, -2.0F, -1.0F));
    public static final DeferredItem<Item> ELEMENTAL_SWORD = ITEMS.registerSimpleItem("elemental_sword", properties -> properties.sword(ModMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F));
    public static final DeferredItem<Item> ELEMENTAL_PICKAXE = ITEMS.registerSimpleItem("elemental_pickaxe", properties -> properties.pickaxe(ModMaterials.ELEMENTAL_TOOL, 1.0F, -2.8F));
    public static final DeferredItem<Item> ELEMENTAL_AXE = ITEMS.registerSimpleItem("elemental_axe", properties -> properties.axe(ModMaterials.ELEMENTAL_TOOL, 2.0F, -3.0F));
    public static final DeferredItem<Item> ELEMENTAL_SHOVEL = ITEMS.registerSimpleItem("elemental_shovel", properties -> properties.shovel(ModMaterials.ELEMENTAL_TOOL, 0.0F, -3.0F));
    public static final DeferredItem<Item> ELEMENTAL_HOE = ITEMS.registerSimpleItem("elemental_hoe", properties -> properties.hoe(ModMaterials.ELEMENTAL_TOOL, -3.0F, -1.0F));

    public static final DeferredItem<BlockItem> CRUCIBLE = block(ModBlocks.CRUCIBLE);
    public static final DeferredItem<BlockItem> ALEMBIC = block(ModBlocks.ALEMBIC);
    public static final DeferredItem<BlockItem> TABLE = block(ModBlocks.TABLE);
    public static final DeferredItem<BlockItem> ARCANE_WORKTABLE = block(ModBlocks.ARCANE_WORKTABLE);
    public static final DeferredItem<BlockItem> ARCANE_STONE = block(ModBlocks.ARCANE_STONE);
    public static final DeferredItem<BlockItem> ARCANE_WOOD = block(ModBlocks.ARCANE_WOOD);
    public static final Map<String, DeferredItem<BlockItem>> WARDED_STONES = blocks(
        ModBlocks.WARDED_STONES,
        properties -> properties.overrideDescription("block.thaumcraft.warded_stone")
    );
    public static final DeferredItem<BlockItem> WARDED_GLASS = block(ModBlocks.WARDED_GLASS);
    public static final Map<String, DeferredItem<BlockItem>> CANDLES = blocks(ModBlocks.CANDLES, properties -> properties);
    public static final Map<String, DeferredItem<BlockItem>> MARKERS = blocks(ModBlocks.MARKERS, properties -> properties);
    public static final DeferredItem<BlockItem> WARDED_JAR = block(ModBlocks.WARDED_JAR);
    public static final DeferredItem<BlockItem> BRAIN_JAR = block(ModBlocks.BRAIN_JAR);
    public static final DeferredItem<FilledJarItem> FILLED_JAR = ITEMS.registerItem("filled_jar", properties -> new FilledJarItem(properties.stacksTo(8)));
    public static final DeferredItem<BlockItem> ARCANE_BELLOWS = block(ModBlocks.ARCANE_BELLOWS);
    public static final DeferredItem<BlockItem> ARCANE_EAR = block(ModBlocks.ARCANE_EAR);
    public static final DeferredItem<BlockItem> ARCANE_PRESSURE_PLATE = block(ModBlocks.ARCANE_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> ARCANE_LEVITATOR = block(ModBlocks.ARCANE_LEVITATOR);
    public static final DeferredItem<BlockItem> HUNGRY_CHEST = block(ModBlocks.HUNGRY_CHEST);
    public static final DeferredItem<BlockItem> AIR_CRYSTAL_CLUSTER = block(ModBlocks.AIR_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> FIRE_CRYSTAL_CLUSTER = block(ModBlocks.FIRE_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> WATER_CRYSTAL_CLUSTER = block(ModBlocks.WATER_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> EARTH_CRYSTAL_CLUSTER = block(ModBlocks.EARTH_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> VIS_CRYSTAL_CLUSTER = block(ModBlocks.VIS_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> MIXED_CRYSTAL_CLUSTER = block(ModBlocks.MIXED_CRYSTAL_CLUSTER);
    public static final DeferredItem<BlockItem> CRYSTAL_CORE = block(ModBlocks.CRYSTAL_CORE);
    public static final DeferredItem<MirrorItem> MAGIC_MIRROR = ITEMS.registerItem(
        "magic_mirror",
        properties -> new MirrorItem(ModBlocks.MAGIC_MIRROR.get(), properties.rarity(Rarity.UNCOMMON).useBlockDescriptionPrefix())
    );
    public static final DeferredItem<HandMirrorItem> HAND_MIRROR = ITEMS.registerItem("hand_mirror", properties -> new HandMirrorItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<PortableHoleItem> PORTABLE_HOLE = ITEMS.registerItem(
        "portable_hole",
        properties -> new PortableHoleItem(properties.durability(321).rarity(Rarity.RARE).setNoCombineRepair())
    );
    public static final DeferredItem<BlockItem> ARCANE_BORE_BASE = block(ModBlocks.ARCANE_BORE_BASE);
    public static final DeferredItem<BlockItem> ARCANE_BORE = block(ModBlocks.ARCANE_BORE);
    public static final DeferredItem<ExcavationWandItem> WAND_EXCAVATION = ITEMS.registerItem(
        "wand_excavation",
        properties -> new ExcavationWandItem(properties.durability(2000).rarity(Rarity.RARE).setNoCombineRepair())
    );
    public static final DeferredItem<CrystalCapacitorItem> CRYSTAL_CAPACITOR = ITEMS.registerItem(
        "crystal_capacitor",
        properties -> new CrystalCapacitorItem(ModBlocks.CRYSTAL_CAPACITOR.get(), properties.useBlockDescriptionPrefix())
    );
    public static final DeferredItem<DoubleHighBlockItem> ARCANE_DOOR = ITEMS.registerItem(
        "arcane_door",
        properties -> new DoubleHighBlockItem(ModBlocks.ARCANE_DOOR.get(), properties.stacksTo(1).useBlockDescriptionPrefix())
    );
    public static final DeferredItem<ArcaneKeyItem> IRON_ARCANE_KEY = ITEMS.registerItem(
        "iron_arcane_key",
        properties -> new ArcaneKeyItem(0, properties.rarity(Rarity.UNCOMMON))
    );
    public static final DeferredItem<ArcaneKeyItem> GOLD_ARCANE_KEY = ITEMS.registerItem(
        "gold_arcane_key",
        properties -> new ArcaneKeyItem(1, properties.rarity(Rarity.UNCOMMON))
    );
    public static final DeferredItem<CastingWandItem> WAND_APPRENTICE = wand("wand_apprentice", 50, 10, Rarity.UNCOMMON);
    public static final DeferredItem<CastingWandItem> WAND_ADEPT = wand("wand_adept", 250, 7, Rarity.RARE);
    public static final DeferredItem<CastingWandItem> WAND_THAUMATURGE = wand("wand_thaumaturge", 1000, 5, Rarity.EPIC);
    public static final DeferredItem<EssentiaPhialItem> ESSENTIA_PHIAL = ITEMS.registerItem("essentia_phial", EssentiaPhialItem::new);
    public static final DeferredItem<EssenceItem> ESSENCE = ITEMS.registerItem("essence", EssenceItem::new);
    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON = ITEMS.registerItem(
        "thaumonomicon",
        properties -> new ThaumonomiconItem(false, properties.stacksTo(1).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON_CHEAT = ITEMS.registerItem(
        "thaumonomicon_cheat",
        properties -> new ThaumonomiconItem(true, properties.stacksTo(1).rarity(Rarity.EPIC))
    );
    public static final DeferredItem<Item> THAUMOMETER = ITEMS.registerSimpleItem("thaumometer", properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ResearchNotesItem> RESEARCH_NOTES = ITEMS.registerItem("research_notes", properties -> new ResearchNotesItem(properties.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<ResearchNotesItem> DISCOVERY = ITEMS.registerItem("discovery", properties -> new ResearchNotesItem(properties.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<ScribingToolsItem> SCRIBING_TOOLS = ITEMS.registerItem("scribing_tools", properties -> new ScribingToolsItem(properties.durability(50)));
    public static final Map<Integer, DeferredItem<GolemCoreItem>> GOLEM_CORES = golemCores();
    public static final DeferredItem<GolemDecorationItem> GOLEM_TOP_HAT = decoration("top_hat", "H");
    public static final DeferredItem<GolemDecorationItem> GOLEM_SPECTACLES = decoration("spectacles", "G");
    public static final DeferredItem<GolemDecorationItem> GOLEM_BOWTIE = decoration("bowtie", "B");
    public static final DeferredItem<GolemDecorationItem> GOLEM_FEZ = decoration("fez", "F");
    public static final DeferredItem<GolemDecorationItem> GOLEM_DART_LAUNCHER = decoration("dart_launcher", "R");
    public static final DeferredItem<GolemDecorationItem> GOLEM_VISOR = decoration("visor", "V");
    public static final DeferredItem<GolemDecorationItem> GOLEM_IRON_PLATING = decoration("iron_plating", "P");
    public static final Map<GolemKind, DeferredItem<GolemPlacerItem>> GOLEMS = golems();
    public static final DeferredItem<SpawnEggItem> BRAINY_ZOMBIE_SPAWN_EGG = ITEMS.registerItem(
        "brainy_zombie_spawn_egg",
        SpawnEggItem::new,
        properties -> properties.spawnEgg(ModEntities.BRAINY_ZOMBIE.get())
    );
    public static final DeferredItem<SpawnEggItem> WISP_SPAWN_EGG = ITEMS.registerItem(
        "wisp_spawn_egg",
        SpawnEggItem::new,
        properties -> properties.spawnEgg(ModEntities.WISP.get())
    );
    public static final DeferredItem<WispEssenceItem> WISP_ESSENCE = ITEMS.registerItem("wisp_essence", WispEssenceItem::new);
    public static final DeferredItem<SpawnEggItem> FIRE_BAT_SPAWN_EGG = ITEMS.registerItem(
        "fire_bat_spawn_egg",
        SpawnEggItem::new,
        properties -> properties.spawnEgg(ModEntities.FIRE_BAT.get())
    );

    private ModItems() {
    }

    private static ResourceKey<ContextIntProvider> fuel(String name) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Thaumcraft.id("cooking/time_" + name));
    }

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type) {
        return ITEMS.registerSimpleItem(name, properties -> properties.humanoidArmor(material, type));
    }

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type, int visDiscount) {
        return ITEMS.registerSimpleItem(name, properties -> properties.humanoidArmor(material, type).component(ModDataComponents.VIS_DISCOUNT.get(), visDiscount));
    }

    private static DeferredItem<CastingWandItem> wand(String name, int maxVis, int interval, Rarity rarity) {
        return ITEMS.registerItem(name, properties -> new CastingWandItem(maxVis, interval, properties.rarity(rarity)));
    }

    private static Map<String, DeferredItem<BlockItem>> blocks(
        Map<String, ? extends DeferredBlock<?>> blocks,
        UnaryOperator<Item.Properties> properties
    ) {
        Map<String, DeferredItem<BlockItem>> items = new LinkedHashMap<>();
        blocks.forEach((color, block) -> items.put(color, ITEMS.registerSimpleBlockItem(block, properties)));
        return items;
    }

    private static Map<Integer, DeferredItem<GolemCoreItem>> golemCores() {
        Map<Integer, DeferredItem<GolemCoreItem>> cores = new LinkedHashMap<>();
        for (int core = 0; core < GolemCoreItem.NAMES.length; core++) {
            int index = core;
            cores.put(core, ITEMS.registerItem("golem_core_" + GolemCoreItem.NAMES[core], properties -> new GolemCoreItem(index, properties.rarity(Rarity.RARE))));
        }
        return cores;
    }

    private static DeferredItem<GolemDecorationItem> decoration(String name, String letter) {
        return ITEMS.registerItem("golem_" + name, properties -> new GolemDecorationItem(letter, name, properties));
    }

    private static Map<GolemKind, DeferredItem<GolemPlacerItem>> golems() {
        Map<GolemKind, DeferredItem<GolemPlacerItem>> golems = new LinkedHashMap<>();
        for (GolemKind kind : GolemKind.values()) {
            golems.put(kind, ITEMS.registerItem(
                kind.id(),
                properties -> new GolemPlacerItem(kind, properties.stacksTo(16).component(ModDataComponents.GOLEM_CORE.get(), 0))
            ));
        }
        return golems;
    }

    private static DeferredItem<BlockItem> block(DeferredBlock<?> block) {
        return ITEMS.registerSimpleBlockItem(block);
    }

    private static DeferredItem<Item> nugget(String name) {
        Consumable consumable = Consumables.defaultFood().consumeSeconds(0.5F).build();
        return ITEMS.registerSimpleItem(
            name,
            properties -> properties.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build(), consumable)
        );
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
