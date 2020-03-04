package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.util.uml.Uses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import static net.novaware.chip8.core.cpu.register.Registers.getVariable;

/**
 * Special unit attached to a timer register which decreases it
 */
public class Timer {

    private static final Logger LOG = LogManager.getLogger();

    @Uses
    private final ByteRegister[] variables;

    @Uses
    private final ByteRegister timerRegister;

    @Uses
    @Nullable
    private final ByteRegister outputRegister;

    public Timer(
            final ByteRegister[] variables,
            final ByteRegister timerRegister,
            final @Nullable ByteRegister outputRegister
    ) {
        this.variables = variables;
        this.timerRegister = timerRegister;
        this.outputRegister = outputRegister;
    }

    public Timer(final ByteRegister[] variables, final ByteRegister timerRegister) {
        this(variables, timerRegister, null);
    }

    public void init() {
        LOG.debug(() -> "Configured with " + timerRegister.getName());

        if (outputRegister != null) {
            LOG.debug(() -> "Will report values higher than 1 to " + outputRegister.getName());
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

    public void storeTimerIntoVariable(short x) {
        byte currentDelay = timerRegister.get();
        getVariable(variables, x).set(currentDelay);
    }

    public void loadVariableIntoTimer(short x) {
        byte delay = getVariable(variables, x).get();
        timerRegister.set(delay);
    }

    public void maybeDecrementValue() {
        int intValue = timerRegister.getAsInt();

        if (intValue > 0) {
            timerRegister.set(intValue - 1);
        }
    }
}
