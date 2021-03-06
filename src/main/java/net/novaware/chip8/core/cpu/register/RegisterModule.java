package net.novaware.chip8.core.cpu.register;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Named;

@Module
public class RegisterModule {

    public static final String CPU_STATE = "cpuState";

    public static final String VARIABLES = "variables";
    public static final int VARIABLES_SIZE = 16;


    public static final String STATUS = "status";
    public static final String STATUS_TYPE = "statusType";

    public static final String INDEX = "index";
    public static final String MEMORY_ADDRESS = "memoryAddress";
    public static final String PROGRAM_COUNTER = "programCounter";

    public static final String STACK_SEGMENT = "stackSegment";
    public static final String STACK_POINTER = "stackPointer";

    public static final String FONT_SEGMENT = "fontSegment";
    public static final String GRAPHIC_SEGMENT = "graphicSegment";
    public static final String GRAPHIC_CHANGE = "graphicChange";

    public static final String INPUT = "input";
    public static final String INPUT_CHECK = "inputCheck";

    public static final String OUTPUT = "output";

    public static final String STORAGE = "storage";

    public static final String DELAY = "delay";
    public static final String SOUND = "sound";
    public static final String SOUND_ON = "soundOn";

    public static final String CURRENT_INSTRUCTION = "currentInstruction";
    public static final String DECODED_INSTRUCTION = "decodedInstruction";

    @Provides
    @BoardScope
    @Named(CPU_STATE)
    static ByteRegister provideCpuState() {
        // not CS because it's reserved for Code Segment
        return new ByteRegister("PS"); // Processor State
    }

    @Provides
    @BoardScope
    @Named(VARIABLES)
    static ByteRegister[] provideVariables() {
        final ByteRegister[] variables = new ByteRegister[VARIABLES_SIZE];

        for(int i = 0; i < variables.length; ++i) {
            variables[i] = new ByteRegister("V" + i);
        }

        return variables;
    }

    @Provides
    @BoardScope
    @Named(STATUS)
    static ByteRegister provideStatus(@Named(VARIABLES) final ByteRegister[] variables) {
        return variables[0xF];
    }

    @Provides
    @BoardScope
    @Named(STATUS_TYPE)
    static ByteRegister provideStatusType() {
        return new ByteRegister("ST");
    }

    @Provides
    @BoardScope
    @Named(INDEX)
    static WordRegister provideIndex() {
        return new WordRegister("I");
    }

    @Provides
    @BoardScope
    @Named(MEMORY_ADDRESS)
    static TribbleRegister provideMemoryAddress() {
        return new TribbleRegister("MAR");
    }

    @Provides
    @BoardScope
    @Named(PROGRAM_COUNTER)
    static TribbleRegister provideProgramCounter() {
        return new TribbleRegister("PC");
    }

    @Provides
    @BoardScope
    @Named(STACK_SEGMENT)
    static TribbleRegister provideStackSegment() {
        return new TribbleRegister("SS");
    }

    @Provides
    @BoardScope
    @Named(STACK_POINTER)
    static TribbleRegister provideStackPointer() {
        return new TribbleRegister("SP");
    }

    @Provides
    @BoardScope
    @Named(FONT_SEGMENT)
    static WordRegister provideFontSegment() {
        return new WordRegister("FS");
    }

    @Provides
    @BoardScope
    @Named(GRAPHIC_SEGMENT)
    static TribbleRegister provideGraphicSegment() {
        return new TribbleRegister("GS");
    }

    @Provides
    @BoardScope
    @Named(GRAPHIC_CHANGE)
    static ByteRegister provideGraphicChange() {
        // GC register recursion is controlled by display related code
        return new ByteRegister("GC", false);
    }

    @Provides
    @BoardScope
    @Named(INPUT)
    static WordRegister provideInput() {
        return new WordRegister("IN");
    }

    @Provides
    @BoardScope
    @Named(INPUT_CHECK)
    static ByteRegister provideInputCheck() {
        return new ByteRegister("IC");
    }

    @Provides
    @BoardScope
    @Named(OUTPUT)
    static WordRegister provideOutput() {
        return new WordRegister("OUT");
    }

    @Provides
    @BoardScope
    @Named(STORAGE)
    static TribbleRegister provideStorage() {
        return new TribbleRegister("STRG");
    }

    @Provides
    @BoardScope
    @Named(DELAY)
    static ByteRegister provideDelay() {
        return new ByteRegister("DT");
    }

    @Provides
    @BoardScope
    @Named(SOUND)
    static ByteRegister provideSound() {
        return new ByteRegister("ST");
    }

    @Provides
    @BoardScope
    @Named(SOUND_ON)
    static ByteRegister provideSoundOn() {
        return new ByteRegister("SO");
    }

    @Provides
    @BoardScope
    @Named(CURRENT_INSTRUCTION)
    static WordRegister provideCurrentInstruction() {
        return new WordRegister("CIR");
    }

    @Provides
    @BoardScope
    @Named(DECODED_INSTRUCTION)
    static WordRegister[] provideDecodedInstruction() {
        final WordRegister[] decodedInstruction = new WordRegister[4];

        for(int i = 0; i < decodedInstruction.length; ++i) {
            decodedInstruction[i] = new WordRegister("DIR" + i);
        }

        return decodedInstruction;
    }
}
