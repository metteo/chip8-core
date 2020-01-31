package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;

public class RegisterValueDef extends AbstractDefinition implements InstructionDefinition {

    private static final int REGISTER_MASK = 0x0F00;
    private static final int VALUE_MASK    = 0x00FF;

    public RegisterValueDef(InstructionType instructionType) {
        super(instructionType);
    }

    private short getRegister(final short instruction) {
        return (short)((instruction & REGISTER_MASK) >>> 8 /* bits */);
    }

    private short getValue(final short instruction) {
        return (short) (instruction & VALUE_MASK);
    }

    @Override
    public int getParamCount() {
        return 2;
    }

    @Override
    public short getParam(final int index, final short instruction) {
        switch(index) {
            case 0: return getRegister(instruction);
            case 1: return getValue(instruction);
            default: return 0x0000;
        }
    }
}
