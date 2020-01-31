package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;

public class TwoRegistersDef extends AbstractDefinition implements InstructionDefinition {

    private static final short REGISTER_X_MASK = 0x0F00;
    private static final short REGISTER_Y_MASK = 0x00F0;

    public TwoRegistersDef(final InstructionType instructionType) {
        super(instructionType);
    }

    private short getRegisterX(final short instruction) {
        return (short)((instruction & REGISTER_X_MASK) >> 8);
    }

    private short getRegisterY(final short instruction) {
        return (short)((instruction & REGISTER_Y_MASK) >> 4);
    }

    @Override
    public int getParamCount() {
        return 2;
    }

    @Override
    public short getParam(final int index, final short instruction) {
        switch(index) {
            case 0: return getRegisterX(instruction);
            case 1: return getRegisterY(instruction);
            default: return 0x0000;
        }
    }
}
