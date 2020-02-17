package net.novaware.chip8.core;

/**
 * DIP Switch working at runtime
 */
public class BoardConfig {

    private int cpuFrequency = 1500; // Hz, ~60fps in Invaders

    private int delayTimerFrequency = 60; // Hz

    private int soundTimerFrequency = 60; // Hz

    private boolean strictMode;

    private boolean enforceMemoryRoRwState = true; //strict

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

    public boolean isEnforceMemoryRoRwState() {
        return enforceMemoryRoRwState;
    }

    public int getCpuFrequency() {
        return cpuFrequency;
    }

    public void setCpuFrequency(int cpuFrequency) {
        this.cpuFrequency = cpuFrequency;
    }

    public int getDelayTimerFrequency() {
        return delayTimerFrequency;
    }

    public void setDelayTimerFrequency(int delayTimerFrequency) {
        this.delayTimerFrequency = delayTimerFrequency;
    }

    public int getSoundTimerFrequency() {
        return soundTimerFrequency;
    }

    public void setSoundTimerFrequency(int soundTimerFrequency) {
        this.soundTimerFrequency = soundTimerFrequency;
    }
}
