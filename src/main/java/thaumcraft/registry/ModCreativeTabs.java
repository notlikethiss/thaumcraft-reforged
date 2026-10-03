package thaumcraft.registry;

import thaumcraft.item.golem.GolemPlacerItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.MODID);

    public static final Supplier<CreativeModeTab> THAUMCRAFT = TABS.register("thaumcraft", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.thaumcraft"))
        .icon(() -> new ItemStack(ModItems.THAUMIUM_INGOT.get()))
        .displayItems((parameters, output) -> {
            for (DeferredHolder<net.minecraft.world.item.Item, ? extends net.minecraft.world.item.Item> item : ModItems.ITEMS.getEntries()) {
                if (item.get() == ModItems.ESSENCE.get() || item.get() == ModItems.WISP_ESSENCE.get()) {
                    for (Aspect aspect : Aspect.values()) {
                        if (aspect != Aspect.UNKNOWN) {
                            ItemStack essence = new ItemStack(item.get());
                            essence.set(ModDataComponents.ESSENCE_ASPECT.get(), aspect);
                            output.accept(essence);
                        }
                    }
                } else if (item.get() instanceof GolemPlacerItem placer) {
                    for (int core = 0; core < 5; core++) {
                        if (placer.kind().allowsCore(core)) {
                            ItemStack golem = new ItemStack(placer);
                            golem.set(ModDataComponents.GOLEM_CORE.get(), core);
                            output.accept(golem);
                        }
                    }
                } else if (CreativeTabFilter.visible(item.get(), parameters)) {
                    output.accept(item.get());
                }
            }
        })
        .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
