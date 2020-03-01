package net.novaware.chip8.core.cpu.register;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Register file containing all the register references
 *
 */
@Singleton
public class Registers {

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
     * General data registers (V0 - VF)
     */
    private final ByteRegister[] variables = new ByteRegister[16];

    private final ByteRegister statusType = new ByteRegister("ST");

    private final TribbleRegister index = new TribbleRegister("I");

    /**
     * Also Instruction Pointer (IP)
     *
     * Holds the address of the next instruction.
     */
    private final TribbleRegister programCounter = new TribbleRegister("PC");

    /**
     * Memory Address Register
     *
     * Holds address of currently executed instruction. Not involved in other
     * memory related operations.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Memory_address_register" >MAR</a>
     */
    private final TribbleRegister memoryAddress = new TribbleRegister("MAR");

    private final TribbleRegister stackSegment = new TribbleRegister("SS");

    private final TribbleRegister stackPointer = new TribbleRegister("SP");

    /**
     * Part of memory (256 bytes) mapped to screen
     */
    private final TribbleRegister graphicSegment = new TribbleRegister("GS");

    /**
     * Contains information what kind of change was applied to the graphics
     *
     * Rendering component can then figure out when to repaint the screen to
     * avoid blinking of sprites during repositioning
     */
    private final ByteRegister graphicChange = new ByteRegister("GC");

    /**
     * Dedicated registered (2 bytes) mapped to keys (bits 0-F represent keys)
     */
    private final WordRegister keyState = new WordRegister("KS");

    /**
     * Register which when non 0 signals suspension of the execution
     */
    private final ByteRegister keyWait = new ByteRegister("KW");

    /**
     * Value of the key that was last pressed, used by wait for key
     */
    private final ByteRegister keyValue = new ByteRegister("KV");

    /**
     * Delay timer value
     */
    private ByteRegister delay = new ByteRegister("DT");

    /**
     * Sound timer value
     */
    private ByteRegister sound = new ByteRegister("ST");

    // TODO: figure out a better name
    private ByteRegister soundOn = new ByteRegister("SO");

    /**
     * Current Instruction Register
     */
    private final WordRegister currentInstruction = new WordRegister("IR");

    /**
     * Currently decoded instruction with parameters (up to 3)
     */
    private final WordRegister[] decodedInstruction = new WordRegister[4];

    @Inject
    public Registers() {
        for(int i = 0; i < variables.length; ++i) {
            variables[i] = new ByteRegister("V" + i);
        }

        statusType.set(VF_EMPTY);

        for(int i = 0; i < decodedInstruction.length; ++i) {
            decodedInstruction[i] = new WordRegister("DI" + i);
        }
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

    public ByteRegister getStatus() {
        return getVariable(0xF);
    }

    public ByteRegister getStatusType() {
        return statusType;
    }

    public TribbleRegister getIndex() {
        return index;
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

    public TribbleRegister getGraphicSegment() {
        return graphicSegment;
    }

    public ByteRegister getGraphicChange() {
        return graphicChange;
    }

    public WordRegister getKeyState() {
        return keyState;
    }

    public ByteRegister getDelay() {
        return delay;
    }

    public ByteRegister getSound() {
        return sound;
    }

    public WordRegister getCurrentInstruction() {
        return currentInstruction;
    }

    public WordRegister[] getDecodedInstruction() {
        return decodedInstruction;
    }

    public ByteRegister getKeyWait() {
        return keyWait;
    }

    public ByteRegister getKeyValue() {
        return keyValue;
    }

    public ByteRegister getSoundOn() {
        return soundOn;
    }

    public TribbleRegister getMemoryAddress() {
        return memoryAddress;
    }
}
