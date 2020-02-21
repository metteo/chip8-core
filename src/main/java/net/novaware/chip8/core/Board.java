package net.novaware.chip8.core;

import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.Timer;
import net.novaware.chip8.core.memory.MemoryMap;
import net.novaware.chip8.core.memory.SplittableMemory;
import net.novaware.chip8.core.port.*;
import net.novaware.chip8.core.util.uml.Owns;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.System.nanoTime;
import static net.novaware.chip8.core.cpu.register.Registers.GC_IDLE;
import static net.novaware.chip8.core.util.SleepUtil.sleepNanos;

@Singleton
public class Board {

    private static final Logger LOG = LogManager.getLogger();

    private final BoardConfig config;

    @Owns
    private final MemoryMap memoryMap;

    @Owns
    private final Cpu cpu;

    //private Clock clock;

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

        LOG.traceExit();
    }

    public void reset() {
    }

    public void run(int maxCycles) throws InterruptedException {

        final long sleepTime = (long) ((double) TimeUnit.SECONDS.toNanos(1) / config.getCpuFrequency());
        int cycle = 0;
        long lastSleepDiff = 0; // compensate for previous sleepiness

        while (cycle < maxCycles) {
            long cycleStart = nanoTime();

            cpu.cycle();

            long cycleTime = nanoTime() - cycleStart;

            lastSleepDiff = sleepNanos(sleepTime - cycleTime - lastSleepDiff);

            ++cycle;
        }

        LOG.warn("Reached maxCycles: {}", maxCycles);
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
}
