package thaumcraft.research;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public class ResearchItem {
    public final String key;
    public final AspectList tags;
    public ResearchItem @Nullable [] parents;
    public ResearchItem @Nullable [] parentsHidden;
    public ResearchItem @Nullable [] siblings;
    public final int displayColumn;
    public final int displayRow;
    public final @Nullable String iconItem;
    public final int iconIndex;
    private boolean special;
    private boolean stub;
    private boolean alternate;
    private boolean hidden;
    private boolean lost;
    private boolean autoUnlock;

    public ResearchItem(String key, AspectList tags, int column, int row, int iconIndex) {
        this(key, tags, column, row, null, iconIndex);
    }

    public ResearchItem(String key, AspectList tags, int column, int row, String iconItem) {
        this(key, tags, column, row, iconItem, -1);
    }

    private ResearchItem(String key, AspectList tags, int column, int row, @Nullable String iconItem, int iconIndex) {
        this.key = key;
        this.tags = tags;
        this.displayColumn = column;
        this.displayRow = row;
        this.iconItem = iconItem;
        this.iconIndex = iconIndex;
        ResearchList.updateBounds(column, row);
    }

    public ItemStack getIconStack() {
        if (iconItem == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(iconItem)).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public ResearchItem setSpecial() {
        special = true;
        return this;
    }

    public ResearchItem setStub() {
        stub = true;
        return this;
    }

    public ResearchItem setAlternate() {
        alternate = true;
        return this;
    }

    public ResearchItem setHidden() {
        hidden = true;
        return this;
    }

    public ResearchItem setLost() {
        lost = true;
        return this;
    }

    public ResearchItem setAutoUnlock() {
        autoUnlock = true;
        return this;
    }

    public ResearchItem setParents(ResearchItem... items) {
        parents = items;
        return this;
    }

    public ResearchItem setParentsHidden(ResearchItem... items) {
        parentsHidden = items;
        return this;
    }

    public ResearchItem setSiblings(ResearchItem... items) {
        siblings = items;
        return this;
    }

    public ResearchItem registerResearchItem() {
        ResearchList.RESEARCH.put(key, this);
        return this;
    }

    public boolean getSpecial() {
        return special;
    }

    public boolean getStub() {
        return stub;
    }

    public boolean getAlternate() {
        return alternate;
    }

    public boolean getHidden() {
        return hidden;
    }

    public boolean getLost() {
        return lost;
    }

    public boolean getAutoUnlock() {
        return autoUnlock;
    }

    public List<Aspect> aspectOrder() {
        return tags.getAspects();
    }
}
