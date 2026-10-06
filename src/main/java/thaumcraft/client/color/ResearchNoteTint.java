package thaumcraft.client.color;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchNote;
import thaumcraft.research.ResearchNoteData;

public record ResearchNoteTint(int layer) implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != layer) {
            return -1;
        }
        ResearchNote note = ResearchNoteData.getNote(stack);
        if (note == null || ResearchList.getResearch(note.key()) == null) {
            return -1;
        }
        return FastColor.ARGB32.opaque(ResearchList.getResearchPrimaryTag(note.key()).color);
    }
}
