package net.novaware.chip8.core.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClockGeneratorJvmImpl implements ClockGenerator {

    private final String name;

    private ScheduledExecutorService executor;

    public ClockGeneratorJvmImpl(String name) {
        this.name = name;

        executor = Executors.newScheduledThreadPool(1,
                r -> new Thread(r, "Chip8-" + this.name + "-Clock"));
    }

    @Override
    public Handle schedule(Runnable target, int frequency) {
        final long period = (long)((double)TimeUnit.SECONDS.toNanos(1) / frequency);

        final ScheduledFuture<?> future =
                executor.scheduleAtFixedRate(target, 1, period, TimeUnit.NANOSECONDS);

        return future::cancel;
    }

    @Override
    public Handle schedule(Runnable target) {
        final ScheduledFuture<?> future = executor.schedule(target, 1, TimeUnit.NANOSECONDS);
        return future::cancel;
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
        executor.shutdown();
    }
}
