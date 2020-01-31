package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

import java.util.Random;

import static java.lang.Short.toUnsignedInt;

/**
 * Arithmetic Logic Unit (ALU)
 */
public class ArithmeticLogic {

    private Random r = new Random();
    
    // Accessible -------------------------------

    private final Registers registers;

    private final Memory memory;

    public ArithmeticLogic(Registers registers, Memory memory) {
        this.registers = registers;

        this.memory = memory;
    }

    // Load operations --------------------------

    /* package */ void loadValueIntoRegister(final short x, final short value) {
        registers.getData(x).set(value);
    }

    /* package */ void copyRegisterIntoRegister(final short x, final short y) {
        final byte yValue = registers.getData(y).get();

        registers.getData(x).set(yValue);
    }

    // Arithmetic operations --------------------

    /* package */ void addValueToRegister(final short x, final short value) {
        int xValue = registers.getData(x).getAsInt();

        xValue += toUnsignedInt(value);

        registers.getData(x).set(xValue);
    }

    /* package */ void addRegisterToRegister(final short x, final short y) {
        int xValue = registers.getData(x).getAsInt();
        final int yValue = registers.getData(y).getAsInt();

        xValue += yValue;

        final int overflow = xValue >>> 8;
        final int carry = overflow > 0 ? 0b1 : 0;

        registers.getData(x).set(xValue);
        registers.getStatus().set(carry);
    }

    /**
     * target = x - y where target may be x or y:
     */
    /* package */ void subtractRegisterFromRegister(final short target, final short x, final short y) {
        final int xValue = registers.getData(x).getAsInt();
        final int yValue = registers.getData(y).getAsInt();
        int targetValue = xValue;

        byte borrow = 0b1;

        if (yValue > xValue) {
            targetValue += 0x100;
            borrow = 0;
        }

        targetValue -= yValue;

        registers.getData(target).set(targetValue);
        registers.getStatus().set(borrow);
    }

    /**
     * Effectively divide by 2
     */
    /* package */ void shiftRightRegisterIntoRegister(final short x, final short y) {
        final int yValue = registers.getData(y).getAsInt();

        final byte leastSignificantBit = (byte)(0b1 & yValue);
        final int xValue = yValue >>> 1;

        registers.getData(x).set(xValue);
        registers.getStatus().set(leastSignificantBit);
    }

    /**
     * Effectively multiply by 2
     */
    /* package */ void shiftLeftRegisterIntoRegister(final short x, final short y) {
        final int yValue = registers.getData(y).getAsInt();

        final byte mostSignificantBit = (byte)((0x80 & yValue) >>> 7);
        final int xValue = yValue << 1;

        registers.getData(x).set(xValue);
        registers.getStatus().set(mostSignificantBit);
    }

    // Comparison operations --------------------

    /**
     * @return true if equal
     */
    /* package */ boolean compareValueWithRegister(final short x, final short value) {
        final int xValue = registers.getData(x).getAsInt();

        return xValue == toUnsignedInt(value);
    }

    /**
     * @return true if equal
     */
    /* package */ boolean compareRegisterWithRegister(final short x, final short y) {
        final int xValue = registers.getData(x).getAsInt();
        final int yValue = registers.getData(y).getAsInt();

        return xValue == yValue;
    }

    // Logical operations -----------------------

    /* package */ void andRegisterToRegister(final short x, final short y) {
        int xValue = registers.getData(x).getAsInt();
        final int yValue = registers.getData(y).getAsInt();

        xValue &= yValue;

        registers.getData(x).set(xValue);
    }

    /* package */ void orRegisterToRegister(final short x, final short y) {
        int xValue = registers.getData(x).getAsInt();
        final int yValue = registers.getData(y).getAsInt();

        xValue |= yValue;

        registers.getData(x).set(xValue);
    }

    /* package */ void xorRegisterToRegister(final short x, final short y) {
        int xValue = registers.getData(x).getAsInt();
        final int yValue = registers.getData(y).getAsInt();

        xValue ^= yValue;

        registers.getData(x).set(xValue);
    }

    // Special operations -----------------------

    /* package */ void bcdRegisterToMemory(final short x) {
        final int xValue = registers.getData(x).getAsInt();
        final int address = registers.getIndex().getAsInt();

        final byte hundreds = (byte)(xValue / 100);
        final byte tens = (byte) ((xValue / 10) % 10);
        final byte units = (byte) (xValue % 10);

        memory.setByte((short)(address    ), hundreds);
        memory.setByte((short)(address + 1), tens);
        memory.setByte((short)(address + 2), units);
    }

    /* package */ void andRandomToRegister(final short x, final short value) {
        int xValue;
        int kkValue = toUnsignedInt(value);

        int random = 4; //chosen by fair dice roll.
                        //guaranteed to be random.

        random = r.nextInt(256);

        xValue = kkValue & random;
        registers.getData(x).set(xValue);
    }


}
