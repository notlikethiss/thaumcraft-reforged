package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneBoreBaseBlockEntity extends BlockEntity {
    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE_BASE.get(), pos, state);
    }
}
