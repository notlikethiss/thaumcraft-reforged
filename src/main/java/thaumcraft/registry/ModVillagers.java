package thaumcraft.registry;

import com.google.common.collect.ImmutableSet;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModVillagers {
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Thaumcraft.MODID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Thaumcraft.MODID);

    public static final DeferredHolder<PoiType, PoiType> ARCANE_WORKTABLE = POI_TYPES.register(
        "arcane_worktable",
        () -> new PoiType(ImmutableSet.copyOf(ModBlocks.ARCANE_WORKTABLE.get().getStateDefinition().getPossibleStates()), 1, 1)
    );

    public static final DeferredHolder<VillagerProfession, VillagerProfession> WIZARD = PROFESSIONS.register(
        "wizard",
        () -> new VillagerProfession(
            "wizard",
            holder -> holder.is(ARCANE_WORKTABLE.getKey()),
            holder -> holder.is(ARCANE_WORKTABLE.getKey()),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN
        )
    );

    private ModVillagers() {
    }

    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        PROFESSIONS.register(bus);
    }

    private static final class Trade implements ItemListing {
        private final Function<RandomSource, ItemCost> cost;
        private final Function<RandomSource, ItemStack> result;
        private final int maxUses;
        private final int xp;

        private Trade(Function<RandomSource, ItemCost> cost, Function<RandomSource, ItemStack> result, int maxUses, int xp) {
            this.cost = cost;
            this.result = result;
            this.maxUses = maxUses;
            this.xp = xp;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(cost.apply(random), result.apply(random), maxUses, xp, 0.05F);
        }
    }

    private static Function<RandomSource, ItemCost> cost(Item item) {
        return random -> new ItemCost(item);
    }

    private static Function<RandomSource, ItemCost> cost(Item item, int min, int max) {
        return random -> new ItemCost(item, random.nextIntBetweenInclusive(min, max));
    }

    private static Function<RandomSource, ItemStack> result(Item item) {
        return random -> new ItemStack(item);
    }

    private static ItemStack shards(RandomSource random) {
        Item item = ModItems.AIR_SHARD.get();
        if (random.nextFloat() < 0.5F) {
            item = ModItems.FIRE_SHARD.get();
        }
        if (random.nextFloat() < 1.0F / 3.0F) {
            item = ModItems.WATER_SHARD.get();
        }
        if (random.nextFloat() < 0.25F) {
            item = ModItems.EARTH_SHARD.get();
        }
        if (random.nextFloat() < 0.2F) {
            item = ModItems.VIS_SHARD.get();
        }
        return new ItemStack(item, random.nextIntBetweenInclusive(2, 3));
    }

    @EventBusSubscriber(modid = Thaumcraft.MODID)
    public static final class Trades {
        private Trades() {
        }

        @SubscribeEvent
        static void onVillagerTrades(VillagerTradesEvent event) {
            if (event.getType() != WIZARD.get()) {
                return;
            }
            event.getTrades().get(1).add(new Trade(cost(Items.EMERALD), result(ModItems.KNOWLEDGE_FRAGMENT.get()), 8, 2));
            event.getTrades().get(1).add(new Trade(cost(ModItems.QUICKSILVER.get(), 4, 6), result(Items.EMERALD), 12, 2));
            event.getTrades().get(2).add(new Trade(cost(Items.EMERALD), result(ModItems.ALUMENTUM.get()), 8, 5));
            event.getTrades().get(2).add(new Trade(cost(ModItems.AMBER.get(), 4, 6), result(Items.EMERALD), 12, 5));
            event.getTrades().get(3).add(new Trade(cost(Items.EMERALD), result(ModItems.NITOR.get()), 8, 10));
            event.getTrades().get(3).add(new Trade(cost(ModItems.CHICKEN_NUGGET.get(), 24, 31), result(Items.EMERALD), 12, 10));
            event.getTrades().get(4).add(new Trade(
                cost(Items.BOOKSHELF, 2, 3),
                random -> new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()),
                8,
                15
            ));
            event.getTrades().get(4).add(new Trade(cost(Items.EMERALD), ModVillagers::shards, 8, 15));
            event.getTrades().get(5).add(new Trade(
                cost(ModItems.WAND_ADEPT.get()),
                random -> new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get(), random.nextIntBetweenInclusive(2, 3)),
                4,
                30
            ));
        }
    }
}
