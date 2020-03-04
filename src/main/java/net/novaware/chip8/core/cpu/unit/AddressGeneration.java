package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Uses;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.register.Registers.getVariable;

/**
 * Address Generation Unit (AGU)
 * <p>
 * also known as
 * <p>
 * Address Computation Unit (ACU)
 */
@BoardScope
public class AddressGeneration {

    @Uses
    private final ByteRegister[] variables;

    @Uses
    private final TribbleRegister index;

    @Uses
    private final ByteRegister status;

    @Uses
    private final ByteRegister statusType;

    @Inject
    public AddressGeneration(
            @Named(VARIABLES) final ByteRegister[] variables,
            @Named(INDEX) final TribbleRegister index,
            @Named(STATUS) final ByteRegister status,
            @Named(STATUS_TYPE) final ByteRegister statusType
    ) {
        this.variables = variables;
        this.index = index;
        this.status = status;
        this.statusType = statusType;
    }

    /* package */ void loadAddressIntoIndex(final short address) {
        index.set(address);
    }

    /* package */ void addRegisterIntoIndex(final short x, boolean overflowI) {
        final int xValue = getVariable(variables, x).getAsInt();
        int iValue = index.getAsInt();

        iValue = iValue + xValue;

        final int overflow = iValue >>> 12;
        final int carry = overflow > 0 ? 0b1 : 0;

        index.set(iValue);

        if (overflowI) {
            status.set(carry);
            statusType.set(Registers.VF_CARRY_I);
        }
    }
}
