package net.novaware.chip8.core.cpu.unit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class ClockJvmImpl implements Clock {

    private final String name;

    private ScheduledExecutorService executor;

    private int frequency;
    private Runnable target;

    private int delay;
    private ScheduledFuture<?> future;

    public ClockJvmImpl(String name) {
        this.name = name;

        executor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Chip8-Clock-" + this.name));
    }

    @Override
    public void setFrequency(int frequency) {
        this.frequency = frequency;

        recalculateDelay();
        maybeRestart();
    }

    private void maybeRestart() {
        //TODO: check if running, if yes, restart?
    }

    private void recalculateDelay() {
        //TODO: recalculate delay
    }

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public void setTarget(Runnable target) {
        this.target = target;
    }

    @Override
    public Runnable getTarget() {
        return target;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
