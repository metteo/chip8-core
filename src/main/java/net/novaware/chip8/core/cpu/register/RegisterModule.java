package net.novaware.chip8.core.cpu.register;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class RegisterModule {

    public static final String VARIABLES = "variables";

    public static final String STATUS = "status";
    public static final String STATUS_TYPE = "statusType";

    public static final String INDEX = "index";
    public static final String MEMORY_ADDRESS = "memoryAddress";
    public static final String PROGRAM_COUNTER = "programCounter";

    public static final String STACK_SEGMENT = "stackSegment";
    public static final String STACK_POINTER = "stackPointer";

    public static final String GRAPHIC_SEGMENT = "graphicSegment";
    public static final String GRAPHIC_CHANGE = "graphicChange";

    public static final String KEY_STATE = "keyState";
    public static final String KEY_WAIT = "keyWait";
    public static final String KEY_VALUE = "keyValue";

    public static final String DELAY = "delay";
    public static final String SOUND = "sound";
    public static final String SOUND_ON = "soundOn";

    public static final String CURRENT_INSTRUCTION = "currentInstruction";
    public static final String DECODED_INSTRUCTION = "decodedInstruction";

    @Provides
    @Singleton
    @Named(VARIABLES)
    static ByteRegister[] provideVariables() {
        final ByteRegister[] variables = new ByteRegister[16];

        for(int i = 0; i < variables.length; ++i) {
            variables[i] = new ByteRegister("V" + i);
        }

        return variables;
    }

    @Provides
    @Singleton
    @Named(STATUS)
    static ByteRegister provideStatus(@Named(VARIABLES) final ByteRegister[] variables) {
        return variables[0xF];
    }

    @Provides
    @Singleton
    @Named(STATUS_TYPE)
    static ByteRegister provideStatusType() {
        return new ByteRegister("ST");
    }

    @Provides
    @Singleton
    @Named(INDEX)
    static TribbleRegister provideIndex() {
        return new TribbleRegister("I");
    }

    @Provides
    @Singleton
    @Named(MEMORY_ADDRESS)
    static TribbleRegister provideMemoryAddress() {
        return new TribbleRegister("MAR");
    }

    @Provides
    @Singleton
    @Named(PROGRAM_COUNTER)
    static TribbleRegister provideProgramCounter() {
        return new TribbleRegister("PC");
    }

    @Provides
    @Singleton
    @Named(STACK_SEGMENT)
    static TribbleRegister provideStackSegment() {
        return new TribbleRegister("SS");
    }

    @Provides
    @Singleton
    @Named(STACK_POINTER)
    static TribbleRegister provideStackPointer() {
        return new TribbleRegister("SP");
    }

    @Provides
    @Singleton
    @Named(GRAPHIC_SEGMENT)
    static TribbleRegister provideGraphicSegment() {
        return new TribbleRegister("GS");
    }

    @Provides
    @Singleton
    @Named(GRAPHIC_CHANGE)
    static ByteRegister provideGraphicChange() {
        return new ByteRegister("GC");
    }

    @Provides
    @Singleton
    @Named(KEY_STATE)
    static WordRegister provideKeyState() {
        return new WordRegister("KS");
    }

    @Provides
    @Singleton
    @Named(KEY_WAIT)
    static ByteRegister provideKeyWait() {
        return new ByteRegister("KW");
    }

    @Provides
    @Singleton
    @Named(KEY_VALUE)
    static ByteRegister provideKeyValue() {
        return new ByteRegister("KV");
    }

    @Provides
    @Singleton
    @Named(DELAY)
    static ByteRegister provideDelay() {
        return new ByteRegister("DT");
    }

    @Provides
    @Singleton
    @Named(SOUND)
    static ByteRegister provideSound() {
        return new ByteRegister("ST");
    }

    @Provides
    @Singleton
    @Named(SOUND_ON)
    static ByteRegister provideSoundOn() {
        return new ByteRegister("SO");
    }

    @Provides
    @Singleton
    @Named(CURRENT_INSTRUCTION)
    static WordRegister provideCurrentInstruction() {
        return new WordRegister("CIR");
    }

    @Provides
    @Singleton
    @Named(DECODED_INSTRUCTION)
    static WordRegister[] provideDecodedInstruction() {
        final WordRegister[] decodedInstruction = new WordRegister[4];

        for(int i = 0; i < decodedInstruction.length; ++i) {
            decodedInstruction[i] = new WordRegister("DIR" + i);
        }

        return decodedInstruction;
    }
}
