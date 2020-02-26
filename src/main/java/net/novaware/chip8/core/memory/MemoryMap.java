package net.novaware.chip8.core.memory;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.TribbleRegister;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * Chip8 Memory Map (4096 bytes)
 */
@Singleton
public class MemoryMap {

    public static final short INTERPRETER_START       = 0x0000;
    public static final short INTERPRETER_END         = 0x01FF;
    public static final int   INTERPRETER_SIZE        = 512;

    public static final short PROGRAM_START           = 0x0200;
    public static final short PROGRAM_END             = 0x0E9F;
    public static final int   PROGRAM_SIZE            = 3232;   // max

    public static final short STACK_START             = 0x0EA0;
    public static final short STACK_END               = 0x0ECF;
    public static final int   STACK_SIZE              = 48;     // max

    public static final short INTERPRETER_RAM_START   = 0x0ED0;
    public static final short INTERPRETER_RAM_END     = 0x0EEF;
    public static final int   INTERPRETER_RAM_SIZE    = 32;

    public static final short VARIABLES_START         = 0x0EF0;
    public static final short VARIABLES_END           = 0x0EFF;
    public static final int   VARIABLES_SIZE          = 16;

    public static final short DISPLAY_IO_START        = 0x0F00;
    public static final short DISPLAY_IO_END          = 0x0FFF;
    public static final int   DISPLAY_IO_SIZE         = 256;

    private final ReadOnlyMemory interpreter;
    private final SplittableMemory program;
    private final TribbleRegisterMemory stack;
    private final PhysicalMemory interpreterRam;
    private final ByteRegisterMemory variables;
    private final PhysicalMemory displayIo;

    private final MappedMemory cpuMemory;

    @Inject
    public MemoryMap(@Named("variables") ByteRegister[] variables) {
        assert variables.length == VARIABLES_SIZE : "variables.length should be " + VARIABLES_SIZE;

        TribbleRegister[] stackRegisters = new TribbleRegister[STACK_SIZE / 2];
        for(int i = 0; i < STACK_SIZE / 2; ++i) {
            stackRegisters[i] = new TribbleRegister("S" + i);
        }


        final PhysicalMemory interpreterRom = new PhysicalMemory("Interpreter ROM", INTERPRETER_SIZE);
        interpreter = new ReadOnlyMemory(interpreterRom);

        final PhysicalMemory programMemory =
                         new PhysicalMemory("Program",  PROGRAM_SIZE);
        program        = new SplittableMemory(programMemory);

        stack          = new TribbleRegisterMemory("Stack",    stackRegisters);
        interpreterRam = new PhysicalMemory("Interpreter RAM", INTERPRETER_RAM_SIZE);
        this.variables = new ByteRegisterMemory("Variables",   variables);
        displayIo      = new PhysicalMemory("Display IO",      DISPLAY_IO_SIZE);

        List<MappedMemory.Entry> entries = new ArrayList<>();
        entries.add(new MappedMemory.Entry(INTERPRETER_START, INTERPRETER_END, interpreter));
        entries.add(new MappedMemory.Entry(PROGRAM_START, PROGRAM_END, program));
        entries.add(new MappedMemory.Entry(STACK_START, STACK_END, stack));
        entries.add(new MappedMemory.Entry(INTERPRETER_RAM_START, INTERPRETER_RAM_END, interpreterRam));
        entries.add(new MappedMemory.Entry(VARIABLES_START, VARIABLES_END, this.variables));
        entries.add(new MappedMemory.Entry(DISPLAY_IO_START, DISPLAY_IO_END, displayIo));

        cpuMemory = new MappedMemory("CPU", entries);
    }

    public void clear() {
        //TODO: implement and test properly
        //TODO: figure out what to do with roms which write to ROM area (reload the rom?)
        for (int i = 0; i < displayIo.getSize(); ++i) {
            displayIo.setByte(ushort(i), ubyte(0));
        }
    }

    public ReadOnlyMemory getInterpreter() {
        return interpreter;
    }

    public SplittableMemory getProgram() {
        return program;
    }

    public TribbleRegisterMemory getStack() {
        return stack;
    }

    public PhysicalMemory getInterpreterRam() {
        return interpreterRam;
    }

    public ByteRegisterMemory getVariables() {
        return variables;
    }

    public PhysicalMemory getDisplayIo() {
        return displayIo;
    }

    public MappedMemory getCpuMemory() {
        return cpuMemory;
    }
}
