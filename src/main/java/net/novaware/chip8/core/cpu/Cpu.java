package net.novaware.chip8.core.cpu;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.ControlUnit;
import net.novaware.chip8.core.cpu.unit.ProcessingUnit;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Central Processing Unit (CPU)
 */
@Singleton
public class Cpu {

    // Internal parts ---------------------------

    private final Registers registers;

    private final ControlUnit controlUnit;

    private final ProcessingUnit processingUnit;

    // Accessible -------------------------------

    private final Memory memory;

    @Inject
    public Cpu(@Named("cpu") final Memory memory, final Registers registers) {
        this.memory = memory;
        this.registers = registers;

        controlUnit = new ControlUnit(registers, this.memory);
        processingUnit = new ProcessingUnit(registers, this.memory);
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
        processingUnit.execute();
    }
}
