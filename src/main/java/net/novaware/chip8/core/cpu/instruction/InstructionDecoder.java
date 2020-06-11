package net.novaware.chip8.core.cpu.instruction;

import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.checkerframework.checker.signedness.qual.Unsigned;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.instruction.InstructionDefinition.notSupported;
import static net.novaware.chip8.core.cpu.register.RegisterModule.CURRENT_INSTRUCTION;
import static net.novaware.chip8.core.cpu.register.RegisterModule.DECODED_INSTRUCTION;

public class InstructionDecoder {

    @Owned
    private final InstructionRegistry registry;

    private final Map<Short, short[]> cache;

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

        this.cache = new HashMap<>();

        this.currentInstruction = currentInstruction;
        this.decodedInstruction = decodedInstruction;
    }

    public void decode() {
        final @Unsigned short instruction = currentInstruction.get();
        final short[] decoded = cache.computeIfAbsent(instruction, this::decode);

        decodedInstruction[0].set(decoded[0]);
        decodedInstruction[1].set(decoded[1]);
        decodedInstruction[2].set(decoded[2]);
        decodedInstruction[3].set(decoded[3]);
    }

    private short[] decode(Short instruction) {
        final InstructionDefinition def = requireNonNull(
                registry.getDefinition(instruction), notSupported(instruction));

        short[] decoded = new short[4];

        decoded[0] = def.getOpCode();
        decoded[1] = def.getParam(0, instruction);
        decoded[2] = def.getParam(1, instruction);
        decoded[3] = def.getParam(2, instruction);

        return decoded;
    }
}
