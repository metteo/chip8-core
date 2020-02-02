package net.novaware.chip8.core.cpu.register;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Register file containing all the register references
 *
 */
@Singleton
public class Registers {

    /**
     * General data registers (V0 - VF)
     */
    private final ByteRegister[] variable = new ByteRegister[16];

    private final TribbleRegister index = new TribbleRegister("I");

    /**
     * Also Instruction Pointer (IP)
     */
    private final TribbleRegister programCounter = new TribbleRegister("PC");

    private final TribbleRegister stackSegment = new TribbleRegister("SS");

    private final TribbleRegister stackPointer = new TribbleRegister("SP");

    /**
     * Part of memory (256 bytes) mapped to screen
     */
    private final TribbleRegister graphicSegment = new TribbleRegister("GS");

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

    /**
     * Currently fetched instruction
     */
    private final WordRegister fetchedInstruction = new WordRegister("IR");

    /**
     * Currently decoded instruction with parameters (up to 3)
     */
    private final WordRegister[] decodedInstruction = new WordRegister[4];

    public boolean redraw = true;

    @Inject
    public Registers() {
        for(int i = 0; i < variable.length; ++i) {
            variable[i] = new ByteRegister("V" + i);
        }

        for(int i = 0; i < decodedInstruction.length; ++i) {
            decodedInstruction[i] = new WordRegister("DI" + i);
        }
    }

    public ByteRegister getVariable(int i) {
        return variable[i];
    }

    public ByteRegister getVariable(short i) {
        return getVariable(Short.toUnsignedInt(i));
    }

    public ByteRegister getStatus() {
        return getVariable(0xF);
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

    public WordRegister getKeyState() {
        return keyState;
    }

    public ByteRegister getDelay() {
        return delay;
    }

    public ByteRegister getSound() {
        return sound;
    }

    public WordRegister getFetchedInstruction() {
        return fetchedInstruction;
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
}
