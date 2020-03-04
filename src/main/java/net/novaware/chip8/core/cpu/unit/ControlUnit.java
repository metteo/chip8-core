package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.instruction.InstructionDecoder;
import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.uml.Owns;
import net.novaware.chip8.core.util.uml.Uses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.unit.UnitModule.DELAY;
import static net.novaware.chip8.core.cpu.unit.UnitModule.SOUND;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * Control Unit (CU)
 */
public class ControlUnit {

    private static final Logger LOG = LogManager.getLogger();

    public interface Config {
        boolean isLegacyShift();

        boolean isLegacyLoadStore();

        boolean isLegacyAddressSum();
    }

    @Owns
    private final Config config;

    @Owns
    private final InstructionDecoder decoder;

    @Uses
    private final Registers registers;

    @Uses
    private final Memory memory;

    @Uses
    private final ArithmeticLogic alu;

    @Uses
    private final AddressGeneration agu;

    @Uses
    private final StackEngine stackEngine;

    @Uses
    private final Gpu gpu;

    @Uses
    private final Timer delayTimer;

    @Uses
    private final Timer soundTimer;

    @Inject
    public ControlUnit(
            final Config config,
            final InstructionDecoder decoder,

            final Registers registers,
            @Named(MMU) final Memory memory,

            final ArithmeticLogic alu,
            final AddressGeneration agu,
            final StackEngine stackEngine,
            final Gpu gpu,

            @Named(DELAY) final Timer delayTimer,
            @Named(SOUND) final Timer soundTimer
    ) {
        this.config = config;
        this.decoder = decoder;

        this.registers = registers;
        this.memory = memory;

        this.alu = alu;
        this.agu = agu;
        this.stackEngine = stackEngine;
        this.gpu = gpu;

        this.delayTimer = delayTimer;
        this.soundTimer = soundTimer;
    }

    public void fetch() {
        final short pc = registers.getProgramCounter().get();
        registers.getMemoryAddress().set(pc);

        final short ma = registers.getMemoryAddress().get();
        final short instruction = memory.getWord(ma);
        registers.getCurrentInstruction().set(instruction);

        registers.getProgramCounter().increment(2);
    }

    public void decode() {
        decoder.decode();
    }

