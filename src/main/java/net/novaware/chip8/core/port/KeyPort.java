package net.novaware.chip8.core.port;

import java.util.function.Consumer;

public interface KeyPort extends InputPort {

    enum Direction {
        UP,
        DOWN,
        ;
    }

    interface Packet {
        Direction getDirection();

        byte getKeyCode();
    }

    @Deprecated(forRemoval = true)
    void updateKeyState(final short state);

    Consumer<Packet> connect();

    void disconnect();
}
