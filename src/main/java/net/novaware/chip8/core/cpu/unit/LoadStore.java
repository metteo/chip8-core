package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.register.Registers.getVariable;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.UnsignedUtil.*;
import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;

/**
 * Load Store Unit
 */
@BoardScope
public class LoadStore {

    private final ByteRegister[] variables;
    private final TribbleRegister index;
    private final Memory memory;

    @Inject
    public LoadStore(
            @Named(VARIABLES) final ByteRegister[] variables,
            @Named(INDEX) final TribbleRegister index,
            @Named(MMU) final Memory memory
    ) {

        this.variables = variables;
        this.index = index;
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
