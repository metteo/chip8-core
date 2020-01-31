package net.novaware.chip8.core;

import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.cpu.unit.Timer;
import net.novaware.chip8.core.memory.MemoryMap;
import net.novaware.chip8.core.port.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Singleton
public class Board {

    private final MemoryMap memoryMap;

    private Cpu cpu;

    //private Clock clock;

    //private KeyPort keys;

    //private StoragePort storage;

    //private DisplayPort display;
    private byte[] displayBuffer = new byte[MemoryMap.DISPLAY_IO_SIZE];
    private BiConsumer<Boolean, byte[]> displayReceiver;

    private AudioPort audio;
    private Consumer<Boolean> audioReceiver;

    @Inject
    public Board(final MemoryMap memoryMap, final Cpu cpu) {
        this.memoryMap = memoryMap;
        this.cpu = cpu;
    }

    public void init() {

        byte[] font = new Loader().loadFont();
        memoryMap.getInterpreter().setBytes((short) 0x0, font, font.length);

        cpu.initialize();

        final Registers registers = cpu.getRegisters();

        Timer delay = new Timer(registers.getDelay(), null);
        Timer sound = new Timer(registers.getSound(), buzz -> {if (audioReceiver != null) audioReceiver.accept(buzz);});
    }

    public void reset() {
    }

    public void run(int maxCycles) throws InterruptedException {

        int cycle = 0;

        while (cycle < maxCycles) {

            cpu.cycle();

            if (cpu.getRegisters().redraw) {
                memoryMap.getDisplayIo().getBytes((short) 0x0, displayBuffer, displayBuffer.length);

                if (displayReceiver != null) {
                    boolean erasing = cpu.getRegisters().getStatus().getAsInt() > 0;
                    displayReceiver.accept(erasing, displayBuffer);
                }

                cpu.getRegisters().redraw = false;
            }

            sleepNanos(640_000); // 1000Hz, original used 500 Hz~ == 2ms but with 1ms it's much smoother
//            sleepNanos(2_200_000);

            //for pong 1ms base is too fast, make configurable using Hz

            ++cycle;
        }

        System.err.println("Reached maxCycles: " + maxCycles);
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
        if (diff > 50_000) System.out.println("sleepNanos not that good");
        //System.out.println(count + "    "); //TODO: happens 1k times for sleep of 640 uS
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
                memoryMap.getProgram().setBytes((short) 0x0, data, data.length);
            }

            @Override
            public void setStoreCallback(Consumer<byte[]> callback) {
                throw new UnsupportedOperationException("unimplemented");
            }
        };
    }
}
