package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModBlockEntities;

public class NitorBlockEntity extends BlockEntity {
    public NitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NITOR.get(), pos, state);
    }
}
