package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Used;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.register.RegisterFile.getVariable;

/**
 * Address Generation Unit (AGU)
 * <p>
 * also known as
 * <p>
 * Address Computation Unit (ACU)
 */
@BoardScope
public class AddressGen implements Unit {

    @Used
    private final ByteRegister[] variables;

    @Used
    private final TribbleRegister index;

    @Used
    private final ByteRegister status;

    @Used
    private final ByteRegister statusType;

    @Inject
    public AddressGen(
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

    @Override
    public void initialize() {
        zeroOutIndex();
    }

    @Override
    public void reset() {
        zeroOutIndex();
    }

    private void zeroOutIndex() {
        index.set(0);
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
            statusType.set(RegisterFile.VF_CARRY_I);
        }
    }
}
