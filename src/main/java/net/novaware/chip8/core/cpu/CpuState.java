package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.clock.ClockGenerator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.Unsigned;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;

public enum CpuState {
    /**
     * Cpu working normally
     */
    OPERATING(0x00),
    /**
     * {@link ClockGenerator} paused, key input wakes it up
     * <p>
     * Usually triggered by key wait instruction
     */
    HALT(0x10),
    /**
     * {@link ClockGenerator} stopped, key input starts it up
     * <p>
     * Usually triggered by infinite jump instruction (jump to the same place in memory)
     * Or an error during execution
     */
    STOP_CLOCK(0x20),
    /**
     * {@link ClockGenerator} stopped and CPU detached from it.
     * <p>
     * Triggered by pausing the Board. Only resume can wake it up
     */
    SLEEP(0x30),
    ;

    private static final List<CpuState> instances = List.of(values());

    private static final Map<Byte, CpuState> byValue = getInstances().stream()
            .collect(toUnmodifiableMap(CpuState::value, identity()));

    @Unsigned
    private final byte value;

    CpuState(final int value) {
        this.value = ubyte(value);
    }

    @Unsigned
    public byte value() {
        return value;
    }

    /**
     * Not using {@link java.util.Optional} on purpose here.
     *
     * @return null if opcode is unrecognized
     */
    @Nullable
    public static CpuState valueOf(byte value) {
        return byValue.get(value);
    }

    /**
     * Prevents allocation of array holding the values() (done only once at enum loading)
     *
     * @see <a href="https://www.javacodegeeks.com/2018/08/memory-hogging-enum-values-method.html">Memory-Hogging Enum.values()</a>
     */
    public static List<CpuState> getInstances() {
        return instances;
    }
}
