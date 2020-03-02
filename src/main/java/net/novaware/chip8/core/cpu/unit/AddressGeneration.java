package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

/**
 * Address Generation Unit (AGU)
 *
 * also known as
 *
 * Address Computation Unit (ACU)
 */
public class AddressGeneration {

    // Accessible -------------------------------

    private final Registers registers;

    private final Memory memory;

    public AddressGeneration(Registers registers, Memory memory) {
        this.registers = registers;

        this.memory = memory;
    }

    /* package */ void loadAddressIntoIndex(final short address) {
        registers.getIndex().set(address);
    }

    /* package */ void addRegisterIntoIndex(final short x, boolean overflowI) {
        final int xValue = registers.getVariable(x).getAsInt();
        int iValue = registers.getIndex().getAsInt();

        iValue = iValue + xValue;

        final int overflow = iValue >>> 12;
        final int carry = overflow > 0 ? 0b1 : 0;

        registers.getIndex().set(iValue);

        if (overflowI) {
            registers.getStatus().set(carry);
            registers.getStatusType().set(Registers.VF_CARRY_I);
        }
    }
}
