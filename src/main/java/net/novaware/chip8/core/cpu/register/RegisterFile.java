package net.novaware.chip8.core.cpu.register;

import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Register File containing all the register references
 */
@BoardScope
public class RegisterFile {

    public static final byte GC_IDLE = 0x00;
    public static final byte GC_ERASE = 0x01;
    public static final byte GC_NOOP = 0x02;
    public static final byte GC_DRAW = 0x03;
    public static final byte GC_MIX = 0x04;

    public static final byte VF_EMPTY = 0x00;
    public static final byte VF_CARRY = 0x01;
    public static final byte VF_CARRY_I = 0x02;
    public static final byte VF_NO_BORROW= 0x03;
    public static final byte VF_LSB = 0x04;
    public static final byte VF_MSB = 0x05;
    public static final byte VF_COLLISION = 0x06;

    /**
     * Describes the state of CPU
     */
    @Owned
    private final ByteRegister cpuState;

    /**
     * General purpose registers (V0 - VE)
     * Status register (VF)
     */
    @Owned
    private final ByteRegister[] variables;

    /**
     * Type of the value in the status register (VF)
     *
     * @see {@link RegisterFile#VF_EMPTY}
     */
    @Owned
    private final ByteRegister statusType;

    /**
     * Index register use for holding memory address
     * Accessible by the program
     */
    @Owned
    private final TribbleRegister index;

    /**
     * Memory Address Register
     *
     * Holds address of currently executed instruction. Not involved in other
     * memory related operations.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Memory_address_register" >MAR</a>
     */
    @Owned
    private final TribbleRegister memoryAddress;

    /**
     * Also Instruction Pointer (IP)
     *
     * Holds the address of the next instruction.
     */
    @Owned
    private final TribbleRegister programCounter;

    /**
     * Stack segment value marks the start of the stack in memory
     * Does not change in Chip8
     */
    @Owned
    private final TribbleRegister stackSegment;

    /**
     * Points to the top of the stack
     */
    @Owned
    private final TribbleRegister stackPointer;

    /**
     * Start of the memory with font sprites
     */
    @Owned
    private final TribbleRegister fontSegment;

    /**
     * Start of the part of memory (256 bytes) mapped to screen
     */
    @Owned
    private final TribbleRegister graphicSegment;

    /**
     * Contains information what kind of change was applied to the graphics
     *
     * Rendering component can then figure out when to repaint the screen to
     * avoid blinking of sprites during repositioning
     */
    @Owned
    private final ByteRegister graphicChange;

    /**
     * Dedicated Input register (16 bits) mapped to 16 CPU pins
     */
    @Owned
    private final WordRegister input;

    /**
     * Dedicated Output register (16 bits) mapped to 16 CPU pins
     */
    @Owned
    private final WordRegister output;

    /**
     * Delay timer register value
     */
    @Owned
    private ByteRegister delay;

    /**
     * Sound timer register value
     */
    @Owned
    private ByteRegister sound;

    // TODO: figure out a better name, or remove when possible to attach multiple callbacks?
    /**
     * Value 0x1 turns on the sound, 0x0 turns it off
     */
    @Owned
    private ByteRegister soundOn;

    /**
     * Current Instruction Register
     * When instruction is fetched it's stored here.
     */
    @Owned
    private final WordRegister currentInstruction;

    /**
     * Currently decoded instruction with parameters (up to 3)
     */
    @Owned
    private final WordRegister[] decodedInstruction;

    @Inject
    public RegisterFile(
        @Named(CPU_STATE) final ByteRegister cpuState,
        @Named(VARIABLES) final ByteRegister[] variables,
        @Named(STATUS_TYPE) final ByteRegister statusType,
        @Named(INDEX) final TribbleRegister index,
        @Named(MEMORY_ADDRESS) final TribbleRegister memoryAddress,
        @Named(PROGRAM_COUNTER) final TribbleRegister programCounter,
        @Named(STACK_SEGMENT) final TribbleRegister stackSegment,
        @Named(STACK_POINTER) final TribbleRegister stackPointer,
        @Named(FONT_SEGMENT) final TribbleRegister fontSegment,
        @Named(GRAPHIC_SEGMENT) final TribbleRegister graphicSegment,
        @Named(GRAPHIC_CHANGE) final ByteRegister graphicChange,
        @Named(INPUT) final WordRegister input,
        @Named(OUTPUT) final WordRegister output,
        @Named(DELAY) final ByteRegister delay,
        @Named(SOUND) final ByteRegister sound,
        @Named(SOUND_ON) final ByteRegister soundOn,
        @Named(CURRENT_INSTRUCTION) final WordRegister currentInstruction,
        @Named(DECODED_INSTRUCTION) final WordRegister[] decodedInstruction
    ) {
        this.cpuState = cpuState;

        this.variables = variables;

        this.statusType = statusType;
        this.statusType.set(VF_EMPTY);

        this.index = index;
        this.memoryAddress = memoryAddress;
        this.programCounter = programCounter;

        this.stackSegment = stackSegment;
        this.stackPointer = stackPointer;

        this.fontSegment = fontSegment;
        this.graphicSegment = graphicSegment;
        this.graphicChange = graphicChange;

        this.input = input;
        this.output = output;

        this.delay = delay;
        this.sound = sound;
        this.soundOn = soundOn;

        this.currentInstruction = currentInstruction;
        this.decodedInstruction = decodedInstruction;
    }

    public ByteRegister getCpuState() {
        return cpuState;
    }

    public ByteRegister[] getVariables() {
        return variables;
    }

    public ByteRegister getVariable(int i) {
        return variables[i];
    }

    public ByteRegister getVariable(short i) {
        return getVariable(uint(i));
    }

    public static ByteRegister getVariable(ByteRegister[] variables, short i) {
        return variables[uint(i)];
    }

    public static ByteRegister getVariable(ByteRegister[] variables, int i) {
        return variables[i];
    }

    public ByteRegister getStatus() {
        return getVariable(0xF);
    }

    public ByteRegister getStatusType() {
        return statusType;
    }

    public TribbleRegister getIndex() {
        return index;
    }

    public TribbleRegister getMemoryAddress() {
        return memoryAddress;
    }

    public TribbleRegister getProgramCounter() {
        return programCounter;
    }

    public TribbleRegister getStackSegment() {
        return stackSegment;
    }

    public TribbleRegister getStackPointer() {
        return stackPointer;
    }

    public TribbleRegister getFontSegment() {
        return fontSegment;
    }

    public TribbleRegister getGraphicSegment() {
        return graphicSegment;
    }

    public ByteRegister getGraphicChange() {
        return graphicChange;
    }

    public WordRegister getInput() {
        return input;
    }

    public WordRegister getOutput() {
        return output;
    }

    public ByteRegister getDelay() {
        return delay;
    }

    public ByteRegister getSound() {
        return sound;
    }

    public ByteRegister getSoundOn() {
        return soundOn;
    }

    public WordRegister getCurrentInstruction() {
        return currentInstruction;
    }

    public WordRegister[] getDecodedInstruction() {
        return decodedInstruction;
    }
}