    public void execute() {
        final WordRegister[] di = registers.getDecodedInstruction();
        final TribbleRegister pc = registers.getProgramCounter();

        final InstructionType instructionType = InstructionType.valueOf(di[0].get());

        int skip = 0;

        final boolean useY = config.isLegacyShift();
        final boolean incrementI = config.isLegacyLoadStore();
        final boolean overflowI = config.isLegacyAddressSum();

        //FIXME: ugly, but compact, figure out how to make it nicer, but still compact and fast

        switch (instructionType) {
            case Ox00E0: gpu.clearScreen(); break;
            case Ox00EE: stackEngine.returnFromSubroutine(); break;
            case Ox0MMM: throw new RuntimeException("system call is unsupported, yet");

            case Ox1MMM: stackEngine.jump(di[1].get()); break;
            case Ox2MMM: stackEngine.call(di[1].get()); break;
            case Ox3XKK: skip = alu.compareValueWithRegister(di[1].get(), di[2].get()) ? 2 : 0; break;
            case Ox4XKK: skip = alu.compareValueWithRegister(di[1].get(), di[2].get()) ? 0 : 2; break;
            case Ox5XY0: skip = alu.compareRegisterWithRegister(di[1].get(), di[2].get()) ? 2 : 0; break;
            case Ox6XKK: alu.loadValueIntoRegister(di[1].get(), di[2].get()); break;
            case Ox7XKK: alu.addValueToRegister(di[1].get(), di[2].get()); break;

            case Ox8XY0: alu.copyRegisterIntoRegister(di[1].get(), di[2].get()); break;
            case Ox8XY1: alu.orRegisterToRegister(di[1].get(), di[2].get()); break;
            case Ox8XY2: alu.andRegisterToRegister(di[1].get(), di[2].get()); break;
            case Ox8XY3: alu.xorRegisterToRegister(di[1].get(), di[2].get()); break;
            case Ox8XY4: alu.addRegisterToRegister(di[1].get(), di[2].get()); break;
            case Ox8XY5: alu.subtractRegisterFromRegister(di[1].get(), di[1].get(), di[2].get()); break;
            case Ox8XY6: alu.shiftRightRegisterIntoRegister(di[1].get(), useY ? di[2].get() : di[1].get()); break;
            case Ox8XY7: alu.subtractRegisterFromRegister(di[1].get(), di[2].get(), di[1].get()); break;
            case Ox8XYE: alu.shiftLeftRegisterIntoRegister(di[1].get(), useY ? di[2].get() : di[1].get()); break;

            case Ox9XY0: skip = alu.compareRegisterWithRegister(di[1].get(), di[2].get()) ? 0 : 2; break;
            case OxAMMM: agu.loadAddressIntoIndex(di[1].get()); break;
            case OxBMMM: stackEngine.jump(di[1].get(), ushort(0x0)); break;
            case OxCXKK: alu.andRandomToRegister(di[1].get(), di[2].get()); break;
            case OxDXYK: gpu.drawSprite(di[1].get(), di[2].get(), di[3].get()); break;

            case OxEX9E: skip = compareKeyStateWithRegister(di[1].get()) ? 2 : 0; break;
            case OxEXA1: skip = compareKeyStateWithRegister(di[1].get()) ? 0 : 2; break;

            case OxFX07: delayTimer.storeTimerIntoVariable(di[1].get()); break;
            case OxFX0A: skip = checkIfKeyPressed(di[1].get()) ? 0 : -2; break;
            case OxFX15: delayTimer.loadVariableIntoTimer(di[1].get()); break;
            case OxFX18: soundTimer.loadVariableIntoTimer(di[1].get()); break;
            case OxFX1E: agu.addRegisterIntoIndex(di[1].get(), overflowI); break;
            case OxFX29: loadFontAddressIntoRegister(di[1].get()); break;
            case OxFX33: alu.bcdRegisterToMemory(di[1].get()); break;
            case OxFX55: loadRegistersIntoMemory(di[1].get(), incrementI); break;
            case OxFX65: loadMemoryIntoRegisters(di[1].get(), incrementI); break;

            default: throw new RuntimeException("Unknown instruction: " + di[0].get());
        }

        pc.increment(skip);

        LOG.trace(() -> toHexString(registers.getMemoryAddress().get()) +
                ": " + toHexString(registers.getCurrentInstruction().get()) +
                " -> " + toHexString(pc.get()));
    }

    /**
     * @return true if key is pressed
     */
    private boolean checkIfKeyPressed(short x) {
        if (registers.getKeyState().getAsInt() > 0) { //some keys are pressed
            registers.getVariable(x).set(registers.getKeyValue().get());
            return true;
        } else {
            return false;
        }
    }

    private void loadFontAddressIntoRegister(final short x) {
        final int xValue = registers.getVariable(x).getAsInt();
        final int fontSegment = registers.getFontSegment().getAsInt();

        final int fontAddress = fontSegment +  xValue * 5;

        registers.getIndex().set(fontAddress);
    }

    /**
     * @return true if key was pressed
     */
    private boolean compareKeyStateWithRegister(final short x) { //TODO: refactor more
        short keys = registers.getKeyState().get();

        byte key = registers.getVariable(x).get();

        LOG.debug(() -> "Key " + toHexString(key) + " checked");

        int keyMask = 1 << key;

        boolean keyNotPressed = (keys & keyMask) == 0;

        return !keyNotPressed;
    }

    //TODO: special unit for handling transfers between memory and registers? registers are memory mapped BTW...

    private void loadMemoryIntoRegisters(final short x, final boolean incrementI) {
        int xIndex = uint(x);
        int iValue = registers.getIndex().getAsInt();


        for (int i = 0; i <= xIndex; ++i, ++iValue) {
            final byte data = memory.getByte(ushort(iValue));
            registers.getVariable(i).set(data);
        }

        if (incrementI) {
            registers.getIndex().set(iValue);
        }
    }

    private void loadRegistersIntoMemory(final short x, final boolean incrementI) {
        int xIndex = uint(x);
        int iValue = registers.getIndex().getAsInt();

        for (int i = 0; i <= xIndex; ++i, ++iValue) {
            final byte data = registers.getVariable(i).get();
            memory.setByte(ushort(iValue), data);
        }

        if (incrementI) {
            registers.getIndex().set(iValue);
        }
    }
}