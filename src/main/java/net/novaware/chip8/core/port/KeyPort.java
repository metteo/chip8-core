package net.novaware.chip8.core.port;

import java.util.function.Consumer;

public interface KeyPort extends InputPort, OutputPort {

    enum Direction {
        UP,
        DOWN,
        ;
    }

    interface InputPacket {
        Direction getDirection();

        byte getKeyCode();
    }

    interface OutputPacket {
        /**
         * Allows the keyboard to display hints which keys are used by the app.
         * @param keyCode
         * @return
         */
        boolean isKeyActive(byte keyCode);
    }

    /**
     *
     * @param receiver
     * @return transmission endpoint
     */
    Consumer<InputPacket> connect(Consumer<OutputPacket> receiver);

    void disconnect();
}
