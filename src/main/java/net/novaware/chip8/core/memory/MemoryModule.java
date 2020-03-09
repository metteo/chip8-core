package net.novaware.chip8.core.memory;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterModule;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

/**
 * CDP based Chip-8 interpreter divided memory into several segments.
 * Since this interpreter is implemented in java the memory reserved for
 * interpreter ROM and RAM was repurposed for bootloader.
 */
@Module
public class MemoryModule {

    //TODO: memory module should accept memory layout instead of constants below.
    public static final String BOOTLOADER_ROM          = "bootloaderRom";
    public static final short  BOOTLOADER_ROM_START    = 0x0000;
    public static final short  BOOTLOADER_ROM_END      = 0x01FF;
    public static final int    BOOTLOADER_ROM_SIZE     = 512;

    public static final String PROGRAM                 = "program";
    public static final short  PROGRAM_START           = 0x0200;
    public static final short  PROGRAM_END             = 0x0E9F;
    public static final int    PROGRAM_SIZE            = 3232;   // max

    public static final String STACK                   = "stack";
    public static final short  STACK_START             = 0x0EA0;
    public static final short  STACK_END               = 0x0ECF;
    public static final int    STACK_SIZE              = 48;     // max
    public static final int    STACK_FRAME_SIZE        = 2;

    public static final String BOOTLOADER_RAM          = "bootloaderRam";
    public static final short  BOOTLOADER_RAM_START    = 0x0ED0;
    public static final short  BOOTLOADER_RAM_END      = 0x0EEF;
    public static final int    BOOTLOADER_RAM_SIZE     = 32;

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
    @BoardScope
    @Named(BOOTLOADER_ROM)
    static Memory provideBootloaderRom() {
        final PhysicalMemory bootloaderRom = new PhysicalMemory("Bootloader ROM", BOOTLOADER_ROM_SIZE);
        return new ReadOnlyMemory(bootloaderRom);
    }

    @Provides
    @BoardScope
    @Named(PROGRAM)
    static Memory provideProgram() {
        final PhysicalMemory programMemory = new PhysicalMemory("Program", PROGRAM_SIZE);
        return new SplittableMemory(programMemory);
    }

    @Provides
    @BoardScope
    @Named(STACK)
    static Memory provideStack() {
        final int stackFrameCount = STACK_SIZE / STACK_FRAME_SIZE;
        TribbleRegister[] stackRegisters = new TribbleRegister[stackFrameCount];
        for(int i = 0; i < stackFrameCount; ++i) {
            stackRegisters[i] = new TribbleRegister("S" + i);
        }

        return new TribbleRegisterMemory("Stack", stackRegisters);
    }

    @Provides
    @BoardScope
    @Named(BOOTLOADER_RAM)
    static Memory provideBootloaderRam() {
        return new PhysicalMemory("Bootloader RAM", BOOTLOADER_RAM_SIZE);
    }

    @Provides
    @BoardScope
    @Named(VARIABLES)
    static Memory provideVariables(@Named(RegisterModule.VARIABLES) final ByteRegister[] variables) {
        assertArgument(variables.length == VARIABLES_SIZE, () -> "variables.length should be " + VARIABLES_SIZE);

        return new ByteRegisterMemory("Variables", variables);
    }

    @Provides
    @BoardScope
    @Named(DISPLAY_IO)
    static Memory provideDisplayIo() {
        return new PhysicalMemory("Display IO", DISPLAY_IO_SIZE);
    }

    @Provides
    @BoardScope
    @Named(MMU)
    static Memory provideMmu(
        @Named(BOOTLOADER_ROM) final Memory bootloaderRom,
        @Named(PROGRAM) final Memory program,
        @Named(STACK) final Memory stack,
        @Named(BOOTLOADER_RAM) final Memory bootloaderRam,
        @Named(VARIABLES) final Memory variables,
        @Named(DISPLAY_IO) final Memory displayIo
    ) {
        List<MappedMemory.Entry> entries = new ArrayList<>();
        entries.add(new MappedMemory.Entry(BOOTLOADER_ROM_START, BOOTLOADER_ROM_END, bootloaderRom));
        entries.add(new MappedMemory.Entry(PROGRAM_START, PROGRAM_END, program));
        entries.add(new MappedMemory.Entry(STACK_START, STACK_END, stack));
        entries.add(new MappedMemory.Entry(BOOTLOADER_RAM_START, BOOTLOADER_RAM_END, bootloaderRam));
        entries.add(new MappedMemory.Entry(VARIABLES_START, VARIABLES_END, variables));
        entries.add(new MappedMemory.Entry(DISPLAY_IO_START, DISPLAY_IO_END, displayIo));

        return new MappedMemory("MMU", entries);
    }
}
