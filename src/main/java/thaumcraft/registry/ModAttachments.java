package thaumcraft.registry;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.AuraChunkData;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thaumcraft.MODID);

    public static final Supplier<AttachmentType<AuraChunkData>> AURA_CHUNK = ATTACHMENTS.register(
        "aura_chunk",
        () -> AttachmentType.builder(AuraChunkData::new).serialize(AuraChunkData.SERIALIZER).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
