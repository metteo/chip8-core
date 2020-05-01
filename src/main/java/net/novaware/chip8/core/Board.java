package net.novaware.chip8.core;

import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryModule;
import net.novaware.chip8.core.memory.ReadOnlyMemory;
import net.novaware.chip8.core.memory.SplittableMemory;
import net.novaware.chip8.core.port.AudioPort;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.port.KeyPort;
import net.novaware.chip8.core.port.StoragePort;
import net.novaware.chip8.core.port.impl.*;
import net.novaware.chip8.core.storage.Bootloader;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.register.RegisterFile.GC_IDLE;
import static net.novaware.chip8.core.memory.MemoryModule.*;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

//TODO: public methods should schedule commands to clock generator
//TODO: don't forget to handle exceptions in the Future
@BoardScope
public class Board {

    private static final Logger LOG = LogManager.getLogger();

    public interface Config {

        /**
         * If true, program ROM in RAM is checked for writes to prevent self modification
         */
        boolean isEnforceMemoryRoRwState();

        /**
         * Defaults to 60 Hz
         */
        int getDelayTimerFrequency();

        /**
         * Defaults to 60 Hz
         */
        int getSoundTimerFrequency();

        /**
         * Defaults to 60 Hz
         */
        int getRenderTimerFrequency();

        /**
         * Defaults to 500 Hz
         */
        int getCpuFrequency();
    }

    @Owned
    private final Board.Config config;

    @Owned
    private final Memory mmu;

    @Owned
    private final Cpu cpu;

    @Owned
    private ClockGenerator clock;

    @Used
    private final Memory displayIo;

    @Used
    private final Memory bootloaderRom;

    @Used
    private final Memory program;

    //TODO: class for managing handles

    private volatile ClockGenerator.@Nullable Handle cycleHandle;
    private volatile ClockGenerator.@Nullable Handle delayHandle;
    private volatile ClockGenerator.@Nullable Handle soundHandle;
    private volatile ClockGenerator.@Nullable Handle renderHandle;

    private DisplayPortImpl primaryDisplayPort;
    private DisplayPortImpl secondaryDisplayPort;

    private AudioPortImpl audioPort;

    private KeyPortImpl keyPort;

    private StoragePortImpl storagePort;

    @Inject
    /* package */ Board(
        final Board.Config config,
        @Named(PROGRAM) final Memory program,
        @Named(BOOTLOADER_ROM) final Memory bootloaderRom,
        @Named(DISPLAY_IO) final Memory displayIo,
        @Named(STORAGE_ROM) final Memory storageRom,
        @Named(MMU) final Memory mmu,
        final ClockGenerator clock,
        final Cpu cpu
    ) {
        this.config = config;

        this.program = program;
        this.bootloaderRom = bootloaderRom;
        this.displayIo = displayIo;
        this.mmu = mmu;

        this.clock = clock;
        this.cpu = cpu;

        //TODO: inject those
        primaryDisplayPort = new DisplayPortImpl(cpu.getRegisters().getGraphicChange(), displayIo);
        secondaryDisplayPort = new DisplayPortImpl(cpu.getRegisters().getGraphicChange(), displayIo);
        audioPort = new AudioPortImpl(cpu.getRegisters().getSoundOn());
        keyPort = new KeyPortImpl(cpu.getRegisters().getInput(), cpu.getRegisters().getInputCheck());
        storagePort = new StoragePortImpl((StorageMemory) storageRom);
    }

    public void powerOn() {
        initialize();
        runOnScheduler(Integer.MAX_VALUE);
    }

    private void powerOff0(boolean force) {
        if (cycleHandle != null) {
            cycleHandle.cancel(force);
            cycleHandle = null;
        }

        if (delayHandle != null) {
            delayHandle.cancel(force);
            delayHandle = null;
        }

        if (soundHandle != null) {
            soundHandle.cancel(force);
            soundHandle = null;
        }

        if (renderHandle != null) {
            renderHandle.cancel(false);
            renderHandle = null;
        }

        clock.shutdown();
    }

