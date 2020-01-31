package net.novaware.chip8.core.cpu.instruction.definition;

import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.instruction.InstructionDefinition;

import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class AddressOnlyDef extends AbstractDefinition implements InstructionDefinition {

    private static final short ADDRESS_MASK = 0x0FFF;

    public AddressOnlyDef(final InstructionType instructionType) {
        super(instructionType);
    }

    protected short getAddress(final short instruction) {
        return ushort(instruction & ADDRESS_MASK);
    }

    @Override
    public int getParamCount() {
        return 1;
    }

    @Override
    public short getParam(final int index, final short instruction) {
        if (index == 0) {
            return getAddress(instruction);
        }

        return 0x0000;
    }
}
