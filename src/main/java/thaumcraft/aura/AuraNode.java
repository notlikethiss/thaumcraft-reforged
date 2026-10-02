package thaumcraft.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.AspectList;

public final class AuraNode {
    public static final Codec<AuraNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("key").forGetter(node -> node.key),
        Codec.INT.fieldOf("level").forGetter(node -> node.level),
        Codec.INT.fieldOf("base_level").forGetter(node -> node.baseLevel),
        NodeType.CODEC.fieldOf("type").forGetter(node -> node.type),
        Codec.DOUBLE.fieldOf("x").forGetter(node -> node.x),
        Codec.DOUBLE.fieldOf("y").forGetter(node -> node.y),
        Codec.DOUBLE.fieldOf("z").forGetter(node -> node.z),
        Codec.BOOL.optionalFieldOf("locked", false).forGetter(node -> node.locked),
        AspectList.CODEC.optionalFieldOf("flux", AspectList.EMPTY).forGetter(node -> node.flux)
    ).apply(instance, AuraNode::new));

    public int key;
    public int level;
    public int baseLevel;
    public AspectList flux = new AspectList();
    public NodeType type;
    public ResourceKey<Level> dimension = Level.OVERWORLD;
    public double x;
    public double y;
    public double z;
    public boolean locked;

    public AuraNode(int key, int level, NodeType type, ResourceKey<Level> dimension, BlockPos pos) {
        this.key = key;
        this.level = level;
        this.baseLevel = level;
        this.type = type;
        this.dimension = dimension;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    private AuraNode(int key, int level, int baseLevel, NodeType type, double x, double y, double z, boolean locked, AspectList flux) {
        this.key = key;
        this.level = level;
        this.baseLevel = baseLevel;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.locked = locked;
        this.flux = flux.copy();
    }

    public AuraNode copy() {
        AuraNode copy = new AuraNode(key, level, baseLevel, type, x, y, z, locked, flux);
        copy.dimension = dimension;
        return copy;
    }

    public double distanceSq(double px, double py, double pz) {
        double dx = x - px;
        double dy = y - py;
        double dz = z - pz;
        return dx * dx + dy * dy + dz * dz;
    }

    public int fluxTotal() {
        return flux.visSize();
    }
}
