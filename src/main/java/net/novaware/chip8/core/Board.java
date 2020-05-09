package net.novaware.chip8.core;

import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGenerator.Handle;
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
import net.novaware.chip8.core.port.impl.AudioPortImpl;
import net.novaware.chip8.core.port.impl.DisplayPortImpl;
import net.novaware.chip8.core.port.impl.KeyPortImpl;
import net.novaware.chip8.core.port.impl.StoragePortImpl;
import net.novaware.chip8.core.storage.Bootloader;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.register.RegisterFile.GC_IDLE;
import static net.novaware.chip8.core.memory.MemoryModule.*;
import static net.novaware.chip8.core.port.impl.PortModule.PRIMARY;
import static net.novaware.chip8.core.port.impl.PortModule.SECONDARY;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

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
    private final Memory bootloaderRom;

    @Used
    private final Memory program;

    @Owned
    private final List<Handle> clockHandles = new ArrayList<>();

    @Owned
    private DisplayPortImpl primaryDisplayPort;

    @Owned
    private DisplayPortImpl secondaryDisplayPort;

    @Owned
    private AudioPortImpl audioPort;

    @Owned
    private KeyPortImpl keyPort;

    @Owned
    private StoragePortImpl storagePort;

    @Owned
    private Consumer<Exception> exceptionHandler = e -> LOG.error("Unexpected exception: ", e);

    @Inject
    /* package */ Board(
        final Board.Config config,
        @Named(PROGRAM) final Memory program,
        @Named(BOOTLOADER_ROM) final Memory bootloaderRom,
        @Named(MMU) final Memory mmu,
        final ClockGenerator clock,
        final Cpu cpu,

        @Named(PRIMARY) final DisplayPortImpl primaryDisplayPort,
        @Named(SECONDARY) final DisplayPortImpl secondaryDisplayPort,
        final AudioPortImpl audioPort,
        final KeyPortImpl keyPort,
        final StoragePortImpl storagePort
    ) {
        this.config = config;

        this.program = program;
        this.bootloaderRom = bootloaderRom;
        this.mmu = mmu;

        this.clock = clock;
        this.cpu = cpu;

        this.primaryDisplayPort = primaryDisplayPort;
        this.secondaryDisplayPort = secondaryDisplayPort;
        this.audioPort = audioPort;
        this.keyPort = keyPort;
        this.storagePort = storagePort;
    }

    private void powerOn0() {
        initialize();
        start();
    }

    private void initialize() {
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
        //TODO: rewrite loading procedure as copying from ROM into RAM (all: bootloader, boot-128, program)

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

    /* package */ void softReset0() {
        cpu.reset();
    }

    /* package */ void hardReset0() {
        mmu.clear(); //TODO: reload program ROM, clear the rest
        loadProgram();
        cpu.reset();
    }

    private void start() {
        Handle delayHandle = clock.schedule(cpu::delayTick, config.getDelayTimerFrequency());
        Handle soundHandle = clock.schedule(cpu::soundTick, config.getSoundTimerFrequency());

        Handle renderHandle = clock.schedule(() -> {
            primaryDisplayPort.tick();
            secondaryDisplayPort.tick();
        }, config.getRenderTimerFrequency());

        //TODO: react to cpu state and control the clock properly
        Handle cycleHandle = clock.schedule(() -> {
            try {
                cpu.cycle();
            } catch(Exception e) {
                exceptionHandler.accept(e);
                cpu.sleep();
            }
        }, config::getCpuFrequency);

        clockHandles.addAll(List.of(cycleHandle, delayHandle, soundHandle, renderHandle));
    }

    private void powerOff0(boolean force) {
        cpu.sleep();

        clockHandles.stream().forEach(h -> h.cancel(force));
        clockHandles.clear();

        clock.shutdown();
    }

    private void scheduleAndHandle(final Runnable target) {
        final Handle handle = clock.schedule(target);

        //TODO: handle exceptions in the Futures returned from clock
    }

    // 1. Connect peripherals -------------------------------------------------

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

    public AudioPort getAudioPort() {
        return audioPort;
    }

    public KeyPort getKeyPort() {
        return keyPort;
    }

    public StoragePort getStoragePort() {
        return storagePort;
    }

    public void setExceptionHandler(Consumer<Exception> exceptionHandler) {
        scheduleAndHandle(() -> setExceptionHandler0(exceptionHandler));
    }

    private void setExceptionHandler0(Consumer<Exception> exceptionHandler) {
        requireNonNull(exceptionHandler, "exceptionHandler must not be null");

        this.exceptionHandler = exceptionHandler;
    }

    // 2. Power ON ------------------------------------------------------------

    public void powerOn() {
        scheduleAndHandle(() -> powerOn0());
    }

    // 3. Pause / Resume when needed ------------------------------------------

    public void pause() {
        scheduleAndHandle(() -> cpu.sleep());
    }

    public void resume() {
        scheduleAndHandle(() -> cpu.wakeUp());
    }

    // 4. Soft reset to restart the program -----------------------------------

    public void softReset() {
        scheduleAndHandle(() -> softReset0());
    }

    // 5. Hard reset to restart the board -------------------------------------

    public void hardReset() {
        scheduleAndHandle(() -> hardReset0());
    }

    // 6. Power OFF to finish and cleanup -------------------------------------

    public void powerOff(final boolean force) {
        scheduleAndHandle(() -> powerOff0(force));
    }
}
