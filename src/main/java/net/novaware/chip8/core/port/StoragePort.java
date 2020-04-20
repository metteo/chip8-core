package net.novaware.chip8.core.port;

import java.util.function.Supplier;

public interface StoragePort extends InputPort {

    interface Packet {

        int getSize();

        byte getByte(short address);
    }

    void connect(Supplier<Packet> source);

    /**
     * Disconnect any previously connected data source.
     */
    void disconnect();
}
