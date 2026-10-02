package thaumcraft.block.world;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AmberBlock extends Block {
    public AmberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 3;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }
}
