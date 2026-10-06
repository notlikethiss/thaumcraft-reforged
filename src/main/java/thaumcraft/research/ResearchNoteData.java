package thaumcraft.research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModDataComponents;

public final class ResearchNoteData {
    public static final int FAILED_SIZE = 64;

    public @Nullable String key;
    public Aspect[] tags = new Aspect[0];
    public int[] progress = new int[0];
    public byte[] failedTags = new byte[FAILED_SIZE];

    public static @Nullable ResearchNote getNote(ItemStack stack) {
        return stack.get(ModDataComponents.RESEARCH_NOTE.get());
    }

    public static boolean hasNote(ItemStack stack) {
        return stack.has(ModDataComponents.RESEARCH_NOTE.get());
    }

    public static void setNote(ItemStack stack, ResearchNote note) {
        stack.set(ModDataComponents.RESEARCH_NOTE.get(), note);
    }

    public static ResearchNoteData read(ItemStack stack) {
        ResearchNoteData data = new ResearchNoteData();
        ResearchNote note = getNote(stack);
        if (note == null) {
            return data;
        }
        data.key = note.key();
        data.tags = note.tags().toArray(new Aspect[0]);
        data.progress = new int[data.tags.length];
        for (int i = 0; i < data.progress.length && i < note.progress().size(); i++) {
            data.progress[i] = note.progress().get(i);
        }
        for (int i = 0; i < FAILED_SIZE && i < note.failed().size(); i++) {
            data.failedTags[i] = note.failed().get(i).byteValue();
        }
        ResearchItem research = ResearchList.getResearch(data.key);
        if (research == null) {
            return data;
        }
        List<Aspect> current = research.aspectOrder();
        boolean busted = current.size() != data.tags.length;
        for (Aspect tag : data.tags) {
            if (tag == Aspect.UNKNOWN) {
                busted = true;
                break;
            }
        }
        if (busted) {
            Aspect[] oldTags = data.tags;
            int[] oldProgress = data.progress;
            data.tags = current.toArray(new Aspect[0]);
            data.progress = new int[data.tags.length];
            data.failedTags = new byte[FAILED_SIZE];
            for (int a = 0; a < data.tags.length; a++) {
                for (int b = 0; b < oldTags.length; b++) {
                    if (data.tags[a] == oldTags[b]) {
                        data.progress[a] = oldProgress[b];
                    }
                }
            }
        }
        return data;
    }

    public static ResearchNote create(String key) {
        List<Aspect> tags = ResearchList.getResearch(key).aspectOrder();
        return new ResearchNote(key, tags, zeros(tags.size()), zeros(FAILED_SIZE));
    }

    public ResearchNote toNote() {
        List<Integer> failed = new ArrayList<>(FAILED_SIZE);
        for (byte value : failedTags) {
            failed.add((int) value);
        }
        return new ResearchNote(key == null ? "" : key, Arrays.asList(tags), Arrays.stream(progress).boxed().toList(), failed);
    }

    public float getTotalProgress() {
        ResearchItem research = ResearchList.getResearch(key);
        if (research == null) {
            return 0.0F;
        }
        float done = 0.0F;
        float total = 0.0F;
        for (int a = 0; a < tags.length; a++) {
            done += progress[a];
            total += research.tags.getAmount(tags[a]);
        }
        return total == 0.0F ? 0.0F : done / total;
    }

    public float getTagProgress(Aspect tag) {
        ResearchItem research = ResearchList.getResearch(key);
        if (research == null) {
            return 0.0F;
        }
        for (int a = 0; a < tags.length; a++) {
            if (tag == tags[a]) {
                return (float) progress[a] / research.tags.getAmount(tags[a]);
            }
        }
        return 0.0F;
    }

    private static List<Integer> zeros(int size) {
        List<Integer> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(0);
        }
        return result;
    }
}
