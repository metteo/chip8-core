package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.cpu.unit.*;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Arrays.stream;
import static net.novaware.chip8.core.cpu.CpuState.OPERATING;
import static net.novaware.chip8.core.cpu.register.RegisterFile.VF_EMPTY;
import static net.novaware.chip8.core.cpu.unit.UnitModule.DELAY;
import static net.novaware.chip8.core.cpu.unit.UnitModule.SOUND;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;

/**
 * Central Processing Unit (CPU)
 */
@BoardScope
public class Cpu implements Unit {

    public interface Config {

    }

    @Owned
    private final Config config;

    @Owned
    private final RegisterFile registers;

    @Owned
    private final ControlUnit controlUnit;

    @Owned
    private final LoadStore lsu;

    @Owned
    private final ArithmeticLogic alu;

    @Owned
    private final AddressGen agu;

    @Owned
    private final StackEngine stackEngine;

    @Owned
    private final PowerMgmt powerMgmt;

    @Owned
    private final Gpu gpu;

    @Owned
    private final Timer delayTimer;

    @Owned
    private final Timer soundTimer;

    @Used
    private final Memory memory;

    @Inject
    public Cpu(
        final Config config,
        @Named(MMU) final Memory memory,
        final RegisterFile registers,

        final LoadStore lsu,
        final ArithmeticLogic alu,
        final AddressGen agu,
        final StackEngine stackEngine,
        final PowerMgmt powerMgmt,
        final Gpu gpu,

        final ControlUnit controlUnit,

        @Named(DELAY) final Timer delayTimer,
        @Named(SOUND) final Timer soundTimer
    ) {
        this.config = config;
        this.memory = memory;
        this.registers = registers;

        this.lsu = lsu;
        this.alu = alu;
        this.agu = agu;
        this.stackEngine = stackEngine;
        this.powerMgmt = powerMgmt;
        this.gpu = gpu;

        this.controlUnit = controlUnit;

        this.delayTimer = delayTimer;
        this.soundTimer = soundTimer;
    }

    @Override
    public void initialize() {
        lsu.initialize();
        alu.initialize();
        agu.initialize();
        stackEngine.initialize();
        powerMgmt.initialize();

        controlUnit.initialize();

        delayTimer.initialize();
        soundTimer.initialize();

        //unhalt / start clock after input change
        registers.getInput().setCallback(in -> {
            powerMgmt.cont();
            powerMgmt.startClock();
        });
    }

    public void sleep() {
        powerMgmt.sleep();
    }

    public void wakeUp() {
        powerMgmt.wakeUp();
    }

    @Override
    public void reset() {
        lsu.reset();
        alu.reset();
        agu.reset();
        stackEngine.reset();
        powerMgmt.reset();
        gpu.reset();

        controlUnit.reset();

        delayTimer.reset();
        soundTimer.reset();

        registers.getStatusType().set(VF_EMPTY);
    }

    public RegisterFile getRegisters() {
        return registers;
    }

    /**
     * Virtual Chip8 Processor cycle.
     * <p>
     * On the original - CDP18S711, single virtual instruction could take
     * multiple instructions / cycles to complete.
     * <p>
     * Supports clock gating
     */
    public void cycle() {
        if (registers.getCpuState().get() == OPERATING.value()) {
            cycle0();
        }
    }

    private void cycle0() {
        controlUnit.fetch();
        controlUnit.decode();
        controlUnit.execute();
    }

    /**
     * Supports clock gating
     */
    public void delayTick() {
        if (registers.getCpuState().get() == OPERATING.value()) {
            delayTimer.tick();
        }
    }

    /**
     * Supports clock gating
     */
    public void soundTick() {
        if (registers.getCpuState().get() == OPERATING.value()) {
            soundTimer.tick();
        }
    }
}
