package net.novaware.chip8.core.clock;

public interface ClockGenerator {

    interface Handle {
        boolean cancel(boolean mayInterrupt);
    }

    Handle schedule(Runnable target, int frequency);

    Handle schedule(Runnable target);

    boolean isPaused();

    void setPaused(boolean paused);
}
