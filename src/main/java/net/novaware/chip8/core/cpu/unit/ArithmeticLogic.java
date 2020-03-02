package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

import java.util.function.IntUnaryOperator;

import static net.novaware.chip8.core.cpu.register.Registers.*;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * Arithmetic Logic Unit (ALU)
 */
public class ArithmeticLogic {

    private final IntUnaryOperator randomSource;
    
    // Accessible -------------------------------

    private final Registers registers;

    private final Memory memory;

    public ArithmeticLogic(IntUnaryOperator randomSource, Registers registers, Memory memory) {
        this.randomSource = randomSource;

        this.registers = registers;

        this.memory = memory;
    }

    // Load operations --------------------------

    /* package */ void loadValueIntoRegister(final short x, final short value) {
        registers.getVariable(x).set(value);
    }

    /* package */ void copyRegisterIntoRegister(final short x, final short y) {
        final byte yValue = registers.getVariable(y).get();

        registers.getVariable(x).set(yValue);
    }

    // Arithmetic operations --------------------

    /* package */ void addValueToRegister(final short x, final short value) {
        int xValue = registers.getVariable(x).getAsInt();

        xValue += uint(value);

        registers.getVariable(x).set(xValue);
    }

    /* package */ void addRegisterToRegister(final short x, final short y) {
        int xValue = registers.getVariable(x).getAsInt();
        final int yValue = registers.getVariable(y).getAsInt();

        xValue += yValue;

        final int overflow = xValue >>> 8;
        final int carry = overflow > 0 ? 0b1 : 0;

        registers.getVariable(x).set(xValue);

        registers.getStatus().set(carry);
        registers.getStatusType().set(VF_CARRY);
    }

    /**
     * target = x - y where target may be x or y:
     */
    /* package */ void subtractRegisterFromRegister(final short target, final short x, final short y) {
        final int xValue = registers.getVariable(x).getAsInt();
        final int yValue = registers.getVariable(y).getAsInt();
        int targetValue = xValue;

        byte borrow = 0b1;

        if (yValue > xValue) {
            targetValue += 0x100;
            borrow = 0;
        }

        targetValue -= yValue;

        registers.getVariable(target).set(targetValue);

        registers.getStatus().set(borrow);
        registers.getStatusType().set(VF_NO_BORROW);
    }

    /**
     * Effectively divide by 2
     */
    /* package */ void shiftRightRegisterIntoRegister(final short x, final short y) {
        final int yValue = registers.getVariable(y).getAsInt();

        final byte leastSignificantBit = ubyte(0b1 & yValue);
        final int xValue = yValue >>> 1;

        registers.getVariable(x).set(xValue);

        registers.getStatus().set(leastSignificantBit);
        registers.getStatusType().set(VF_LSB);
    }

    /**
     * Effectively multiply by 2
     */
    /* package */ void shiftLeftRegisterIntoRegister(final short x, final short y) {
        final int yValue = registers.getVariable(y).getAsInt();

        final byte mostSignificantBit = ubyte((0x80 & yValue) >>> 7);
        final int xValue = yValue << 1;

        registers.getVariable(x).set(xValue);

        registers.getStatus().set(mostSignificantBit);
        registers.getStatusType().set(VF_MSB);
    }

    // Comparison operations --------------------

    /**
     * @return true if equal
     */
    /* package */ boolean compareValueWithRegister(final short x, final short value) {
        final int xValue = registers.getVariable(x).getAsInt();

        return xValue == uint(value);
    }

    /**
     * @return true if equal
     */
    /* package */ boolean compareRegisterWithRegister(final short x, final short y) {
        final int xValue = registers.getVariable(x).getAsInt();
        final int yValue = registers.getVariable(y).getAsInt();

        return xValue == yValue;
    }

    // Logical operations -----------------------

    /* package */ void andRegisterToRegister(final short x, final short y) {
        int xValue = registers.getVariable(x).getAsInt();
        final int yValue = registers.getVariable(y).getAsInt();

        xValue &= yValue;

        registers.getVariable(x).set(xValue);
    }

    /* package */ void orRegisterToRegister(final short x, final short y) {
        int xValue = registers.getVariable(x).getAsInt();
        final int yValue = registers.getVariable(y).getAsInt();

        xValue |= yValue;

        registers.getVariable(x).set(xValue);
    }

    /* package */ void xorRegisterToRegister(final short x, final short y) {
        int xValue = registers.getVariable(x).getAsInt();
        final int yValue = registers.getVariable(y).getAsInt();

        xValue ^= yValue;

        registers.getVariable(x).set(xValue);
    }

    // Special operations -----------------------

    /* package */ void bcdRegisterToMemory(final short x) {
        final int xValue = registers.getVariable(x).getAsInt();
        final int address = registers.getIndex().getAsInt();

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

        registers.getVariable(x).set(xValue);
    }
}
