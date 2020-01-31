package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Special unit attached to a timer register which decreases it / makes the sound
 */
public class Timer {

    //TODO: abstract this away so it can be replaced for other platforms
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> future;

    private Consumer<Boolean> buzzer;
    boolean buzzing = false; //TODO: buzzing should start when ST > 1 and end when ST < 1 (sounds shorter than 1 don't take effect)

    private ByteRegister register;

    public Timer(ByteRegister register, Consumer<Boolean> buzzer) {
        this.register = register;
        this.buzzer = buzzer;

        executor = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Chip8-Timer-" + register.getName()));

        register.setCallback(r -> {
            int value = register.getAsInt();

            if (value > 0) {
                start();
            }

            if (value < 1) {
                stop();
            }

            //TODO: rename it to callback, it can be sound or light
            if (this.buzzer != null && !buzzing && value > 1) {
                buzzing = true;
                this.buzzer.accept(buzzing); //TODO: delegate to audio component
            }

            if (this.buzzer != null && buzzing && value < 1) {
                buzzing = false;
                this.buzzer.accept(buzzing);
            }
        });
    }

    private void start() { //TODO: write integration test
        if (future != null) {
            return;
        }
        // 60Hz == 60 times a second == 1000ms / 60 == 16.(6) miliseconds
        future = executor.scheduleAtFixedRate(this::maybeDecrementValue, 16, 16, TimeUnit.MILLISECONDS);
    }

    private void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }

    /* package */ void maybeDecrementValue() {
        int intValue = register.getAsInt();

        if (intValue > 0) {
            register.set(intValue - 1); //FIXME: race condition!!!!!!!! cpu on main, timer on separate executor
        }
    }
}
