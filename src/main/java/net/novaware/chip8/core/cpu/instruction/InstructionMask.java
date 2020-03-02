package net.novaware.chip8.core.cpu.instruction;

import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.List;

import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * NOTE: Every mask instance name starts with capital 'O', not 0 because of Java syntax rules
 */
public enum InstructionMask {
    /**
     * Full mask used in parameter-less instructions
     */
    OxFFFF(0xFFFF),
    /**
     * Single register mask
     */
    OxF0FF(0xF0FF),
    /**
     * Double register mask
     */
    OxF00F(0xF00F),

    /**
     * Triple nibble mask for address instructions and those involving multiple params.
     */
    OxF000(0xF000),
    ;

    private static final List<InstructionMask> instances = List.of(values());

    @Unsigned
    private final short value;

    InstructionMask(final int value) {
        this.value = ushort(value);
    }

    @Unsigned
    public short value() {
        return value;
    }

    /**
     * Prevents allocation of array holding the values() (done only once at enum loading)
     *
     * @see <a href="https://www.javacodegeeks.com/2018/08/memory-hogging-enum-values-method.html">Memory-Hogging Enum.values()</a>
     */
    public static List<InstructionMask> getInstances() {
        return instances;
    }
}
