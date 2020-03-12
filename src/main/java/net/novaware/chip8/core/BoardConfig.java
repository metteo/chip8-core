package net.novaware.chip8.core;

/**
 * DIP Switch working at runtime
 */
public class BoardConfig {

    private int cpuFrequency = 500; // Hz

    private int delayTimerFrequency = 60; // Hz

    private int soundTimerFrequency = 60; // Hz

    private int renderTimerFrequency = 60; // Hz

    private boolean strictMode = true;

    private boolean enforceMemoryRoRwState = true; //strict

    private boolean enforceStackSize; //strict

    // e.g. can't write to stack and gpu in the same call
    private boolean disallowCrossingMemoryBoundaries; //strict

    private boolean legacyMode = true;

    /**
     * If true, uses Y instead of X during as source during shifting
     */
    private boolean legacyShift = true;

    /**
     * If true, increments I during load and store operations
     */
    private boolean legacyLoadStore = true;

    /**
     * If true, adding register value to index that causes overflow is reported using VF
     */
    private boolean legacyAddressSum = true;

    private boolean haltOnInfJump;

    private boolean stickyKeys;

    private boolean tracing;

    private boolean logging; // ??

    private boolean debugging; // ??

    public boolean isEnforceMemoryRoRwState() {
        return enforceMemoryRoRwState;
    }

    public void setEnforceMemoryRoRwState(boolean enforceMemoryRoRwState) {
        this.enforceMemoryRoRwState = enforceMemoryRoRwState;
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

    public int getRenderTimerFrequency() {
        return renderTimerFrequency;
    }

    public void setRenderTimerFrequency(int renderTimerFrequency) {
        this.renderTimerFrequency = renderTimerFrequency;
    }

    public boolean isLegacyShift() {
        return legacyShift;
    }

    public void setLegacyShift(boolean legacyShift) {
        this.legacyShift = legacyShift;
    }

    public boolean isLegacyLoadStore() {
        return legacyLoadStore;
    }

    public void setLegacyLoadStore(boolean legacyLoadStore) {
        this.legacyLoadStore = legacyLoadStore;
    }

    public boolean isLegacyAddressSum() {
        return legacyAddressSum;
    }

    public void setLegacyAddressSum(boolean legacyAddressSum) {
        this.legacyAddressSum = legacyAddressSum;
    }
}
