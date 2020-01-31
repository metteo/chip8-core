package net.novaware.chip8.core.port;

import java.util.function.Consumer;

public interface AudioPort extends OutputPort {

    void attach(Consumer<Boolean> consumer);
}
