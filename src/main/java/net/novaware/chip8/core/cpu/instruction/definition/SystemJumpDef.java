package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;

import static net.novaware.chip8.core.cpu.instruction.InstructionType.*;
import static net.novaware.chip8.core.cpu.instruction.InstructionDefinition.notSupported;
import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public class SystemJumpDef extends AddressOnlyDef {

    public SystemJumpDef(final InstructionType instructionType) {
        super(instructionType);

        assertArgument(instructionType != Ox0MMM, "only Ox0MMM is supported");
    }

    @Override
    public boolean isRecognized(final short instruction) {
        return (instruction & Ox0MMM.mask()) == Ox0MMM.opcode() &&
                instruction != Ox00E0.opcode() &&
                instruction != Ox00EE.opcode();
    }

    @Override
    protected short getAddress(final short instruction) {
        assert isRecognized(instruction) : notSupported(instruction);

        return super.getAddress(instruction);
    }
}
