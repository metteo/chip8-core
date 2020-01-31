package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.instruction.InstructionDecoder;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

/**
 * Control Unit (CU)
 *
 * <br>Operation register + program counter
 */
public class ControlUnit {

    // Contains ---------------------------------

    private InstructionDecoder decoder;

    // Accessible -------------------------------

    private final Registers registers;

    private final Memory memory;

    public ControlUnit(Registers registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;

        this.decoder = new InstructionDecoder(registers);
    }

    public void fetch() {
        final short pc = registers.getProgramCounter().get();

        final short instruction = memory.getWord(pc);

        registers.getFetchedInstruction().set(instruction);
    }

    public void decode() {
        decoder.decode();
    }
}
