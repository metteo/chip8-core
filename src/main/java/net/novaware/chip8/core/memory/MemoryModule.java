package net.novaware.chip8.core.memory;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterModule;
import net.novaware.chip8.core.cpu.register.TribbleRegister;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

@Module
public class MemoryModule {

    public static final String INTERPRETER_ROM         = "interpreterRom";
    public static final short  INTERPRETER_ROM_START   = 0x0000;
    public static final short  INTERPRETER_ROM_END     = 0x01FF;
    public static final int    INTERPRETER_ROM_SIZE    = 512;

    public static final String PROGRAM                 = "program";
    public static final short  PROGRAM_START           = 0x0200;
    public static final short  PROGRAM_END             = 0x0E9F;
    public static final int    PROGRAM_SIZE            = 3232;   // max

    public static final String STACK                   = "stack";
    public static final short  STACK_START             = 0x0EA0;
    public static final short  STACK_END               = 0x0ECF;
    public static final int    STACK_SIZE              = 48;     // max

    public static final String INTERPRETER_RAM         = "interpreterRam";
    public static final short  INTERPRETER_RAM_START   = 0x0ED0;
    public static final short  INTERPRETER_RAM_END     = 0x0EEF;
    public static final int    INTERPRETER_RAM_SIZE    = 32;

    public static final String VARIABLES               = "variables";
    public static final short  VARIABLES_START         = 0x0EF0;
    public static final short  VARIABLES_END           = 0x0EFF;
    public static final int    VARIABLES_SIZE          = 16;

    public static final String DISPLAY_IO              = "displayIo";
    public static final short  DISPLAY_IO_START        = 0x0F00;
    public static final short  DISPLAY_IO_END          = 0x0FFF;
    public static final int    DISPLAY_IO_SIZE         = 256;

    public static final String MMU                     = "mmu";

    @Provides
    @Singleton
    @Named(INTERPRETER_ROM)
    static Memory provideInterpreterRom() {
        final PhysicalMemory interpreterRom = new PhysicalMemory("Interpreter ROM", INTERPRETER_ROM_SIZE);
        return new ReadOnlyMemory(interpreterRom);
    }

    @Provides
    @Singleton
    @Named(PROGRAM)
    static Memory provideProgram() {
        final PhysicalMemory programMemory = new PhysicalMemory("Program", PROGRAM_SIZE);
        return new SplittableMemory(programMemory);
    }

    @Provides
    @Singleton
    @Named(STACK)
    static Memory provideStack() {
        TribbleRegister[] stackRegisters = new TribbleRegister[STACK_SIZE / 2];
        for(int i = 0; i < STACK_SIZE / 2; ++i) {
            stackRegisters[i] = new TribbleRegister("S" + i);
        }

        return new TribbleRegisterMemory("Stack", stackRegisters);
    }

    @Provides
    @Singleton
    @Named(INTERPRETER_RAM)
    static Memory provideInterpreterRam() {
        return new PhysicalMemory("Interpreter RAM", INTERPRETER_RAM_SIZE);
    }

    @Provides
    @Singleton
    @Named(VARIABLES)
    static Memory provideVariables(@Named(RegisterModule.VARIABLES) final ByteRegister[] variables) {
        assertArgument(variables.length != VARIABLES_SIZE, "variables.length should be " + VARIABLES_SIZE);

        return new ByteRegisterMemory("Variables", variables);
    }

    @Provides
    @Singleton
    @Named(DISPLAY_IO)
    static Memory provideDisplayIo() {
        return new PhysicalMemory("Display IO", DISPLAY_IO_SIZE);
    }

    @Provides
    @Singleton
    @Named(MMU)
    static Memory provideMmu(
            @Named(INTERPRETER_ROM) final Memory interpreterRom,
            @Named(PROGRAM) final Memory program,
            @Named(STACK) final Memory stack,
            @Named(INTERPRETER_RAM) final Memory interpreterRam,
            @Named(VARIABLES) final Memory variables,
            @Named(DISPLAY_IO) final Memory displayIo
    ) {
        List<MappedMemory.Entry> entries = new ArrayList<>();
        entries.add(new MappedMemory.Entry(INTERPRETER_ROM_START, INTERPRETER_ROM_END, interpreterRom));
        entries.add(new MappedMemory.Entry(PROGRAM_START, PROGRAM_END, program));
        entries.add(new MappedMemory.Entry(STACK_START, STACK_END, stack));
        entries.add(new MappedMemory.Entry(INTERPRETER_RAM_START, INTERPRETER_RAM_END, interpreterRam));
        entries.add(new MappedMemory.Entry(VARIABLES_START, VARIABLES_END, variables));
        entries.add(new MappedMemory.Entry(DISPLAY_IO_START, DISPLAY_IO_END, displayIo));

        return new MappedMemory("MMU", entries);
    }
}
