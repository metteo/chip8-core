package net.novaware.chip8.core.port;

/**
 * Provides debug / diagnostics facilities.
 *
 * WARNING: {@link Receiver} methods are called on core thread, dispatch the execution to another thread to prevent
 * unstable cpu frequency / threading issues
 */
public interface DebugPort extends OutputPort {

    //TODO: safe access to registers
    //TODO: safe access to memory
    //TODO: safe control of cpu

    interface Receiver {
        /**
         * Called when exception is thrown in core thread
         */
        void onException(Exception exception);

        void onDelayTimerChange(int value);

        void onSoundTimerChange(int value);

        /**
         * Reports real (calculated) cpu frequency (NOT the one set in config)
         */
        void onCpuFrequencyChange(int value);

        void onStateChange(boolean paused);
    }

    void connect(Receiver receiver);

    void disconnect();
}
