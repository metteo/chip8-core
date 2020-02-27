package net.novaware.chip8.core;

import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Loader;
import net.novaware.chip8.core.memory.MemoryMap;
import net.novaware.chip8.core.memory.SplittableMemory;
import net.novaware.chip8.core.port.AudioPort;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.port.KeyPort;
import net.novaware.chip8.core.port.StoragePort;
import net.novaware.chip8.core.util.uml.Owns;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.novaware.chip8.core.cpu.register.Registers.GC_IDLE;

@Singleton
public class Board {

    private static final Logger LOG = LogManager.getLogger();

    private final BoardConfig config;

    @Owns
    private final MemoryMap memoryMap;

    @Owns
    private final Cpu cpu;

    private ClockGenerator clock;
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
        @Override
        public void load(byte[] data) {
            SplittableMemory programMemory = memoryMap.getProgram();
            programMemory.setBytes((short) 0x0, data, data.length);
            programMemory.setSplit(data.length);
            programMemory.setStrict(config::isEnforceMemoryRoRwState);
        }

        @Override
        public void setStoreCallback(Consumer<byte[]> callback) {
            throw new UnsupportedOperationException("unimplemented");
        }
    };

    private byte[] displayBuffer = new byte[MemoryMap.DISPLAY_IO_SIZE];
    private BiConsumer<Integer, byte[]> displayReceiver;

    private Consumer<Boolean> audioReceiver;

    @Inject
    public Board(final BoardConfig config, final MemoryMap memoryMap, final Cpu cpu) {
        this.config = config;
        this.memoryMap = memoryMap;
        this.cpu = cpu;

        clock = new ClockGeneratorJvmImpl("Board");
    }

    public void init() {
        LOG.traceEntry();

        //TODO: load the font from file or integrate into bigger rom
        byte[] font = new Loader().loadFont();
        memoryMap.getInterpreter().setBytes((short) 0x0, font, font.length);
        memoryMap.getInterpreter().setReadOnly(config::isEnforceMemoryRoRwState);

        cpu.initialize();

        final Registers registers = cpu.getRegisters();

        registers.getGraphicChange().setCallback(gc -> {
            int change = gc.getAsInt();

            if (change > 0) {
                memoryMap.getDisplayIo().getBytes((short) 0x0, displayBuffer, displayBuffer.length);

                if (displayReceiver != null) {
                    displayReceiver.accept(change, displayBuffer);
                }

                gc.set(GC_IDLE);
            }
        });

        registers.getSoundOn().setCallback(so -> {
            audioReceiver.accept(so.getAsInt() == 1);
        });

        LOG.traceExit();
    }

    public void reset() {
        // https://en.wikipedia.org/wiki/Hardware_reset
        memoryMap.clear(); // TODO:  hard reset clears whole memory and reloads roms?
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
