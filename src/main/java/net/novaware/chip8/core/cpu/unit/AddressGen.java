package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Used;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterFile.getVariable;
import static net.novaware.chip8.core.cpu.register.RegisterModule.*;

/**
 * Address Generation Unit (AGU)
 * <p>
 * also known as
 * <p>
 * Address Computation Unit (ACU)
 */
@BoardScope
public class AddressGen implements Unit {

    public interface Config {
        /**
         * If true, adding register value to index that causes overflow is NOT reported using VF
         */
        boolean isLegacyAddressSum();
    }

    private final Config config;

    @Used
    private final ByteRegister[] variables;

    @Used
    private final WordRegister index;

    @Used
    private final ByteRegister status;

    @Used
    private final ByteRegister statusType;

    @Inject
    public AddressGen(
        final Config config,
        @Named(VARIABLES) final ByteRegister[] variables,
        @Named(INDEX) final WordRegister index,
        @Named(STATUS) final ByteRegister status,
        @Named(STATUS_TYPE) final ByteRegister statusType
    ) {
        this.config = config;
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

    /* package */ void loadIndexWithAddress(final short address) {
        index.set(address);
    }

    /* package */ void sumIndexWithVariable(final short x) {
        final int xValue = getVariable(variables, x).getAsInt();
        int iValue = index.getAsInt();

        iValue = iValue + xValue;

        final int overflow = iValue >>> 12;
        final int carry = overflow > 0 ? 0b1 : 0;

        index.set(iValue);

        final boolean overflowI = !config.isLegacyAddressSum();

        if (overflowI) {
            status.set(carry);
            statusType.set(RegisterFile.VF_CARRY_I);
        }
    }
}
