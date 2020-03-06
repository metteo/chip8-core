package net.novaware.chip8.core.port;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface StoragePort extends InputPort, OutputPort {

    void attachSource(Supplier<byte[]> source);

    void attachDestination(Consumer<byte[]> callback);
}
