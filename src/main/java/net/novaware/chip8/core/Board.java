package net.novaware.chip8.core;

import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.Timer;
import net.novaware.chip8.core.memory.Loader;
import net.novaware.chip8.core.memory.MemoryMap;
import net.novaware.chip8.core.memory.SplittableMemory;
import net.novaware.chip8.core.port.*;
import net.novaware.chip8.core.util.uml.Owns;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

    //private Clock clock;

    private ScheduledExecutorService executor;
    private volatile ScheduledFuture<?> future;

    private long lastNanoTime; // ns

    private KeyPort keyPort = new KeyPort() {
        @Override
        public void updateKeyState(short state) {
            cpu.getRegisters().getKeyState().set(state);
        }

        @Override
        public void keyPressed(byte key) {
            cpu.getRegisters().getKeyValue().set(key);
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
    }

    public void init() {
        LOG.traceEntry();

        //TODO: load the font from file or integrate into bigger rom
        byte[] font = new Loader().loadFont();
        memoryMap.getInterpreter().setBytes((short) 0x0, font, font.length);
        memoryMap.getInterpreter().setReadOnly(config::isEnforceMemoryRoRwState);

        cpu.initialize();

        final Registers registers = cpu.getRegisters();

        Timer delay = new Timer(registers.getDelay(), null, config.getDelayTimerFrequency());
        Timer sound = new Timer(
                registers.getSound(),
                buzz -> {
                    if (audioReceiver != null) audioReceiver.accept(buzz);
                },
                config.getSoundTimerFrequency()
        );

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

        executor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Chip8-Board-Clock"));

        LOG.traceExit();
    }

    public void reset() {
    }

    public void runOnScheduler(int maxCycles) {
        final long period = (long)((double)TimeUnit.SECONDS.toNanos(1) / config.getCpuFrequency());

        lastNanoTime = System.nanoTime();

        final AtomicInteger cycles = new AtomicInteger();

        future = executor.scheduleAtFixedRate(() -> {
            long currentNanoTime = System.nanoTime();
            final long actualPeriod = currentNanoTime - lastNanoTime;
            double error = (double) Math.abs(period - actualPeriod) / period * 100;

            lastNanoTime = currentNanoTime;

            //LOG.info("SES error: " + String.format("%3.2f", error) + " % for " + period + " ns");

            cpu.cycle();

            if (maxCycles != Integer.MAX_VALUE) { // bypass counting
                int currentCycles = cycles.incrementAndGet();

                if (currentCycles >= maxCycles && future != null) {
                    LOG.warn("Reached maxCycles: {}", maxCycles);

                    future.cancel(false);
                    future = null;
                }
            }
        }, period, period, TimeUnit.NANOSECONDS);
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
        return future != null;
    }
}
