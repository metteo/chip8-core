package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.port.AudioPort;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class AudioPortImpl implements AudioPort {

    private final ByteRegister soundOn;

    private @Nullable Consumer<Packet> receiver;

    private final Packet packet = new Packet() {
        @Override
        public boolean isSoundOn() {
            return soundOn.getAsInt() == 1;
        }
    };

    public AudioPortImpl(ByteRegister soundOn) {
        this.soundOn = soundOn;
    }

    public void attachToRegister() {
        soundOn.subscribe(so -> {
            maybeCallReceiver();
        });
    }

    @Override
    public void connect(Consumer<Packet> receiver) {
        requireNonNull(receiver, "receiver must not be null");

        this.receiver = receiver;
    }

    private void maybeCallReceiver() {
        if (receiver != null) {
            receiver.accept(packet);
        }
    }

    @Override
    public void disconnect() {
        receiver = null;
    }
}
