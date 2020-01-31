package net.novaware.chip8.core.cpu.instruction.definition;

import static net.novaware.chip8.core.cpu.instruction.InstructionType.*;
import static net.novaware.chip8.core.cpu.instruction.InstructionDefinition.notSupported;

public class SystemJumpDef extends AddressOnlyDef {

    public SystemJumpDef() {
        super(Ox0MMM);
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
