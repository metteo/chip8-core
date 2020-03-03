package net.novaware.chip8.core.cpu.instruction;

import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.util.uml.Owns;
import net.novaware.chip8.core.util.uml.Uses;
import org.checkerframework.checker.signedness.qual.Unsigned;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.instruction.InstructionDefinition.notSupported;
import static net.novaware.chip8.core.cpu.register.RegisterModule.CURRENT_INSTRUCTION;
import static net.novaware.chip8.core.cpu.register.RegisterModule.DECODED_INSTRUCTION;

public class InstructionDecoder {

    @Owns
    private final InstructionRegistry registry;

    @Uses
    private final WordRegister currentInstruction;

    @Uses
    private final WordRegister[] decodedInstruction;

    @Inject
    public InstructionDecoder(
            @Named(CURRENT_INSTRUCTION) final WordRegister currentInstruction,
            @Named(DECODED_INSTRUCTION) final WordRegister[] decodedInstruction,
            final InstructionRegistry instructionRegistry
    ) {
        this.registry = instructionRegistry;
        this.currentInstruction = currentInstruction;
        this.decodedInstruction = decodedInstruction;
    }

    public void decode() {
        @Unsigned
        final short instruction = currentInstruction.get();
        final InstructionDefinition def = registry.getDefinition(instruction);

        if (def != null) {
            decodedInstruction[0].set(def.getOpCode());
            decodedInstruction[1].set(def.getParam(0, instruction));
            decodedInstruction[2].set(def.getParam(1, instruction));
            decodedInstruction[3].set(def.getParam(2, instruction));
        } else {
            throw new RuntimeException(notSupported(instruction));
        }
    }
}
