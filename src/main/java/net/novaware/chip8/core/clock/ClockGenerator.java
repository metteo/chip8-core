package net.novaware.chip8.core.clock;

import java.util.function.IntSupplier;

public interface ClockGenerator {

    interface Handle {
        boolean cancel(boolean mayInterrupt);
    }

    Handle schedule(Runnable target, int frequency);

    Handle schedule(Runnable target, IntSupplier frequency);

    Handle schedule(Runnable target);

    boolean isPaused();

    void setPaused(boolean paused);

    void shutdown();
}
