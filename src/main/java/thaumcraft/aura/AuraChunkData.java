package thaumcraft.aura;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public final class AuraChunkData {
    public static final IAttachmentSerializer<AuraChunkData> SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public AuraChunkData read(IAttachmentHolder holder, ValueInput input) {
            AuraChunkData data = new AuraChunkData(holder);
            input.listOrEmpty("nodes", AuraNode.CODEC).forEach(data.pending::add);
            return data;
        }

        @Override
        public boolean write(AuraChunkData attachment, ValueOutput output) {
            List<AuraNode> nodes = attachment.currentNodes();
            if (nodes.isEmpty()) {
                return false;
            }
            ValueOutput.TypedOutputList<AuraNode> list = output.list("nodes", AuraNode.CODEC);
            nodes.forEach(list::add);
            return true;
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
