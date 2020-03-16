package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.port.KeyPort;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class KeyPortImpl implements KeyPort {

    private final WordRegister input;

    private @Nullable Consumer<OutputPacket> receiver;
    private @Nullable Transmitter transmitter;

    public KeyPortImpl(WordRegister input) {
        this.input = input;
    }

    @Override
    public Consumer<InputPacket> connect(Consumer<OutputPacket> receiver) {
        requireNonNull(receiver);

        this.receiver = receiver;
        transmitter = new Transmitter(this);

        return transmitter;
    }

    public void attachToRegister() {
        //TODO:
    }

    void receive(InputPacket input) {
        //cpu.getRegisters().getInput().set(state)
    }

    @Override
    public void disconnect() {
        if (transmitter != null) {
            transmitter.disable();
        }

        transmitter = null;
        receiver = null;
    }

    static class Transmitter implements Consumer<InputPacket> {

        @Nullable
        KeyPortImpl keyPort;

        public Transmitter(KeyPortImpl keyPort) {
            this.keyPort = keyPort;
        }

        void disable() {
            keyPort = null;
        }

        @Override
        public void accept(InputPacket inputPacket) {
            //TODO: schedule this instead of direct call
            if (keyPort != null) {
                keyPort.receive(inputPacket);
            }
        }
    }
}
