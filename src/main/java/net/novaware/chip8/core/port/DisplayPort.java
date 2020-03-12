package net.novaware.chip8.core.port;

import java.util.function.Consumer;

public interface DisplayPort extends OutputPort {

    enum Type {
        PRIMARY,
        SECONDARY,
        ;
    }

    /**
     * @see <a href="https://chip8.fandom.com/wiki/Flicker">Chip8 Flicker</a>
     */
    enum Mode {

        /**
         * Every draw instruction is reflected on the screen immediately.
         * Causes flicker
         */
        DIRECT,

        /**
         * Detect when the app switches from drawing to erasing and trigger refresh
         * Clock helps in cases when drawing is not followed by erasing for longer period.
         */
        FALLING_EDGE,

        /**
         * Merge 2 (or more) frames (current + n previous) using OR to show deleted items.
         */
        MERGE_FRAME,

        /**
         * Alter app code on the fly to insert explicit refresh at the end of the screen loop.
         */
        EXPLICIT;
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

    /**
     * Connects the consuming device to the port. Consumer should not hold on to the Packet (Flyweight)
     * @param receiver
     */
    void connect(Consumer<Packet> receiver);

    //TODO: add consumer of fps and draw instrution calls to show indicator that something is happening in the back buffer

    Mode getMode();

    void setMode(Mode mode);

    /**
     * Disconnect any previously connected receiver.
     */
    void disconnect();
}
