package net.novaware.chip8.core.port;

import net.novaware.chip8.core.cpu.register.RegisterFile;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

public interface DisplayPort extends OutputPort {

    enum Type {
        PRIMARY,
        SECONDARY,
        ;
    }

    interface Packet {

        /**
         * Usually 64
         */
        int getColumnCount();

        /**
         * Usually 32
         */
        int getRowCount();

        /**
         * @param column x coordinate
         * @param row y coordinate
         * @return true if pixel is on
         */
        boolean getPixel(int column, int row);
    }

    @Deprecated(forRemoval = true)
    int GC_IDLE = uint(RegisterFile.GC_IDLE);
    @Deprecated(forRemoval = true)
    int GC_ERASE = uint(RegisterFile.GC_ERASE);
    @Deprecated(forRemoval = true)
    int GC_NOOP = uint(RegisterFile.GC_NOOP);
    @Deprecated(forRemoval = true)
    int GC_DRAW = uint(RegisterFile.GC_DRAW);
    @Deprecated(forRemoval = true)
    int GC_MIX = uint(RegisterFile.GC_MIX);

    @Deprecated(forRemoval = true)
    void attach(BiConsumer<Integer, byte[]> receiver);

    /**
     * Connects the consuming device to the port. Consumer should not hold on to the Packet (Flyweight)
     * @param receiver
     */
    void connect(Consumer<Packet> receiver);

    /**
     * Disconnect any previously connected receiver.
     */
    void disconnect();
}
