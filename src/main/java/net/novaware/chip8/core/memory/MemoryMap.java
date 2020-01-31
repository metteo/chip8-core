package net.novaware.chip8.core.memory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

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

    public static final short DATA_REGISTERS_START    = 0x0EF0;
    public static final short DATA_REGISTERS_END      = 0x0EFF;
    public static final int   DATA_REGISTERS_SIZE     = 16;

    public static final short DISPLAY_IO_START        = 0x0F00;
    public static final short DISPLAY_IO_END          = 0x0FFF;
    public static final int   DISPLAY_IO_SIZE         = 256;

    private final PhysicalMemory interpreter;
    private final PhysicalMemory program;
    private final PhysicalMemory stack;
    private final PhysicalMemory interpreterRam;
    private final PhysicalMemory dataRegisters;
    private final PhysicalMemory displayIo;

    private final MappedMemory cpuMemory;

    @Inject
    public MemoryMap() {
        interpreter    = new PhysicalMemory("Interpreter ROM", INTERPRETER_SIZE);
        program        = new PhysicalMemory("Program R[OA]M",  PROGRAM_SIZE);
        stack          = new PhysicalMemory("Stack",           STACK_SIZE);
        interpreterRam = new PhysicalMemory("Interpreter RAM", INTERPRETER_RAM_SIZE);
        dataRegisters  = new PhysicalMemory("Data Registers",  DATA_REGISTERS_SIZE);
        displayIo      = new PhysicalMemory("Display IO",      DISPLAY_IO_SIZE);

        List<MappedMemory.Entry> entries = new ArrayList<>();
        entries.add(new MappedMemory.Entry(INTERPRETER_START, INTERPRETER_END, interpreter));
        entries.add(new MappedMemory.Entry(PROGRAM_START, PROGRAM_END, program));
        entries.add(new MappedMemory.Entry(STACK_START, STACK_END, stack));
        entries.add(new MappedMemory.Entry(INTERPRETER_RAM_START, INTERPRETER_RAM_END, interpreterRam));
        entries.add(new MappedMemory.Entry(DATA_REGISTERS_START, DATA_REGISTERS_END, dataRegisters));
        entries.add(new MappedMemory.Entry(DISPLAY_IO_START, DISPLAY_IO_END, displayIo));

        cpuMemory = new MappedMemory(entries);
    }

    public Memory getInterpreter() {
        return interpreter;
    }

    public Memory getProgram() {
        return program;
    }

    public Memory getStack() {
        return stack;
    }

    public Memory getInterpreterRam() {
        return interpreterRam;
    }

    public Memory getDataRegisters() {
        return dataRegisters;
    }

    public Memory getDisplayIo() {
        return displayIo;
    }

    public Memory getCpuMemory() {
        return cpuMemory;
    }
}
