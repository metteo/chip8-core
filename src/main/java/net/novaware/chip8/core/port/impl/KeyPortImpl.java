package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.port.KeyPort;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

public class KeyPortImpl implements KeyPort {

    private final ByteRegister input;

    private @Nullable Consumer<Packet> sender;

    public KeyPortImpl(ByteRegister input) {
        this.input = input;
    }

    @Override
    public void updateKeyState(short state) {

    }

    @Override
    public Consumer<Packet> connect() {
        //TODO: make sure we switch to proper thread in the consumer
        return p -> {};
    }

    @Override
    public void disconnect() {

    }
}
