package net.novaware.chip8.core.util;

import java.util.concurrent.*;

/**
 *
 */
public class AccurateScheduledExecutor {

    private Thread thread;

    public AccurateScheduledExecutor(final String name) {
        this.thread = new Thread(this::run, name);
    }

    /**
     * Method interface borrowed from
     * {@link ScheduledExecutorService#schedule(Runnable, long, TimeUnit)}
     *
     * @param command to be executed
     * @param delay value <= 0 means immediate initial execution
     * @param unit {@link TimeUnit} for delay
     * @return future object which allows canceling.
     *
     * @see ScheduledExecutorService#schedule(Runnable, long, TimeUnit)
     */
    public ScheduledFuture<?> schedule(final Runnable command,
                                       final long delay,
                                       final TimeUnit unit
    ) {
        throw new UnsupportedOperationException("not implemented"); // TODO: implement
    }

    /**
     * Method interface borrowed from
     * {@link ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)}
     *
     * @param command to be executed
     * @param initialDelay value <= 0 means immediate initial execution
     * @param period time between beginnings of executions
     * @param unit {@link TimeUnit} for initialDelay & period
     * @return future object which allows canceling.
     *
     * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command,
                                           final long initialDelay,
                                           final long period,
                                           final TimeUnit unit
    ) {
        throw new UnsupportedOperationException("not implemented"); // TODO: implement
    }

    public void shutdown() {
        throw new UnsupportedOperationException("not implemented"); // TODO: implement
    }

    public boolean isShutdown() {
        throw new UnsupportedOperationException("not implemented"); // TODO: implement
    }

    private void run() {
        throw new UnsupportedOperationException("not implemented"); // TODO: implement
    }
}
