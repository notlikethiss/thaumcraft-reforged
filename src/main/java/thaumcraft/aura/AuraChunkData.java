package thaumcraft.aura;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public final class AuraChunkData {
    public static final IAttachmentSerializer<CompoundTag, AuraChunkData> SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public AuraChunkData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            AuraChunkData data = new AuraChunkData(holder);
            ValueInput.of(tag, provider).listOrEmpty("nodes", AuraNode.CODEC).forEach(data.pending::add);
            return data;
        }

        @Override
        public CompoundTag write(AuraChunkData attachment, HolderLookup.Provider provider) {
            List<AuraNode> nodes = attachment.currentNodes();
            if (nodes.isEmpty()) {
                return null;
            }
            CompoundTag tag = new CompoundTag();
            ValueOutput.TypedOutputList<AuraNode> list = ValueOutput.of(tag, provider).list("nodes", AuraNode.CODEC);
            nodes.forEach(list::add);
            return tag;
        }
    };

    private final IAttachmentHolder holder;
    private final List<AuraNode> pending = new ArrayList<>();
    private boolean loaded;

    public AuraChunkData(IAttachmentHolder holder) {
        this.holder = holder;
    }

    public void addPending(AuraNode node) {
        pending.add(node);
    }

    public List<AuraNode> pendingView() {
        return List.copyOf(pending);
    }

    public List<AuraNode> takePending() {
        List<AuraNode> nodes = new ArrayList<>(pending);
        pending.clear();
        loaded = true;
        return nodes;
    }

    private List<AuraNode> currentNodes() {
        if (loaded && holder instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel level) {
            return AuraManager.nodesInChunk(level.dimension(), chunk.getPos());
        }
        return pending;
    }
}
