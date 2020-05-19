package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.CpuState;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.unit.PowerMgmt;
import net.novaware.chip8.core.port.DebugPort;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

public class DebugPortImpl implements DebugPort {

    private final ByteRegister delayTimer;

    private final ByteRegister soundTimer;

    private final ByteRegister cpuState;

    private final PowerMgmt powerMgmt;

    private @Nullable Receiver receiver;

    public DebugPortImpl(
            ByteRegister delayTimer,
            ByteRegister soundTimer,
            ByteRegister cpuState, PowerMgmt powerMgmt
    ) {
        this.delayTimer = delayTimer;
        this.soundTimer = soundTimer;
        this.cpuState = cpuState;
        this.powerMgmt = powerMgmt;
    }

    public void attachToRegister() {
        delayTimer.subscribe(this::onDelayTimerChange);
        soundTimer.subscribe(this::onSoundTimerChange);
        cpuState.subscribe(this::onCpuStateChange);
    }

    private void onCpuStateChange(ByteRegister cpuState) {
        if(receiver != null) {
            receiver.onStateChange(powerMgmt.getState() == CpuState.SLEEP);
        }
    }

    private void onDelayTimerChange(ByteRegister delay) {
        if (receiver != null) {
            receiver.onDelayTimerChange(delay.getAsInt());
        }
    }

    private void onSoundTimerChange(ByteRegister sound) {
        if (receiver != null) {
            receiver.onSoundTimerChange(sound.getAsInt());
        }
    }

    public void onException(Exception exception) {
        if (receiver != null) {
            receiver.onException(exception);
        }
    }

    public void onCpuFrequencyChange(int frequency) {
        if (receiver != null) {
            receiver.onCpuFrequencyChange(frequency);
        }
    }

    @Override
    public void connect(Receiver receiver) {
        requireNonNull(receiver, "receiver must not be null");

        this.receiver = receiver;
    }

    public boolean hasReceiver() {
        return receiver != null;
    }

    @Override
    public void disconnect() {
        receiver = null;
    }
}
