package net.novaware.chip8.core.clock;

import java.util.function.IntSupplier;

//TODO: implement using a loop for high frequency tests to get rid of scheduling overhead / delays
public class LoopedClockGenerator implements ClockGenerator {

    private final String name;

    public LoopedClockGenerator(String name) {
        this.name = name;
    }

    @Override
    public Handle schedule(Runnable target, int frequency) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Handle schedule(Runnable target, IntSupplier frequency) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Handle schedule(Runnable target) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isPaused() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setPaused(boolean paused) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("not implemented");
    }
}
