package net.novaware.chip8.core.port;

import java.util.function.Consumer;

public interface AudioPort extends OutputPort {

    interface Packet {
        boolean isSoundOn();
    }

    void connect(Consumer<Packet> receiver);

    void disconnect();
}
