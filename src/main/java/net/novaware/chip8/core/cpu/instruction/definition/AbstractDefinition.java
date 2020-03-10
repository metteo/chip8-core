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
    public boolean isRecognized(final @Unsigned short instruction) {
        return (instruction & this.instructionType.mask()) == this.instructionType.opcode();
    }

    @Override
    public @Unsigned short getOpCode() {
        return instructionType.opcode();
    }

    public abstract int getParamCount();

    public abstract @Unsigned short getParam(final int index, final @Unsigned short instruction);
}
