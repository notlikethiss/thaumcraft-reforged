package thaumcraft.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.blockentity.AlembicBlockEntity;
import thaumcraft.blockentity.ArcaneWorktableBlockEntity;
import thaumcraft.blockentity.CrucibleBlockEntity;
import thaumcraft.blockentity.InfusionWorkbenchBlockEntity;
import thaumcraft.blockentity.NitorBlockEntity;

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
    public static final Supplier<BlockEntityType<InfusionWorkbenchBlockEntity>> INFUSION_WORKBENCH = BLOCK_ENTITIES.register(
        "infusion_workbench",
        () -> new BlockEntityType<>(InfusionWorkbenchBlockEntity::new, ModBlocks.ARCANE_STONE.get())
    );

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
