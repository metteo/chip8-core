package net.novaware.chip8.core.clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;

//FIXME: check errors from futures!
public class ScheduledClockGenerator implements ClockGenerator {

    private final String name;

    private ScheduledExecutorService executor;

    public ScheduledClockGenerator(String name) {
        this.name = name;

        executor = Executors.newScheduledThreadPool(1,
                r -> new Thread(r, "Chip8-" + this.name + "-Clock"));
    }

    @Override
    public Handle schedule(Runnable target, int frequency) {
        final ScheduledFuture<?> future = schedule0(target, frequency);

        return future::cancel;
    }

    private ScheduledFuture<?> schedule0(Runnable target, int frequency) {
        final long period = (long)((double) TimeUnit.SECONDS.toNanos(1) / frequency);

        return executor.scheduleAtFixedRate(target, 1, period, TimeUnit.NANOSECONDS);
    }

    //FIXME: rewrite and test!
    @Override
    public Handle schedule(Runnable target, IntSupplier frequency) {
        final AtomicInteger freq = new AtomicInteger(frequency.getAsInt());
        final AtomicReference<ScheduledFuture<?>> sfr = new AtomicReference<>();

        final ScheduledFuture<?> sf1 = executor.scheduleAtFixedRate(() -> {
            if (freq.get() != frequency.getAsInt()) {
                sfr.get().cancel(false);

                freq.set(frequency.getAsInt());
                sfr.set(schedule0(target, freq.get()));
            }
        }, 1, 1, TimeUnit.SECONDS);

        final ScheduledFuture<?> sf2 = schedule0(target, freq.get());
        sfr.set(sf2);

        return force -> {
            sf1.cancel(force);
            return sfr.get().cancel(force);
        };
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
