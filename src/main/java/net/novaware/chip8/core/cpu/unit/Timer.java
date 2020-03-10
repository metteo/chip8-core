package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import static net.novaware.chip8.core.cpu.register.RegisterFile.getVariable;

/**
 * Special unit attached to a timer register which decreases it
 */
public class Timer implements Unit {

    private static final Logger LOG = LogManager.getLogger();

    @Used
    private final ByteRegister[] variables;

    @Used
    private final ByteRegister timerRegister;

    @Used
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

    @Override
    public void initialize() {
        zeroOutRegisters();
        configureOutput();
    }

    @Override
    public void reset() {
        zeroOutRegisters();
    }

    private void zeroOutRegisters() {
        timerRegister.set(0);

        if (outputRegister != null) {
            outputRegister.set(0);
        }
    }

    private void configureOutput() {
        LOG.debug(() -> "Configured with " + timerRegister.getName());

        if (outputRegister != null) {
            configureOutput(outputRegister);
        }
    }

    private void configureOutput(final ByteRegister register) {
        LOG.debug(() -> "Will report values higher than 1 to " + register.getName());
        timerRegister.setCallback(r -> {
            final int timer = timerRegister.getAsInt();
            final int state = register.getAsInt();

            if (timer > 1 && state == 0) {
                register.set(1);
            }

            if (timer < 1 && state != 0) {
                register.set(0);
            }
        });
    }

    /**
     * @return true if any work was done
     */
    public boolean tick() {
        int intValue = timerRegister.getAsInt();

        if (intValue > 0) {
            timerRegister.set(intValue - 1);
            return true;
        }

        return false;
    }

    public void storeTimerIntoVariable(short x) {
        byte currentDelay = timerRegister.get();
        getVariable(variables, x).set(currentDelay);
    }

    public void loadTimerWithVariable(short x) {
        byte delay = getVariable(variables, x).get();
        timerRegister.set(delay);
    }
}
