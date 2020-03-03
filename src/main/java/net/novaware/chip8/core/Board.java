package net.novaware.chip8.core;

import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.*;
import net.novaware.chip8.core.port.AudioPort;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.port.KeyPort;
import net.novaware.chip8.core.port.StoragePort;
import net.novaware.chip8.core.util.uml.Owns;
import net.novaware.chip8.core.util.uml.Uses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.novaware.chip8.core.cpu.register.Registers.GC_IDLE;
import static net.novaware.chip8.core.memory.MemoryModule.*;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

//TODO: public methods should schedule commands to clock generator
@Singleton
public class Board {

    private static final Logger LOG = LogManager.getLogger();

    @Owns
    private final BoardConfig config;

    @Owns
    private final Memory mmu;

    @Owns
    private final Cpu cpu;

    @Owns
    private ClockGenerator clock;

    @Uses
    private final Memory interpreterRom;

    @Uses
    private final Memory program;

    //TODO: class for managing handles
    private volatile ClockGenerator.Handle cycleHandle;
    private volatile ClockGenerator.Handle delayHandle;
    private volatile ClockGenerator.Handle soundHandle;

    private KeyPort keyPort = new KeyPort() {
        @Override
        public void updateKeyState(short state) {
            clock.schedule(() -> cpu.getRegisters().getKeyState().set(state));
        }

        @Override
        public void keyPressed(byte key) {
            clock.schedule(() -> cpu.getRegisters().getKeyValue().set(key));
        }

        @Override
        public void reset() {
            clock.schedule(Board.this::reset);
        }
    };

    private StoragePort storagePort = new StoragePort() {
        //TODO: the board should request data from storage device, not the other way around
        @Override
        public void load(byte[] data) {
            SplittableMemory programMemory = (SplittableMemory) program; //TODO: add check
            programMemory.setStrict(false); //disable RO mode
            programMemory.setBytes(ushort(0x0), data, data.length);
            programMemory.setSplit(data.length);
            programMemory.setStrict(config::isEnforceMemoryRoRwState);
        }

        @Override
        public void setStoreCallback(Consumer<byte[]> callback) {
            throw new UnsupportedOperationException("unimplemented");
        }
    };

    private byte[] displayBuffer = new byte[MemoryModule.DISPLAY_IO_SIZE];
    private BiConsumer<Integer, byte[]> displayReceiver;

    private Consumer<Boolean> audioReceiver;

    @Inject
    public Board(
            final BoardConfig config,
            @Named(PROGRAM) final Memory program,
            @Named(INTERPRETER_ROM) final Memory interpreterRom,
            @Named(MMU) final Memory mmu,
            final ClockGenerator clock,
            final Cpu cpu
    ) {
        this.config = config;

        this.program = program;
        this.interpreterRom = interpreterRom;
        this.mmu = mmu;

        this.clock = clock;
        this.cpu = cpu;
    }

    public void powerOn() {
        // https://computer.howstuffworks.com/pc3.htm
        // power on self test and beep
        // hardware specs printed: cpu Hz, memory size, storage connected, logo?
        // check the program in storage, if ok load it
        // call the program

        init();
        runOnScheduler(Integer.MAX_VALUE);
    }

    public void powerOff(boolean force) {
        cycleHandle.cancel(force);
        delayHandle.cancel(force);
        soundHandle.cancel(force);

        reset();
    }

    public void init() {
        LOG.traceEntry();

        //TODO: load the font from file or integrate into bigger rom
        short fontAddress = INTERPRETER_ROM_START;
        byte[] font = new Loader().loadFont();
        mmu.setBytes(fontAddress, font, font.length);

        cpu.initialize();

        final Registers registers = cpu.getRegisters();

        registers.getFontSegment().set(fontAddress);

        registers.getGraphicChange().setCallback(gc -> {
            int change = gc.getAsInt();

            if (change > 0) {
                mmu.getBytes(DISPLAY_IO_START, displayBuffer, displayBuffer.length);

                if (displayReceiver != null) {
                    displayReceiver.accept(change, displayBuffer);
                }

                gc.set(GC_IDLE);
            }
        });

        registers.getSoundOn().setCallback(so -> {
            audioReceiver.accept(so.getAsInt() == 1);
        });

        ReadOnlyMemory interpreter = (ReadOnlyMemory) interpreterRom; //TODO: add check
        interpreter.setReadOnly(config::isEnforceMemoryRoRwState);

        LOG.traceExit();
    }

    public void reset() {
        // https://en.wikipedia.org/wiki/Hardware_reset
        mmu.clear(); // TODO:  hard reset clears whole memory and reloads roms?
        cpu.reset();  //TODO: soft reset clears only display / registers
    }

    public void runOnScheduler(int maxCycles) {
        final boolean countCycles = maxCycles != Integer.MAX_VALUE;
        final AtomicInteger cycles = new AtomicInteger();

        // TODO: handle threading of handle references xD
        delayHandle = clock.schedule(cpu::delayTick, config.getDelayTimerFrequency());
        soundHandle = clock.schedule(cpu::soundTick, config.getSoundTimerFrequency());

        cycleHandle = clock.schedule(() -> {
            cpu.cycle();

            if (countCycles) { // bypass counting
                int currentCycles = cycles.incrementAndGet();

                if (currentCycles >= maxCycles && cycleHandle != null) {
                    LOG.warn("Reached maxCycles: {}", maxCycles);

                    cycleHandle.cancel(false);
                    cycleHandle = null;

                    delayHandle.cancel(false);
                    delayHandle = null;

                    soundHandle.cancel(false);
                    soundHandle = null;
                }
            }
        }, config.getCpuFrequency());
    }

    public AudioPort getAudioPort() {
        return consumer -> audioReceiver = consumer;
    }

    public DisplayPort getDisplayPort() {
        return receiver -> displayReceiver = receiver;
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
