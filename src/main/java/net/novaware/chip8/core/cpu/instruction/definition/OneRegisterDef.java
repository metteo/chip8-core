package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;

public class OneRegisterDef extends AbstractDefinition implements InstructionDefinition {

    private static final int REGISTER_MASK = 0x0F00;

    public OneRegisterDef(final InstructionType instructionType) {
        super(instructionType);
    }

    private short getRegister(final short instruction) {
        return (short)((instruction & REGISTER_MASK) >>> 8 /* bits */);
    }

    @Override
    public int getParamCount() {
        return 1;
    }

    @Override
    public short getParam(final int index, final short instruction) {
        if (index == 0) {
            return getRegister(instruction);
        }

        return 0x0000;
    }
}
