package net.novaware.chip8.core.port;

import java.util.function.Consumer;

public interface StoragePort extends InputPort, OutputPort {

    //TODO: consider that the computer fetches data from tape instead of tape sending the data
    // so maybe use here attach(Producer<byte[]> producer)? and call it from the board
    void load(byte[] data);

    void setStoreCallback(Consumer<byte[]> callback);
}
