package net.novaware.chip8.core.cpu.instruction;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.register.WordRegister;
import org.checkerframework.checker.signedness.qual.Unsigned;

import static net.novaware.chip8.core.cpu.instruction.InstructionDefinition.notSupported;

public class InstructionDecoder {

    // Contains ---------------------------------

    private InstructionRegistry registry = new InstructionRegistry();

    // Accessible -------------------------------

    private Registers registers;

    public InstructionDecoder(Registers registers) {
        this.registers = registers;
    }

    public void decode() {
        @Unsigned
        final short instruction = registers.getFetchedInstruction().get();
        final InstructionDefinition def = registry.getDefinition(instruction);

        if (def != null) {
            final WordRegister[] decodedInstruction = registers.getDecodedInstruction();
            decodedInstruction[0].set(def.getOpCode());
            decodedInstruction[1].set(def.getParam(0, instruction));
            decodedInstruction[2].set(def.getParam(1, instruction));
            decodedInstruction[3].set(def.getParam(2, instruction));
        } else {
            throw new RuntimeException(notSupported(instruction));
        }
    }
}
