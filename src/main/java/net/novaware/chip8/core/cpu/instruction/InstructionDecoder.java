package net.novaware.chip8.core.cpu.instruction;

import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.instruction.InstructionDefinition.notSupported;
import static net.novaware.chip8.core.cpu.register.RegisterModule.CURRENT_INSTRUCTION;
import static net.novaware.chip8.core.cpu.register.RegisterModule.DECODED_INSTRUCTION;

public class InstructionDecoder {

    @Owned
    private final InstructionRegistry registry;

    @Used
    private final WordRegister currentInstruction;

    @Used
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
        final @Unsigned short instruction = currentInstruction.get();
        final InstructionDefinition def = requireNonNull(
                registry.getDefinition(instruction), notSupported(instruction));

        decodedInstruction[0].set(def.getOpCode());
        decodedInstruction[1].set(def.getParam(0, instruction));
        decodedInstruction[2].set(def.getParam(1, instruction));
        decodedInstruction[3].set(def.getParam(2, instruction));
    }
}
