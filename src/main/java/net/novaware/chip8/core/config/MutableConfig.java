package net.novaware.chip8.core.config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: @ThreadSafe here
//TODO: allow semi-dynamic changes of frequencies (adjust clocks on change)
public class MutableConfig implements CoreConfig {

    private final AtomicInteger cpuFrequency = new AtomicInteger(500); // Hz

    private final AtomicInteger delayTimerFrequency = new AtomicInteger(60); // Hz

    private final AtomicInteger soundTimerFrequency = new AtomicInteger(60); // Hz

    private final AtomicInteger renderTimerFrequency = new AtomicInteger(60); // Hz

    private final AtomicBoolean enforceMemoryRoRwState = new AtomicBoolean(false); //strict

    private final AtomicBoolean legacyShift = new AtomicBoolean(true);

    private final AtomicBoolean legacyLoadStore = new AtomicBoolean(true);

    private final AtomicBoolean legacyAddressSum = new AtomicBoolean(true);

    private final AtomicBoolean trimVarForFont = new AtomicBoolean(true);

    private final AtomicBoolean wrapping = new AtomicBoolean(true);

    private final AtomicBoolean verticalClipping = new AtomicBoolean(false);

    private final AtomicBoolean horizontalClipping = new AtomicBoolean(false);

    @Override
    public int getCpuFrequency() {
        return cpuFrequency.get();
    }

    @Override
    public int getDelayTimerFrequency() {
        return delayTimerFrequency.get();
    }

    @Override
    public int getSoundTimerFrequency() {
        return soundTimerFrequency.get();
    }

    @Override
    public int getRenderTimerFrequency() {
        return renderTimerFrequency.get();
    }

    @Override
    public boolean isEnforceMemoryRoRwState() {
        return enforceMemoryRoRwState.get();
    }

    @Override
    public boolean isLegacyShift() {
        return legacyShift.get();
    }

    @Override
    public boolean isLegacyLoadStore() {
        return legacyLoadStore.get();
    }

    @Override
    public boolean isLegacyAddressSum() {
        return legacyAddressSum.get();
    }

    @Override
    public boolean isTrimVarForFont() {
        return trimVarForFont.get();
    }

    @Override
    public boolean isWrapping() {
        return wrapping.get();
    }

    @Override
    public boolean isVerticalClipping() {
        return verticalClipping.get();
    }

    @Override
    public boolean isHorizontalClipping() {
        return horizontalClipping.get();
    }

    public void setCpuFrequency(int cpuFrequency) {
        this.cpuFrequency.set(cpuFrequency);
    }

    public void setDelayTimerFrequency(int delayTimerFrequency) {
        this.delayTimerFrequency.set(delayTimerFrequency);
    }

    public void setSoundTimerFrequency(int soundTimerFrequency) {
        this.soundTimerFrequency.set(soundTimerFrequency);
    }

    public void setRenderTimerFrequency(int renderTimerFrequency) {
        this.renderTimerFrequency.set(renderTimerFrequency);
    }

    public void setEnforceMemoryRoRwState(boolean enforceMemoryRoRwState) {
        this.enforceMemoryRoRwState.set(enforceMemoryRoRwState);
    }

    public void setLegacyShift(boolean legacyShift) {
        this.legacyShift.set(legacyShift);
    }

    public void setLegacyLoadStore(boolean legacyLoadStore) {
        this.legacyLoadStore.set(legacyLoadStore);
    }

    public void setLegacyAddressSum(boolean legacyAddressSum) {
        this.legacyAddressSum.set(legacyAddressSum);
    }

    public void setTrimVarForFont(boolean trimVarForFont) {
        this.trimVarForFont.set(trimVarForFont);
    }

    public void setWrapping(boolean wrapping) {
        this.wrapping.set(wrapping);
    }

    public void setVerticalClipping(boolean verticalClipping) {
        this.verticalClipping.set(verticalClipping);
    }

    public void setHorizontalClipping(boolean horizontalClipping) {
        this.horizontalClipping.set(horizontalClipping);
    }
}
