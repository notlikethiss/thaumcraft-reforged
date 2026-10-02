package thaumcraft.client.color;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.registry.ModDataComponents;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchNote;

public record ResearchNoteTint() implements ItemTintSource {
    public static final MapCodec<ResearchNoteTint> MAP_CODEC = MapCodec.unit(new ResearchNoteTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        ResearchNote note = stack.get(ModDataComponents.RESEARCH_NOTE.get());
        if (note == null || ResearchList.getResearch(note.key()) == null) {
            return -1;
        }
        return ARGB.opaque(ResearchList.getResearchPrimaryTag(note.key()).color);
    }

    @Override
    public MapCodec<ResearchNoteTint> type() {
        return MAP_CODEC;
    }
}
