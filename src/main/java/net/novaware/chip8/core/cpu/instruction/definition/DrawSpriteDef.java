package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;

import static net.novaware.chip8.core.cpu.instruction.InstructionType.OxDXYK;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class DrawSpriteDef extends AbstractDefinition implements InstructionDefinition {

    private static final int REGISTER_X_MASK = 0x0F00;
    private static final int REGISTER_Y_MASK = 0x00F0;
    private static final int VALUE_MASK      = 0x000F;

    public DrawSpriteDef() {
        super(OxDXYK);
    }

    private short getRegisterX(final short instruction) {
        return ushort((instruction & REGISTER_X_MASK) >>> 8 /* bits */);
    }

    private short getRegisterY(final short instruction) {
        return ushort((instruction & REGISTER_Y_MASK) >>> 4 /* bits */);
    }

    private short getValue(final short instruction) {
        return ushort(instruction & VALUE_MASK);
    }

    @Override
    public int getParamCount() {
        return 3;
    }

    @Override
    public short getParam(final int index, final short instruction) {
        switch(index) {
            case 0: return getRegisterX(instruction);
            case 1: return getRegisterY(instruction);
            case 2: return getValue(instruction);
            default: return 0x0000;
        }
    }
}
