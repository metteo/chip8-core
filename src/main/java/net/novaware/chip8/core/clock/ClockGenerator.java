package net.novaware.chip8.core.clock;

public interface ClockGenerator {

    /**
     * @param frequency in Hz
     */
    void setFrequency(int frequency);

    int getFrequency();

    void setTarget(Runnable target);

    Runnable getTarget();

    void start();

    void stop();
}
