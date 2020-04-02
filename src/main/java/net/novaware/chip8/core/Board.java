package net.novaware.chip8.core;

import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.config.CoreConfig;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.memory.*;
import net.novaware.chip8.core.port.AudioPort;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.port.KeyPort;
import net.novaware.chip8.core.port.StoragePort;
import net.novaware.chip8.core.port.impl.AudioPortImpl;
import net.novaware.chip8.core.port.impl.DisplayPortImpl;
import net.novaware.chip8.core.port.impl.KeyPortImpl;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.register.RegisterFile.GC_IDLE;
import static net.novaware.chip8.core.memory.MemoryModule.*;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.USHORT_0;

//TODO: public methods should schedule commands to clock generator
//TODO: don't forget to handle exceptions in the Future
@BoardScope
public class Board {

    private static final Logger LOG = LogManager.getLogger();

    public interface Config {
        boolean isEnforceMemoryRoRwState();

        int getDelayTimerFrequency();

        int getSoundTimerFrequency();

        int getRenderTimerFrequency();

        int getCpuFrequency();
    }

    @Owned
    private final CoreConfig config;

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

    //TODO: refactor out into own class, use original cosmac vip rom addressing
    private StoragePort storagePort = new StoragePort() {
        @Override
        public void attachSource(Supplier<byte[]> source) {
            programSupplier = source;
        }

        @Override
        public void attachDestination(Consumer<byte[]> callback) {
            throw new UnsupportedOperationException("unimplemented");
        }
    };

    private Supplier<byte[]> programSupplier = () -> new byte[0];

    @Inject
    /* package */ Board(
        final CoreConfig config,
        @Named(PROGRAM) final Memory program,
        @Named(BOOTLOADER_ROM) final Memory bootloaderRom,
        @Named(DISPLAY_IO) final Memory displayIo,
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

        primaryDisplayPort = new DisplayPortImpl(cpu.getRegisters().getGraphicChange(), displayIo);
        secondaryDisplayPort = new DisplayPortImpl(cpu.getRegisters().getGraphicChange(), displayIo);
        audioPort = new AudioPortImpl(cpu.getRegisters().getSoundOn());
        keyPort = new KeyPortImpl(cpu.getRegisters().getInput(), cpu.getRegisters().getInputCheck());
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

        registers.getOutput().setCallback(out -> {
            int output = out.getAsInt();

            if (output == 0x11) {
                LOG.error("CPU stopped abruptly at " + toHexString(registers.getMemoryAddress().get()));
                powerOff0(false);
                //TODO: report this somehow outside (exception handler, outputport?)
            }
        });

        registers.getGraphicChange().setCallback(gcr -> {
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
        final byte[] data = programSupplier.get();

        SplittableMemory programMemory = (SplittableMemory) program; //TODO: add check
        programMemory.setStrict(false); //disable RO mode
        program.setBytes(USHORT_0, data, data.length);
        programMemory.setSplit(data.length);
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

    public boolean isRunning() {
        return cycleHandle != null;
    }
}
