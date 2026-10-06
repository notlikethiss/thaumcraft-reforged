package thaumcraft.blockentity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.Config;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.ward.WardManager;

public class OwnedBlockEntity extends TcBlockEntity {
    public static final int ACCESS_USE = 0;
    public static final int ACCESS_GRANT = 1;

    private @Nullable UUID owner;
    private String ownerName = "";
    private final List<Access> access = new ArrayList<>();
    private boolean safeToRemove;

    public OwnedBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.OWNED.get(), pos, state);
    }

    protected OwnedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setOwner(Player player) {
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
        setChanged();
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isOwner(Player player) {
        return player.getUUID().equals(owner);
    }

    public int getAccess(Player player) {
        for (Access entry : access) {
            if (entry.player().equals(player.getUUID())) {
                return entry.level();
            }
        }
        return -1;
    }

    public boolean hasAccess(Player player, int level) {
        for (Access entry : access) {
            if (entry.player().equals(player.getUUID()) && entry.level() == level) {
                return true;
            }
        }
        return false;
    }

    public boolean canUse(Player player) {
        return isOwner(player) || getAccess(player) >= 0;
    }

    public boolean canGrant(Player player) {
        return isOwner(player) || hasAccess(player, ACCESS_GRANT);
    }

    public void addAccess(Player player, int level) {
        access.add(new Access(player.getUUID(), level));
        setChanged();
    }

    public boolean sharesUsersWith(OwnedBlockEntity other) {
        if (owner != null && owner.equals(other.owner)) {
            return true;
        }
        for (Access entry : access) {
            if (entry.player().equals(other.owner)) {
                return true;
            }
            for (Access otherEntry : other.access) {
                if (entry.player().equals(otherEntry.player())) {
                    return true;
                }
            }
        }
        for (Access otherEntry : other.access) {
            if (otherEntry.player().equals(owner)) {
                return true;
            }
        }
        return false;
    }

    public boolean canRemoveWard(Player player) {
        return !Config.wardedStone() || isOwner(player);
    }

    public void markSafeToRemove() {
        safeToRemove = true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (Config.wardedStone() && !safeToRemove && level instanceof ServerLevel serverLevel) {
            WardManager.addBlockToRestore(serverLevel, pos, state, saveWithFullMetadata(serverLevel.registryAccess()));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        ownerName = input.getStringOr("owner_name", "");
        access.clear();
        input.read("access", Access.CODEC.listOf()).ifPresent(access::addAll);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        Optional.ofNullable(owner).ifPresent(uuid -> output.store("owner", UUIDUtil.CODEC, uuid));
        output.putString("owner_name", ownerName);
        output.store("access", Access.CODEC.listOf(), List.copyOf(access));
    }

    public record Access(UUID player, int level) {
        public static final Codec<Access> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player").forGetter(Access::player),
            Codec.INT.fieldOf("level").forGetter(Access::level)
        ).apply(instance, Access::new));
    }
}
