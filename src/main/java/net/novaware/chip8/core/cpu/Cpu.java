package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.*;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Random;

/**
 * Central Processing Unit (CPU)
 */
@Singleton
public class Cpu {

    // Contains ---------------------------------

    private final Registers registers;

    private final ControlUnit controlUnit;

    private final ArithmeticLogic alu;

    private final AddressGeneration agu;

    private final StackEngine stackEngine;

    private final GraphicsProcessing gpu;

    // Accessible -------------------------------

    private final Memory memory;

    @Inject
    public Cpu(@Named("cpu") final Memory memory, final Registers registers) {
        this.memory = memory;
        this.registers = registers;

        alu = new ArithmeticLogic(new Random()::nextInt, registers, memory);
        agu = new AddressGeneration(registers, memory);
        stackEngine = new StackEngine(registers, memory);
        gpu = new GraphicsProcessing(registers, memory);

        controlUnit = new ControlUnit(registers, this.memory, alu, agu, stackEngine, gpu);
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
