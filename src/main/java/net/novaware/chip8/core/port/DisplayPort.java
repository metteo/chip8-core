package net.novaware.chip8.core.port;

import java.util.function.BiConsumer;

public interface DisplayPort extends OutputPort {

    void attach(BiConsumer<Integer, byte[]> receiver);

}
