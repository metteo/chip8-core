package net.novaware.chip8.core.cpu.unit;

public interface Unit {
    default void initialize() {}
    default void reset() {}
}
