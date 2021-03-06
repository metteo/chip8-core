package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.CpuState;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.util.di.BoardScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.CpuState.*;
import static net.novaware.chip8.core.cpu.register.RegisterModule.CPU_STATE;

/**
 * Power Management Unit
 */
@BoardScope
public class PowerMgmt implements Unit {

    private static final Logger LOG = LogManager.getLogger();

    private final ByteRegister cpuState;

    @Inject
    public PowerMgmt(
        @Named(CPU_STATE) final ByteRegister cpuState
    ) {
        this.cpuState = cpuState;
    }

    @Override
    public void initialize() {
        setState(OPERATING);
    }

    @Override
    public void reset() {
        setState(OPERATING);
    }

    public CpuState getState() {
        final CpuState cpuState = valueOf(this.cpuState.get());

        if (cpuState == null) {
            throw new IllegalStateException("unknown cpu state");
        }

        return cpuState;
    }

    public void setState(CpuState state) {
        requireNonNull(state, "state must not be null");

        LOG.info("Switching CPU to " + state + " state.");
        cpuState.set(state.value());
    }

    /**
     * Switch from {@link CpuState#OPERATING} to {@link CpuState#HALT}
     */
    public void halt() {
        if (getState() == OPERATING) {
            setState(HALT);
        }
    }

    /**
     * Switch from {@link CpuState#HALT} to {@link CpuState#OPERATING}
     * <p>
     * NOTE: This method should be named 'continue' but it's a reserved
     * java keyword so we use a abbreviation that matches reverse operation
     * {@link #halt()}
     */
    public void cont() {
        if (getState() == HALT) {
            setState(OPERATING);
        }
    }

    /**
     * Switch from {@link CpuState#OPERATING} to {@link CpuState#STOP_CLOCK}
     */
    public void stopClock() {
        if (getState() == OPERATING) {
            setState(STOP_CLOCK);
        }
    }

    /**
     * Switch from {@link CpuState#STOP_CLOCK} to {@link CpuState#OPERATING}
     */
    public void startClock() {
        if (getState() == STOP_CLOCK) {
            setState(OPERATING);
        }
    }

    /**
     * Goes to {@link CpuState#SLEEP}
     */
    public void sleep() {
        if (getState() != SLEEP) { // to prevent callback
            setState(SLEEP);
        }
    }

    /**
     * Switch from {@link CpuState#SLEEP} to {@link CpuState#OPERATING}
     */
    public void wakeUp() {
        if (getState() == SLEEP) {
            setState(OPERATING);
        }
    }
}