    public void initialize() {
        LOG.traceEntry();

        final RegisterFile registers = cpu.getRegisters();

        Bootloader bootloader = new Bootloader();
        bootloader.fill(mmu);
        ((ReadOnlyMemory) bootloaderRom).setReadOnly(config::isEnforceMemoryRoRwState); //TODO: add check

        registers.getFontSegment().set(bootloader.getFontAddress());
        registers.getGraphicSegment().set(MemoryModule.DISPLAY_IO_START);
        registers.getStackSegment().set(MemoryModule.STACK_START);

        loadProgram();

        cpu.initialize();

        registers.getOutput().subscribe(out -> {
            int output = out.getAsInt();

            if (output == 0x11) {
                LOG.error("CPU stopped abruptly at " + toHexString(registers.getMemoryAddress().get()));
                powerOff0(false);
                //TODO: report exit code somehow outside (exception handler, outputport?)
            }
        });

        registers.getGraphicChange().subscribe(gcr -> {
            if (gcr.getAsInt() == GC_IDLE) { return; } // prevent recursive loop
            primaryDisplayPort.onGraphicChange();
            secondaryDisplayPort.onGraphicChange();
            gcr.set(GC_IDLE);
        });

        audioPort.attachToRegister();
        keyPort.attachToRegister();

        LOG.traceExit();
    }

    private void loadProgram(){
        //TODO: rewrite loading procedure as copying from ROM into RAM (all: bootloader, boot-128, program from tape)

        SplittableMemory programMemory = (SplittableMemory) program; //TODO: add check
        programMemory.setStrict(false); //disable RO mode

        int sourceAddr = uint(STORAGE_ROM_START);
        for (int addr = 0; addr < PROGRAM_SIZE; ++addr) {
            byte b = mmu.getByte(ushort(sourceAddr));
            program.setByte(ushort(addr), b);
            ++sourceAddr;
        }

        //programMemory.setSplit(data.length); //TODO: reactivate split, get program size from packet?
        programMemory.setStrict(config::isEnforceMemoryRoRwState);
    }

    public void softReset() {
        clock.schedule(() -> softReset0());
    }

    /* package */ void softReset0() {
        cpu.reset();
    }

    public void hardReset() {
        clock.schedule(() -> hardReset0());
    }

    /* package */ void hardReset0() {
        mmu.clear(); //TODO: reload program ROM, clear the rest
        loadProgram();
        cpu.reset();
    }

    public void runOnScheduler(int maxCycles) {
        final boolean countCycles = maxCycles != Integer.MAX_VALUE;
        final AtomicInteger cycles = new AtomicInteger();

        // TODO: handle threading of handle references xD
        delayHandle = clock.schedule(cpu::delayTick, config.getDelayTimerFrequency());
        soundHandle = clock.schedule(cpu::soundTick, config.getSoundTimerFrequency());
        renderHandle = clock.schedule(() -> {
            primaryDisplayPort.tick();
            secondaryDisplayPort.tick();
        }, config.getRenderTimerFrequency());

        //TODO: react to cpu state and control the clock properly
        cycleHandle = clock.schedule(() -> {
            try {
                //TODO: report exceptions back to Board owner
                cpu.cycle();
            } catch(Exception e) {
                LOG.error("Exception during CPU cycle: ", e);
                clock.shutdown(); //TODO: maybe trigger stop clock instead?
            }

            if (countCycles) { // bypass counting
                int currentCycles = cycles.incrementAndGet();

                if (currentCycles >= maxCycles && cycleHandle != null) {
                    LOG.warn("Reached maxCycles: {}", maxCycles);

                    powerOff0(false);
                }
            }
        }, config.getCpuFrequency());
    }

    public void pause() {
        clock.schedule(() -> cpu.sleep());
    }

    public void resume() {
        clock.schedule(() -> cpu.wakeUp());
    }

    public AudioPort getAudioPort() {
        return audioPort;
    }

    public DisplayPort getDisplayPort(final DisplayPort.Type type) {
        requireNonNull(type, "type must not be null");
        switch (type) {
            case SECONDARY:
                return secondaryDisplayPort;
            case PRIMARY:
            default:
                return primaryDisplayPort;
        }
    }

    public KeyPort getKeyPort() {
        return keyPort;
    }

    public StoragePort getStoragePort() {
        return storagePort;
    }

    //TODO: maybe use cpu state register?
    public boolean isRunning() {
        return cycleHandle != null;
    }
}
