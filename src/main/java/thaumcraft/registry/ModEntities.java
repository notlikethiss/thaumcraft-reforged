package thaumcraft.registry;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.FollowingItem;
import thaumcraft.entity.SpecialItem;
import thaumcraft.entity.golem.AdvancedClayGolem;
import thaumcraft.entity.golem.AdvancedStoneGolem;
import thaumcraft.entity.golem.ClayGolem;
import thaumcraft.entity.golem.DecantingGolem;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.IronGuardianGolem;
import thaumcraft.entity.golem.StoneGolem;
import thaumcraft.entity.golem.StrawGolem;
import thaumcraft.entity.golem.TallowGolem;
import thaumcraft.entity.golem.WoodGolem;
import thaumcraft.entity.monster.BrainyZombie;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.entity.monster.GiantBrainyZombie;
import thaumcraft.entity.monster.Wisp;
import thaumcraft.entity.projectile.Alumentum;
import thaumcraft.entity.projectile.Dart;
import thaumcraft.entity.projectile.FrostShard;

public final class ModEntities {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Thaumcraft.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BrainyZombie>> BRAINY_ZOMBIE = ENTITIES.registerEntityType(
        "brainy_zombie",
        BrainyZombie::new,
        MobCategory.MONSTER,
        builder -> builder.sized(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).ridingOffset(-0.7F).clientTrackingRange(4).updateInterval(3).notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<GiantBrainyZombie>> GIANT_BRAINY_ZOMBIE = ENTITIES.registerEntityType(
        "giant_brainy_zombie",
        GiantBrainyZombie::new,
        MobCategory.MONSTER,
        builder -> builder.sized(0.6F, 1.8F).eyeHeight(1.62F).clientTrackingRange(4).updateInterval(3).notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<FireBat>> FIRE_BAT = ENTITIES.registerEntityType(
        "fire_bat",
        FireBat::new,
        MobCategory.MONSTER,
        builder -> builder.sized(0.5F, 0.9F).eyeHeight(0.45F).fireImmune().clientTrackingRange(4).updateInterval(3).notInPeaceful()
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Wisp>> WISP = ENTITIES.registerEntityType(
        "wisp",
        Wisp::new,
        MobCategory.MONSTER,
        builder -> builder.sized(0.9F, 0.9F).eyeHeight(0.45F).clientTrackingRange(4).updateInterval(3).notInPeaceful()
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Alumentum>> ALUMENTUM = ENTITIES.registerEntityType(
        "alumentum",
        Alumentum::new,
        MobCategory.MISC,
        builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<Dart>> DART = ENTITIES.registerEntityType(
        "dart",
        Dart::new,
        MobCategory.MISC,
        builder -> builder.sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<FrostShard>> FROST_SHARD = ENTITIES.registerEntityType(
        "frost_shard",
        FrostShard::new,
        MobCategory.MISC,
        builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SpecialItem>> SPECIAL_ITEM = ENTITIES.registerEntityType(
        "special_item",
        SpecialItem::new,
        MobCategory.MISC,
        builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<FollowingItem>> FOLLOWING_ITEM = ENTITIES.registerEntityType(
        "following_item",
        FollowingItem::new,
        MobCategory.MISC,
        builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<WoodGolem>> WOOD_GOLEM = golem("wood_golem", WoodGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ClayGolem>> CLAY_GOLEM = golem("clay_golem", ClayGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<StoneGolem>> STONE_GOLEM = golem("stone_golem", StoneGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<TallowGolem>> TALLOW_GOLEM = golem("tallow_golem", TallowGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<StrawGolem>> STRAW_GOLEM = golem("straw_golem", StrawGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<AdvancedClayGolem>> ADVANCED_CLAY_GOLEM = golem("advanced_clay_golem", AdvancedClayGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<AdvancedStoneGolem>> ADVANCED_STONE_GOLEM = golem("advanced_stone_golem", AdvancedStoneGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<IronGuardianGolem>> IRON_GUARDIAN_GOLEM = golem("iron_guardian_golem", IronGuardianGolem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<DecantingGolem>> DECANTING_GOLEM = golem("decanting_golem", DecantingGolem::new);
    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<? extends GolemBase>>> GOLEMS = List.of(
        WOOD_GOLEM, CLAY_GOLEM, STONE_GOLEM, TALLOW_GOLEM, STRAW_GOLEM, ADVANCED_CLAY_GOLEM, ADVANCED_STONE_GOLEM, IRON_GUARDIAN_GOLEM, DECANTING_GOLEM
    );

    private ModEntities() {
    }

    private static <T extends GolemBase> DeferredHolder<EntityType<?>, EntityType<T>> golem(String name, EntityType.EntityFactory<T> factory) {
        return ENTITIES.registerEntityType(name, factory, MobCategory.MISC, builder -> builder.sized(0.4F, 0.95F).eyeHeight(0.8F).clientTrackingRange(4).updateInterval(3));
    }

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
        modEventBus.addListener(ModEntities::createAttributes);
        modEventBus.addListener(ModEntities::registerSpawnPlacements);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(BRAINY_ZOMBIE.get(), BrainyZombie.createAttributes().build());
        event.put(GIANT_BRAINY_ZOMBIE.get(), GiantBrainyZombie.createAttributes().build());
        event.put(FIRE_BAT.get(), FireBat.createAttributes().build());
        event.put(WISP.get(), Wisp.createAttributes().build());
        for (DeferredHolder<EntityType<?>, ? extends EntityType<? extends GolemBase>> golem : GOLEMS) {
            event.put(golem.get(), GolemBase.createAttributes().build());
        }
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
            BRAINY_ZOMBIE.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            BrainyZombie::checkBrainyZombieSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            GIANT_BRAINY_ZOMBIE.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            BrainyZombie::checkBrainyZombieSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
            FIRE_BAT.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            FireBat::checkFireBatSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
}
