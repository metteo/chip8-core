package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.*;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryMap;
import net.novaware.chip8.core.util.uml.Owns;
import net.novaware.chip8.core.util.uml.Uses;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Random;

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
        boolean isLegacyShift();

        boolean isLegacyLoadStore();

        boolean isLegacyAddressSum();
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
    private final GraphicsProcessing gpu;

    @Uses
    private final Memory memory;

    @Inject
    public Cpu(final Config config, @Named("cpu") final Memory memory, final Registers registers) {
        this.config = config;
        this.memory = memory;
        this.registers = registers;

        alu = new ArithmeticLogic(new Random()::nextInt, registers, memory);
        agu = new AddressGeneration(registers, memory);
        stackEngine = new StackEngine(registers, memory);
        gpu = new GraphicsProcessing(registers, memory);

        ControlUnit.Config cuConfig = new ControlUnit.Config() {
            @Override public boolean isLegacyShift()      { return config.isLegacyShift(); }
            @Override public boolean isLegacyLoadStore()  { return config.isLegacyLoadStore(); }
            @Override public boolean isLegacyAddressSum() { return config.isLegacyAddressSum(); }
        };

        controlUnit = new ControlUnit(cuConfig, registers, this.memory, alu, agu, stackEngine, gpu);
    }

    public void initialize() { //TODO: move it to interpreter and start from 0x0000
        registers.getProgramCounter().set(MemoryMap.PROGRAM_START);
        registers.getStackPointer().set(MemoryMap.STACK_START);
        registers.getStackSegment().set(MemoryMap.STACK_START);
        registers.getGraphicSegment().set(MemoryMap.DISPLAY_IO_START);
    }


    public Registers getRegisters() {
        return registers;
    }

    public void cycle() {
        controlUnit.fetch();
        controlUnit.decode();
        controlUnit.execute();
    }
}
