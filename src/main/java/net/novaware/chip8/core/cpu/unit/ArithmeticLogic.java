package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owns;
import net.novaware.chip8.core.util.uml.Uses;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.IntUnaryOperator;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.register.Registers.*;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * Arithmetic Logic Unit (ALU)
 */
@BoardScope
public class ArithmeticLogic {

    @Owns
    private final IntUnaryOperator randomSource;

    @Uses
    private final ByteRegister[] variables;

    @Uses
    private final TribbleRegister index;

    @Uses
    private final ByteRegister status;

    @Uses
    private final ByteRegister statusType;

    @Uses
    private final Memory memory;

    @Inject
    public ArithmeticLogic(
            @Named("random") final IntUnaryOperator randomSource,
            @Named(VARIABLES) final ByteRegister[] variables,
            @Named(INDEX) final TribbleRegister index,
            @Named(STATUS) final ByteRegister status,
            @Named(STATUS_TYPE) final ByteRegister statusType,
            @Named(MMU) final Memory memory
    ) {
        this.randomSource = randomSource;

        this.variables = variables;
        this.index = index;
        this.status = status;
        this.statusType = statusType;
        this.memory = memory;
    }

    // Load operations --------------------------

    /* package */ void loadValueIntoRegister(final short x, final short value) {
        getVariable(variables, x).set(value);
    }

    /* package */ void copyRegisterIntoRegister(final short x, final short y) {
        final byte yValue = getVariable(variables, y).get();

        getVariable(variables, x).set(yValue);
    }

    // Arithmetic operations --------------------

    /* package */ void addValueToRegister(final short x, final short value) {
        int xValue = getVariable(variables, x).getAsInt();

        xValue = xValue + uint(value);

        getVariable(variables, x).set(xValue);
    }

    /* package */ void addRegisterToRegister(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue + yValue;

        final int overflow = xValue >>> 8;
        final int carry = overflow > 0 ? 0b1 : 0;

        getVariable(variables, x).set(xValue);

        status.set(carry);
        statusType.set(VF_CARRY);
    }

    /**
     * target = x - y where target may be x or y:
     */
    /* package */ void subtractRegisterFromRegister(final short target, final short x, final short y) {
        final int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();
        int targetValue = xValue;

        byte borrow = 0b1;

        if (yValue > xValue) {
            targetValue = targetValue + 0x100;
            borrow = 0;
        }

        targetValue = targetValue - yValue;

        getVariable(variables, target).set(targetValue);

        status.set(borrow);
        statusType.set(VF_NO_BORROW);
    }

    /**
     * Effectively divide by 2
     */
    /* package */ void shiftRightRegisterIntoRegister(final short x, final short y) {
        final int yValue = getVariable(variables, y).getAsInt();

        final byte leastSignificantBit = ubyte(0b1 & yValue);
        final int xValue = yValue >>> 1;

        getVariable(variables, x).set(xValue);

        status.set(leastSignificantBit);
        statusType.set(VF_LSB);
    }

    /**
     * Effectively multiply by 2
     */
    /* package */ void shiftLeftRegisterIntoRegister(final short x, final short y) {
        final int yValue = getVariable(variables, y).getAsInt();

        final byte mostSignificantBit = ubyte((0x80 & yValue) >>> 7);
        final int xValue = yValue << 1;

        getVariable(variables, x).set(xValue);

        status.set(mostSignificantBit);
        statusType.set(VF_MSB);
    }

    // Comparison operations --------------------

    /**
     * @return true if equal
     */
    /* package */ boolean compareValueWithRegister(final short x, final short value) {
        final int xValue = getVariable(variables, x).getAsInt();

        return xValue == uint(value);
    }

    /**
     * @return true if equal
     */
    /* package */ boolean compareRegisterWithRegister(final short x, final short y) {
        final int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        return xValue == yValue;
    }

    // Logical operations -----------------------

    /* package */ void andRegisterToRegister(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue & yValue;

        getVariable(variables, x).set(xValue);
    }

    /* package */ void orRegisterToRegister(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue | yValue;

        getVariable(variables, x).set(xValue);
    }

    /* package */ void xorRegisterToRegister(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue ^ yValue;

        getVariable(variables, x).set(xValue);
    }

    // Special operations -----------------------

    /* package */ void bcdRegisterToMemory(final short x) {
        final int xValue = getVariable(variables, x).getAsInt();
        final int address = index.getAsInt();

        final byte hundreds = ubyte(xValue / 100);
        final byte tens = ubyte((xValue / 10) % 10);
        final byte units = ubyte(xValue % 10);

        memory.setByte(ushort(address), hundreds);
        memory.setByte(ushort(address + 1), tens);
        memory.setByte(ushort(address + 2), units);
    }

    /* package */ void andRandomToRegister(final short x, final short value) {
        int kkValue = uint(value);
        int random = randomSource.applyAsInt(256);

        int xValue = kkValue & random;

        getVariable(variables, x).set(xValue);
    }
}
