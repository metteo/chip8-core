package net.novaware.chip8.core;

import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.Timer;
import net.novaware.chip8.core.memory.MemoryMap;
import net.novaware.chip8.core.memory.SplittableMemory;
import net.novaware.chip8.core.port.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.novaware.chip8.core.cpu.register.Registers.GC_IDLE;

@Singleton
public class Board {

    private static final Logger LOG = LogManager.getLogger();

    private final BoardConfig config;

    private final MemoryMap memoryMap;

    private final Cpu cpu;

    //private Clock clock;

    //private KeyPort keys;

    //private StoragePort storage;

    //private DisplayPort display;
    private byte[] displayBuffer = new byte[MemoryMap.DISPLAY_IO_SIZE];
    private BiConsumer<Integer, byte[]> displayReceiver;

    private AudioPort audio;
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

        final long sleepTime = (long) (1_000_000_000d / config.getCpuFrequency());

        int cycle = 0;

        while (cycle < maxCycles) {

            cpu.cycle();

            sleepNanos(sleepTime);

            ++cycle;
        }

        LOG.warn("Reached maxCycles: {}", maxCycles);
    }

    private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);

    //TODO: use LockSupport.parkNanos() instead?
    public static void sleepNanos (long nanoDuration) throws InterruptedException {
        final long end = System.nanoTime() + nanoDuration;

        long timeLeft = nanoDuration;
        int count = 0;
        do {
            if (timeLeft > SLEEP_PRECISION)
                Thread.sleep (1);
            else
                Thread.sleep (0); // Thread.yield();

            timeLeft = end - System.nanoTime();

            if (Thread.interrupted())
                throw new InterruptedException();
            ++count;
        } while (timeLeft > 0);

        long diff = System.nanoTime() - end;
        if (diff > 50_000) LOG.warn("sleepNanos not that good");

        //TODO: happens 1k times for sleep of 640 uS
        if (count > 100) {
            LOG.debug("Sleep nanos tries to sleep {} times to wait for {}", count, nanoDuration);
        }
    }

    public AudioPort getAudioPort() {
        return consumer -> audioReceiver = consumer;
    }

    public DisplayPort getDisplayPort() {
        return receiver -> displayReceiver = receiver;
    }

    public KeyPort getKeyPort() {
        return new KeyPort() {
            @Override
            public void updateKeyState(short state) {
                cpu.getRegisters().getKeyState().set(state);
            }

            @Override
            public void keyPressed(byte key) {
                cpu.getRegisters().getKeyValue().set(key);
            }
        };
    }

    public StoragePort getStoragePort() {
        return new StoragePort() {
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
    }
}
