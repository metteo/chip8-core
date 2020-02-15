package net.novaware.chip8.core;

/**
 * DIP Switch working at runtime
 */
public class BoardConfig {

    private int cpuFrequency;

    private int delayTimerFrequency;

    private int soundTimerFrequency;

    private boolean strictMode;

    private boolean enforceMemoryRoRwState; //strict

    private boolean enforceStackSize; //strict

    // e.g. can't write to stack and gpu in the same call
    private boolean disallowCrossingMemoryBoundaries; //strict

    private boolean shiftQuirk;

    private boolean loadStoreQuirk;

    private boolean addressSumOverflowQuirk;

    private boolean haltOnInfJump;

    private boolean stickyKeys;

    private boolean tracing;

    private boolean logging; // ??

    private boolean debugging; // ??
}
