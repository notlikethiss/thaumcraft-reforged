package thaumcraft;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import thaumcraft.aspect.AspectSync;
import thaumcraft.crafting.TcRecipeAspectSource;
import thaumcraft.crafting.ThaumcraftRecipes;
import thaumcraft.research.ResearchList;
import thaumcraft.registry.ModAttachments;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModCreativeTabs;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModFeatures;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModMenus;
import thaumcraft.registry.ModSounds;

@Mod(Thaumcraft.MODID)
public class Thaumcraft {
    public static final String MODID = "thaumcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Thaumcraft(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModMenus.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SYNCED, Config.SPEC);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ResearchList.init();
            ThaumcraftRecipes.init();
            AspectSync.addProvider(TcRecipeAspectSource::collect);
            DispenserBlock.registerProjectileBehavior(ModItems.ALUMENTUM.get());
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
