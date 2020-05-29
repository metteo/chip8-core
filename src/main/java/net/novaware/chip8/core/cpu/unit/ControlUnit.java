package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.instruction.InstructionDecoder;
import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Arrays.stream;
import static net.novaware.chip8.core.cpu.unit.UnitModule.DELAY;
import static net.novaware.chip8.core.cpu.unit.UnitModule.SOUND;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * Control Unit (CU)
 */
public class ControlUnit implements Unit {

    private static final Logger LOG = LogManager.getLogger();

    public interface Config {

        /**
         * If true, uses Y instead of X as source during shifting
         */
        boolean isLegacyShift();

        /**
         * If true, increments I during load and store operations
         */
        boolean isLegacyLoadStore();
    }
    @Owned
    private final Config config;

    @Owned
    private final InstructionDecoder decoder;

    @Used
    private final RegisterFile registers;

    @Used
    private final Memory memory;

    @Used
    private final LoadStore lsu;

    @Used
    private final ArithmeticLogic alu;

    @Used
    private final AddressGen agu;

    @Used
    private final StackEngine stackEngine;

    @Used
    private final PowerMgmt powerMgmt;

    @Used
    private final Gpu gpu;

    @Used
    private final NativeUnit nativeUnit;

    @Used
    private final Timer delayTimer;

    @Used
    private final Timer soundTimer;

    @Inject
    public ControlUnit(
        final Config config,
        final InstructionDecoder decoder,

        final RegisterFile registers,
        @Named(MMU) final Memory memory,

        final LoadStore lsu,
        final ArithmeticLogic alu,
        final AddressGen agu,
        final StackEngine stackEngine,
        final PowerMgmt powerMgmt,
        final Gpu gpu,
        final NativeUnit nativeUnit,

        @Named(DELAY) final Timer delayTimer,
        @Named(SOUND) final Timer soundTimer
    ) {
        this.config = config;
        this.decoder = decoder;

        this.registers = registers;
        this.memory = memory;

        this.lsu = lsu;
        this.alu = alu;
        this.agu = agu;
        this.stackEngine = stackEngine;
        this.powerMgmt = powerMgmt;
        this.gpu = gpu;
        this.nativeUnit = nativeUnit;

        this.delayTimer = delayTimer;
        this.soundTimer = soundTimer;
    }

    @Override
    public void initialize() {
        zeroOutRegisters();
    }

    @Override
    public void reset() {
        zeroOutRegisters();
    }

    private void zeroOutRegisters() {
        registers.getMemoryAddress().set(0);
        registers.getProgramCounter().set(0); //TODO: set it to code segment (0x200 after boot128 soft reset)
        registers.getCurrentInstruction().set(0);
        stream(registers.getDecodedInstruction()).forEach(wr -> wr.set(0));
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

    private InstructionType toInstructionType(final short instruction) {
        final InstructionType instructionType = InstructionType.valueOf(instruction);

        if (instructionType == null) {
            throw new RuntimeException("Unknown instruction: " + toHexString(instruction));
        }

        return instructionType;
    }

    public void execute() {
        final WordRegister[] di = registers.getDecodedInstruction();

        final InstructionType instructionType = toInstructionType(di[0].get());
        final short p1 = di[1].get(), p2 = di[2].get(), p3 = di[3].get();

        int skip = 0;

        final boolean useY = config.isLegacyShift();
        final boolean incrementI = config.isLegacyLoadStore();

        //FIXME: ugly, but compact, figure out how to make it nicer, but still compact and fast

        switch (instructionType) {
            case Ox00E0: gpu.clearScreen(); break;
            case Ox00EE: stackEngine.returnFromRoutine(); break;
            case Ox0MMM: nativeUnit.callMls(p1); break;
            case Ox1MMM: stackEngine.jump(p1); maybeStopClock(p1); break;
            case Ox2MMM: stackEngine.callRoutine(p1); break;
            case Ox3XKK: skip = alu.compareVariableWithValue(p1, p2) ? 2 : 0; break;
            case Ox4XKK: skip = alu.compareVariableWithValue(p1, p2) ? 0 : 2; break;
            case Ox5XY0: skip = alu.compareVariableWithVariable(p1, p2) ? 2 : 0; break;
            case Ox6XKK: alu.loadVariableWithValue(p1, p2); break;
            case Ox7XKK: alu.sumVariableWithValue(p1, p2); break;

            case Ox8XY0: alu.copyVariableIntoVariable(p1, p2); break;
            case Ox8XY1: alu.orVariableWithVariable(p1, p2); break;
            case Ox8XY2: alu.andVariableWithVariable(p1, p2); break;
            case Ox8XY3: alu.xorVariableWithVariable(p1, p2); break;
            case Ox8XY4: alu.sumVariableWithVariable(p1, p2); break;
            case Ox8XY5: alu.subtractVariableFromVariable(p1, p1, p2); break;
            case Ox8XY6: alu.shiftRightVariableIntoVariable(p1, useY ? p2 : p1); break;
            case Ox8XY7: alu.subtractVariableFromVariable(p1, p2, p1); break;
            case Ox8XYE: alu.shiftLeftVariableIntoVariable(p1, useY ? p2 : p1); break;

            case Ox9XY0: skip = alu.compareVariableWithVariable(p1, p2) ? 0 : 2; break;
            case OxAMMM: agu.loadIndexWithAddress(p1); break;
            case OxBMMM: stackEngine.jump(p1, USHORT_0); break;
            case OxCXKK: alu.andVariableWithRandom(p1, p2); break;
            case OxDXYK: gpu.drawSprite(p1, p2, p3); break;

            case OxEX9E: skip = alu.compareInputWithVariable(p1) ? 2 : 0; break;
            case OxEXA1: skip = alu.compareInputWithVariable(p1) ? 0 : 2; break;

            case OxFX07: delayTimer.storeTimerIntoVariable(p1); break;
            // decrement PC to retry in case of wake up from SLEEP instead of HALT
            case OxFX0A: skip = lsu_storeInputIntoVariable(p1) ? 0 : -2; break;
            case OxFX15: delayTimer.loadTimerWithVariable(p1); break;
            case OxFX18: soundTimer.loadTimerWithVariable(p1); break;
            case OxFX1E: agu.sumIndexWithVariable(p1); break;
            case OxFX29: gpu.loadFontAddressIntoRegister(p1); break;
            case OxFX33: lsu.loadMemoryWithBcdVariable(p1); break;
            case OxFX55: lsu.loadMemoryWithVariables(p1, incrementI); break;
            case OxFX65: lsu.storeMemoryIntoVariables(p1, incrementI); break;

            default: throw new RuntimeException("Unknown instruction: " + di[0].get());
        }

        registers.getProgramCounter().increment(skip);

        LOG.trace(() -> toHexString(registers.getMemoryAddress().get()) +
                ": " + toHexString(registers.getCurrentInstruction().get()) +
                " -> " + toHexString(registers.getProgramCounter().get()));
    }

    private void maybeStopClock(final short destination) {
        int from = registers.getMemoryAddress().getAsInt();
        int to = uint(destination);

        if (from == to) {
            // prevent infinite jumps, like on IBM logo ROM
            powerMgmt.stopClock();
        }
    }

    /**
     * @return true if input register is non-0
     */
    private boolean lsu_storeInputIntoVariable(short x) {
        boolean nonZero = lsu.storeInputIntoVariable(x);

        if (nonZero) {
            LOG.debug(() -> "Got: " + toHexString(registers.getVariable(x).get()));
        } else {
            LOG.debug("Will HALT for input");
            powerMgmt.halt();
        }

        return nonZero;
    }
}