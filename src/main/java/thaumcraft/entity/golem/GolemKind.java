package thaumcraft.entity.golem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import thaumcraft.Thaumcraft;

public enum GolemKind {
    WOOD("wood_golem", "golem_wood", 1),
    CLAY("clay_golem", "golem_clay", 0),
    STONE("stone_golem", "golem_stone", 2),
    TALLOW("tallow_golem", "golem_tallow", 3),
    STRAW("straw_golem", "golem_straw", -1),
    ADVANCED_CLAY("advanced_clay_golem", "golem_clay", 4),
    ADVANCED_STONE("advanced_stone_golem", "golem_stone", 5),
    IRON_GUARDIAN("iron_guardian_golem", "golem_iron", 6),
    DECANTING("decanting_golem", "golem_tallow", 7);

    private final String id;
    private final String texture;
    private final int guiId;

    GolemKind(String id, String texture, int guiId) {
        this.id = id;
        this.texture = texture;
        this.guiId = guiId;
    }

    public String id() {
        return id;
    }

    public ResourceLocation texture() {
        return Thaumcraft.id("textures/model/" + texture + ".png");
    }

    public int guiId() {
        return guiId;
    }

    public boolean advanced() {
        return this == ADVANCED_CLAY || this == ADVANCED_STONE || this == DECANTING;
    }

    public boolean allowsCore(int core) {
        if (this == TALLOW && core == 2 || this == STRAW && core == 4) {
            return false;
        }
        return !advanced() || core == 0;
    }

    public EntityType<?> entityType() {
        return BuiltInRegistries.ENTITY_TYPE.getValue(Thaumcraft.id(id));
    }

    public Item item() {
        return BuiltInRegistries.ITEM.getValue(Thaumcraft.id(id));
    }
}
