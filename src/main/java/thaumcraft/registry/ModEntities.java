package thaumcraft.registry;

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
import thaumcraft.entity.monster.BrainyZombie;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.entity.monster.GiantBrainyZombie;

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

    private ModEntities() {
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
