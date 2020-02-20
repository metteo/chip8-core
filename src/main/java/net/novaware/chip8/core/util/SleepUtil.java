package net.novaware.chip8.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @see <a href="https://stackoverflow.com/questions/824110/accurate-sleep-for-java-on-windows">Accurate Sleep for Java on Windows</a>
 */
public class SleepUtil {

    private static final Logger LOG = LogManager.getLogger();

    private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);
    private static final long SPIN_YIELD_PRECISION = TimeUnit.MILLISECONDS.toNanos(1);

    private static final long ACCURACY = 10_000; // nanos

    //TODO: use LockSupport.parkNanos() instead?

    public static void sleepNanos (long nanoDuration) throws InterruptedException {
        final long end = System.nanoTime() + nanoDuration;

        long timeLeft = nanoDuration;

        do {
            if (timeLeft > SLEEP_PRECISION) {
                Thread.sleep(1);
            } else if (timeLeft > SPIN_YIELD_PRECISION) {
                Thread.sleep(0);
            } else {
                Thread.yield();
            }

            timeLeft = end - System.nanoTime();

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

        } while (timeLeft > ACCURACY);

        long diff = System.nanoTime() - end;
        if (diff > ACCURACY) {
            LOG.warn("sleepNanos not that good, diff: " + diff + " nanos");
        }
    }
}
