package thaumcraft.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.menu.ArcaneBoreMenu;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.menu.HandMirrorMenu;
import thaumcraft.menu.InfusionWorkbenchMenu;
import thaumcraft.menu.ResearchTableMenu;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Thaumcraft.MODID);

    public static final Supplier<MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH = MENUS.register(
        "arcane_workbench",
        () -> IMenuTypeExtension.create(ArcaneWorkbenchMenu::fromNetwork)
    );
    public static final Supplier<MenuType<InfusionWorkbenchMenu>> INFUSION_WORKBENCH = MENUS.register(
        "infusion_workbench",
        () -> IMenuTypeExtension.create(InfusionWorkbenchMenu::fromNetwork)
    );

    public static final Supplier<MenuType<ResearchTableMenu>> RESEARCH_TABLE = MENUS.register(
        "research_table",
        () -> IMenuTypeExtension.create(ResearchTableMenu::fromNetwork)
    );

    public static final Supplier<MenuType<ArcaneBoreMenu>> ARCANE_BORE = MENUS.register(
        "arcane_bore",
        () -> IMenuTypeExtension.create(ArcaneBoreMenu::fromNetwork)
    );
    public static final Supplier<MenuType<HandMirrorMenu>> HAND_MIRROR = MENUS.register(
        "hand_mirror",
        () -> new MenuType<>(HandMirrorMenu::new, FeatureFlags.VANILLA_SET)
    );

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
