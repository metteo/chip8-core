package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;
import org.checkerframework.checker.signedness.qual.Unsigned;

public abstract class AbstractDefinition implements InstructionDefinition {

    protected final InstructionType instructionType;

    AbstractDefinition(final InstructionType instructionType) {
        this.instructionType = instructionType;
    }

    @Override
    public InstructionType getInstructionType() {
        return instructionType;
    }

    @Override
    public boolean isRecognized(@Unsigned final short instruction) {
        return (instruction & this.instructionType.mask()) == this.instructionType.opcode();
    }

    @Override
    @Unsigned
    public short getOpCode() {
        return instructionType.opcode();
    }

    public abstract int getParamCount();

    @Unsigned
    public abstract short getParam(final int index, @Unsigned final short instruction);
}
