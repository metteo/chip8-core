package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.cpu.unit.*;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryModule;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.CpuState.*;
import static net.novaware.chip8.core.cpu.unit.UnitModule.DELAY;
import static net.novaware.chip8.core.cpu.unit.UnitModule.SOUND;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;

/**
 * Central Processing Unit (CPU)
 */
@BoardScope
public class Cpu {

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

    public void initialize() {
        //TODO: move it to interpreter and start from 0x0000
        //TODO: delegate to subunits what can't be done in interpreter
        registers.getProgramCounter().set(MemoryModule.PROGRAM_START);

        agu.initialize();
        stackEngine.initialize();

        delayTimer.initialize();
        soundTimer.initialize();

        powerMgmt.setState(OPERATING);

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

    public void reset() {
        agu.reset();
        stackEngine.reset();

        registers.getMemoryAddress().set(0);
        registers.getProgramCounter().set(MemoryModule.PROGRAM_START);
        registers.getIndex().set(0);
        registers.getDelay().set(0);
        registers.getSound().set(0);
        registers.getSoundOn().set(0);

        ByteRegister[] vars = registers.getVariables();
        for (int i = 0; i < vars.length; ++i) {
            vars[i].set(0);
        }
    }

    public RegisterFile getRegisters() {
        return registers;
    }

    /**
     * Virtual Chip8 Processor cycle.
     *
     * On the original - CDP18S711, single virtual instruction could take
     * multiple instructions / cycles to complete.
     *
     * Supports clock gating
     */
    public void cycle() {
        if (registers.getCpuState().get() == OPERATING.value()) {
            controlUnit.fetch();
            controlUnit.decode();
            controlUnit.execute();
        }
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
