package thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import thaumcraft.block.device.ArcaneEarBlock;
import thaumcraft.block.device.EarListener;
import thaumcraft.registry.ModBlockEntities;

public class ArcaneEarBlockEntity extends TcBlockEntity {
    private int note;
    private int signal;

    public ArcaneEarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    public int getNote() {
        return note;
    }

    public void changePitch() {
        note = (note + 1) % 25;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneEarBlockEntity ear) {
        if (ear.signal > 0 && --ear.signal == 0) {
            ArcaneEarBlock.setPowered(level, pos, state, false);
        }
        NoteBlockInstrument tone = ArcaneEarBlock.tone(level, pos);
        for (EarListener.HeardNote heard : EarListener.heard(level)) {
            if (heard.instrument() == tone
                && heard.note() == ear.note
                && heard.pos().distToCenterSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 4096.0) {
                ArcaneEarBlock.triggerNote(level, pos, ear.note, true);
                ear.signal = 10;
                ArcaneEarBlock.setPowered(level, pos, level.getBlockState(pos), true);
                break;
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        note = Mth.clamp(input.getIntOr("note", 0), 0, 24);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("note", note);
    }
}
