package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * Special unit attached to a timer register which decreases it
 */
public class Timer {

    private static final Logger LOG = LogManager.getLogger();

    private final ByteRegister timerRegister;

    @Nullable
    private final ByteRegister outputRegister;

    public Timer(ByteRegister timerRegister, @Nullable ByteRegister outputRegister) {
        this.timerRegister = timerRegister;
        this.outputRegister = outputRegister;
    }

    public Timer(ByteRegister timerRegister) {
        this(timerRegister, null);
    }

    public void init() {
        LOG.info(() -> "Configured with " + timerRegister.getName());

        if (outputRegister != null) {
            LOG.info(() -> "Will report values higher than 1 to " + outputRegister.getName());
            timerRegister.setCallback(r -> {
                final int timer = timerRegister.getAsInt();
                final int state = outputRegister.getAsInt();

                if (timer > 1 && state == 0) {
                    outputRegister.set(1);
                }

                if (timer < 1 && state != 0) {
                    outputRegister.set(0);
                }
            });
        }
    }

    public void maybeDecrementValue() {
        int intValue = timerRegister.getAsInt();

        if (intValue > 0) {
            timerRegister.set(intValue - 1);
        }
    }
}
