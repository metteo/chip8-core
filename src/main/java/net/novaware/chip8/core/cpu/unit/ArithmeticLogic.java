package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.function.IntUnaryOperator;

import static net.novaware.chip8.core.cpu.register.RegisterFile.*;
import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.unit.UnitModule.RANDOM;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Arithmetic Logic Unit (ALU)
 */
@BoardScope
public class ArithmeticLogic implements Unit {

    private static final Logger LOG = LogManager.getLogger();

    @Owned
    private final IntUnaryOperator randomSource;

    @Used
    private final ByteRegister[] variables;

    @Used
    private final WordRegister input;

    @Used
    private final ByteRegister status;

    @Used
    private final ByteRegister statusType;

    @Inject
    public ArithmeticLogic(
        @Named(RANDOM) final IntUnaryOperator randomSource,
        @Named(VARIABLES) final ByteRegister[] variables,
        @Named(INPUT) final WordRegister input,
        @Named(STATUS) final ByteRegister status,
        @Named(STATUS_TYPE) final ByteRegister statusType
    ) {
        this.randomSource = randomSource;

        this.variables = variables;
        this.input = input;
        this.status = status;
        this.statusType = statusType;
    }

    // Load operations --------------------------

    /* package */ void loadVariableWithValue(final short x, final short value) {
        getVariable(variables, x).set(value);
    }

    /* package */ void copyVariableIntoVariable(final short x, final short y) {
        final byte yValue = getVariable(variables, y).get();

        getVariable(variables, x).set(yValue);
    }

    // Arithmetic operations --------------------

    /* package */ void sumVariableWithValue(final short x, final short value) {
        int xValue = getVariable(variables, x).getAsInt();

        xValue = xValue + uint(value);

        getVariable(variables, x).set(xValue);
    }

    /* package */ void sumVariableWithVariable(final short x, final short y) {
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
    /* package */ void subtractVariableFromVariable(final short target, final short x, final short y) {
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
    /* package */ void shiftRightVariableIntoVariable(final short x, final short y) {
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
    /* package */ void shiftLeftVariableIntoVariable(final short x, final short y) {
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
    /* package */ boolean compareVariableWithValue(final short x, final short value) {
        final int xValue = getVariable(variables, x).getAsInt();

        return xValue == uint(value);
    }

    /**
     * @return true if equal
     */
    /* package */ boolean compareVariableWithVariable(final short x, final short y) {
        final int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        return xValue == yValue;
    }

    /**
     * @return true if Vx-th bit was set
     */
    /* package */ boolean compareInputWithVariable(final short x) {
        final int inValue = input.getAsInt();
        final int bit = getVariable(variables, x).getAsInt();

        assert bit < 0x10 : "input comparison should be in range 0-F";

        final int bitMask = 1 << bit;
        final boolean bitSet = (inValue & bitMask) != 0;

        LOG.debug(() -> toHexString(ubyte(bit)) + "th input bit was " + (bitSet ? "" : "UN") + "SET");

        return bitSet;
    }

    // Logical operations -----------------------

    /* package */ void andVariableWithVariable(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue & yValue;

        getVariable(variables, x).set(xValue);
    }

    /* package */ void orVariableWithVariable(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue | yValue;

        getVariable(variables, x).set(xValue);
    }

    /* package */ void xorVariableWithVariable(final short x, final short y) {
        int xValue = getVariable(variables, x).getAsInt();
        final int yValue = getVariable(variables, y).getAsInt();

        xValue = xValue ^ yValue;

        getVariable(variables, x).set(xValue);
    }

    // Special operations -----------------------

    /* package */ void andVariableWithRandom(final short x, final short value) {
        int kkValue = uint(value);
        int random = randomSource.applyAsInt(256);

        int xValue = kkValue & random;
        //xValue = random % (kkValue + 1); //Rocket2 requires this implementation

        getVariable(variables, x).set(xValue);
    }
}
