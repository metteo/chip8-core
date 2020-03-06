package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.instruction.InstructionDecoder;
import net.novaware.chip8.core.cpu.instruction.InstructionType;
import net.novaware.chip8.core.cpu.register.RegisterFile;
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
    private final RegisterFile registers;

    @Uses
    private final Memory memory;

    @Uses
    private final LoadStore lsu;

    @Uses
    private final ArithmeticLogic alu;

    @Uses
    private final AddressGeneration agu;

    @Uses
    private final StackEngine stackEngine;

    @Uses
    private final PowerMgmt powerMgmt;

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

            final RegisterFile registers,
            @Named(MMU) final Memory memory,

            final LoadStore lsu,
            final ArithmeticLogic alu,
            final AddressGeneration agu,
            final StackEngine stackEngine,
            final PowerMgmt powerMgmt,
            final Gpu gpu,

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

        final InstructionType instructionType = InstructionType.valueOf(di[0].get());
        final short p1 = di[1].get(), p2 = di[2].get(), p3 = di[3].get();

        int skip = 0;

        final boolean useY = config.isLegacyShift();
        final boolean incrementI = config.isLegacyLoadStore();
        final boolean overflowI = config.isLegacyAddressSum();

        //FIXME: ugly, but compact, figure out how to make it nicer, but still compact and fast

        switch (instructionType) {
            case Ox00E0: gpu.clearScreen(); break;
            case Ox00EE: stackEngine.returnFromSubroutine(); break;
            case Ox0MMM: throw new RuntimeException("system call is unsupported, yet");

            case Ox1MMM: stackEngine.jump(p1); maybeStopClock(p1); break;
            case Ox2MMM: stackEngine.call(p1); break;
            case Ox3XKK: skip = alu.compareValueWithRegister(p1, p2) ? 2 : 0; break;
            case Ox4XKK: skip = alu.compareValueWithRegister(p1, p2) ? 0 : 2; break;
            case Ox5XY0: skip = alu.compareRegisterWithRegister(p1, p2) ? 2 : 0; break;
            case Ox6XKK: alu.loadValueIntoRegister(p1, p2); break;
            case Ox7XKK: alu.addValueToRegister(p1, p2); break;

            case Ox8XY0: alu.copyRegisterIntoRegister(p1, p2); break;
            case Ox8XY1: alu.orRegisterToRegister(p1, p2); break;
            case Ox8XY2: alu.andRegisterToRegister(p1, p2); break;
            case Ox8XY3: alu.xorRegisterToRegister(p1, p2); break;
            case Ox8XY4: alu.addRegisterToRegister(p1, p2); break;
            case Ox8XY5: alu.subtractRegisterFromRegister(p1, p1, p2); break;
            case Ox8XY6: alu.shiftRightRegisterIntoRegister(p1, useY ? p2 : p1); break;
            case Ox8XY7: alu.subtractRegisterFromRegister(p1, p2, p1); break;
            case Ox8XYE: alu.shiftLeftRegisterIntoRegister(p1, useY ? p2 : p1); break;

            case Ox9XY0: skip = alu.compareRegisterWithRegister(p1, p2) ? 0 : 2; break;
            case OxAMMM: agu.loadAddressIntoIndex(p1); break;
            case OxBMMM: stackEngine.jump(p1, ushort(0x0)); break;
            case OxCXKK: alu.andRandomToRegister(p1, p2); break;
            case OxDXYK: gpu.drawSprite(p1, p2, p3); break;

            case OxEX9E: skip = alu.compareInputWithRegister(p1) ? 2 : 0; break;
            case OxEXA1: skip = alu.compareInputWithRegister(p1) ? 0 : 2; break;

            case OxFX07: delayTimer.storeTimerIntoVariable(p1); break;
            // decrement PC to retry in case of wake up from SLEEP instead of HALT
            case OxFX0A: skip = lsu_loadInputIntoRegister(p1) ? 0 : -2; break;
            case OxFX15: delayTimer.loadVariableIntoTimer(p1); break;
            case OxFX18: soundTimer.loadVariableIntoTimer(p1); break;
            case OxFX1E: agu.addRegisterIntoIndex(p1, overflowI); break;
            case OxFX29: gpu.loadFontAddressIntoRegister(p1); break;
            case OxFX33: lsu.storeRegisterInMemoryAsBcd(p1); break;
            case OxFX55: lsu.storeRegistersInMemory(p1, incrementI); break;
            case OxFX65: lsu.loadMemoryIntoRegisters(p1, incrementI); break;

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
    private boolean lsu_loadInputIntoRegister(short x) {
        boolean nonZero = lsu.loadInputIntoRegister(x);

        if (nonZero) {
            LOG.debug(() -> "Got: " + toHexString(registers.getVariable(x).get()));
        } else {
            LOG.debug("Will HALT for input");
            powerMgmt.halt();
        }

        return nonZero;
    }
}