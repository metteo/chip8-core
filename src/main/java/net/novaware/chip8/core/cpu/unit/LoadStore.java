package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigInteger;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.register.RegisterFile.getVariable;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * Load Store Unit
 */
@BoardScope
public class LoadStore implements Unit {

    private final ByteRegister[] variables;
    private final TribbleRegister index;
    private final WordRegister input;
    private final Memory memory;

    @Inject
    public LoadStore(
            @Named(VARIABLES) final ByteRegister[] variables,
            @Named(INDEX) final TribbleRegister index,
            @Named(INPUT) final WordRegister input,
            @Named(MMU) final Memory memory
    ) {

        this.variables = variables;
        this.index = index;
        this.input = input;
        this.memory = memory;
    }

    /* package */ void loadMemoryIntoRegisters(final short x, final boolean incrementI) {
        int xIndex = uint(x);
        int iValue = index.getAsInt();


        for (int i = 0; i <= xIndex; ++i, ++iValue) {
            final byte data = memory.getByte(ushort(iValue));
            getVariable(variables, i).set(data);
        }

        if (incrementI) {
            index.set(iValue);
        }
    }

    /**
     * @return true if input register is non-0
     */
    /* package */ boolean loadInputIntoRegister(short x) {
        int inValue = input.getAsInt();

        if (inValue > 0) { //some bits are set
            // TODO: instantiation, maybe try to avoid
            int mostSigNonZeroBitIndex = BigInteger.valueOf(inValue).bitLength() - 1;

            getVariable(variables, x).set(mostSigNonZeroBitIndex);
            return true;
        }

        return false;
    }

    /* package */ void storeRegistersInMemory(final short x, final boolean incrementI) {
        int xIndex = uint(x);
        int iValue = index.getAsInt();

        for (int i = 0; i <= xIndex; ++i, ++iValue) {
            final byte data = getVariable(variables, i).get();
            memory.setByte(ushort(iValue), data);
        }

        if (incrementI) {
            index.set(iValue);
        }
    }

    /* package */ void storeRegisterInMemoryAsBcd(final short x) {
        final int xValue = getVariable(variables, x).getAsInt();
        final int address = index.getAsInt();

        final byte hundreds = ubyte(xValue / 100);
        final byte tens = ubyte((xValue / 10) % 10);
        final byte units = ubyte(xValue % 10);

        memory.setByte(ushort(address), hundreds);
        memory.setByte(ushort(address + 1), tens);
        memory.setByte(ushort(address + 2), units);
    }
}
