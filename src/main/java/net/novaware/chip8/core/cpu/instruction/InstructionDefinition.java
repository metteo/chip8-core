package net.novaware.chip8.core.cpu.instruction;

import org.checkerframework.checker.signedness.qual.Unsigned;

public interface InstructionDefinition {

    InstructionType getInstructionType();

    /**
     * Verifies if input is applicable to this definition.
     * <p>
     * Callers should use it before calling any other method.
     */
    boolean isRecognized(@Unsigned short instruction);

    @Unsigned
    short getOpCode();

    int getParamCount();

    @Unsigned
    short getParam(int index, @Unsigned short instruction);

    static String notSupported(@Unsigned short instruction) {
        return String.format("instruction 0x%04X is not supported", instruction);
    }
}
