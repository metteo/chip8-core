package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.cpu.instruction.InstructionDecoder;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.*;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryModule;
import net.novaware.chip8.core.util.uml.Owns;
import net.novaware.chip8.core.util.uml.Uses;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static net.novaware.chip8.core.memory.MemoryModule.MMU;

/**
 * Central Processing Unit (CPU)
 */
@Singleton
public class Cpu {

    // https://www.golinuxhub.com/2018/06/what-cpu-c-states-check-cpu-core-linux.html
    public enum State {
        OPERATING,
        HALT,
        STOP_CLOCK,
        SLEEP,
        ;
    }

    public interface Config {

    }

    @Owns
    private final Config config;

    @Owns
    private final Registers registers;

    @Owns
    private final ControlUnit controlUnit;

    @Owns
    private final ArithmeticLogic alu;

    @Owns
    private final AddressGeneration agu;

    @Owns
    private final StackEngine stackEngine;

    @Owns
    private final Gpu gpu;

    @Owns
    private final Timer delayTimer;

    @Owns
    private final Timer soundTimer;

    @Uses
    private final Memory memory;

    //TODO: make it a register and only expose a getter with enum
    private State state;

    @Inject
    public Cpu(
            final Config config,
            @Named(MMU) final Memory memory,
            final Registers registers,

            final ArithmeticLogic alu,
            final AddressGeneration agu,
            final StackEngine stackEngine,
            final Gpu gpu,

            final ControlUnit controlUnit,

            @Named("delay") final Timer delayTimer,
            @Named("sound") final Timer soundTimer
    ) {
        this.config = config;
        this.memory = memory;
        this.registers = registers;

        this.alu = alu;
        this.agu = agu;
        this.stackEngine = stackEngine;
        this.gpu = gpu;

        this.controlUnit = controlUnit;

        this.delayTimer = delayTimer;
        this.soundTimer = soundTimer;
    }

    public void initialize() { //TODO: move it to interpreter and start from 0x0000
        registers.getProgramCounter().set(MemoryModule.PROGRAM_START);
        registers.getStackPointer().set(MemoryModule.STACK_START);
        registers.getStackSegment().set(MemoryModule.STACK_START);
        registers.getGraphicSegment().set(MemoryModule.DISPLAY_IO_START);

        delayTimer.init();
        soundTimer.init();

        state = State.OPERATING;
    }

    public void reset() {
        registers.getMemoryAddress().set(0);
        registers.getProgramCounter().set(MemoryModule.PROGRAM_START);
        registers.getStackPointer().set(MemoryModule.STACK_START);
        registers.getIndex().set(0);
        registers.getDelay().set(0);
        registers.getSound().set(0);
        registers.getSoundOn().set(0);

        ByteRegister[] vars = registers.getVariables();
        for (int i = 0; i < vars.length; ++i) {
            vars[i].set(0);
        }
    }

    public Registers getRegisters() {
        return registers;
    }

    /**
     * Virtual Chip8 Processor cycle.
     *
     * On the original - CDP18S711, single virtual instruction could take
     * multiple instructions / cycles to complete.
     */
    public void cycle() {
        if (state == State.OPERATING) {
            controlUnit.fetch();
            controlUnit.decode();
            controlUnit.execute();
        }
    }

    public void delayTick() {
        if (state == State.OPERATING) {
            delayTimer.maybeDecrementValue();
        }
    }

    public void soundTick() {
        if (state == State.OPERATING) {
            soundTimer.maybeDecrementValue();
        }
    }
}
