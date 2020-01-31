package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;

public class OpCodeOnlyDef extends AbstractDefinition implements InstructionDefinition {

    public OpCodeOnlyDef(final InstructionType instructionType) {
        super(instructionType);
    }

    @Override
    public int getParamCount() {
        return 0;
    }

    @Override
    public short getParam(final int index, final short instruction) {
        return 0x0000;
    }


}
