package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.port.KeyPort;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class KeyPortImpl implements KeyPort {

    private static final Logger LOG = LogManager.getLogger();

    @Used
    private final WordRegister input;

    @Used
    private final WordRegister inputCheck;

    @Owned
    private @Nullable Consumer<OutputPacket> receiver;

    @Owned
    private @Nullable Transmitter transmitter;

    @Owned
    private int inputsChecked = 0;

    @Owned
    private OutputPacket outputPacket = keyCode -> {
        int kc = uint(keyCode);
        int keyMask = 1 << kc;

        return (keyMask & inputsChecked) > 0;
    };

    public KeyPortImpl(final WordRegister input, final WordRegister inputCheck) {
        this.input = input;
        this.inputCheck = inputCheck;
    }

    /**
     * @param receiver output receiver
     * @return transmitter of input
     */
    @Override
    public Consumer<InputPacket> connect(Consumer<OutputPacket> receiver) {
        requireNonNull(receiver);

        this.receiver = receiver;
        transmitter = new Transmitter(this);

        return transmitter;
    }

    public void attachToRegister() {
        inputCheck.subscribe(ic -> onInputCheck());
    }

    private void onInputCheck() {
        final int ic = inputCheck.getAsInt();

        int inputMask = 1 << ic;

        boolean alreadyRegistered = (inputsChecked & inputMask) > 0;
        if (alreadyRegistered) {
            return; //don't trigger when no change
        }

        inputsChecked = inputsChecked | inputMask;

        maybeCallReceiver();
    }

    private void maybeCallReceiver() {
        if (receiver != null) {
            receiver.accept(outputPacket);
        }
    }

    void receive(InputPacket input) {
        requireNonNull(input, "input packet must not be null");

        final Direction direction = input.getDirection();
        requireNonNull(direction, "direction must not be null");

        final byte keyCode = input.getKeyCode();

        if (keyCode < 0x0 || keyCode > 0xF) {
            LOG.warn("Ignoring invalid key code in input packet: " + toHexString(keyCode));
            return;
        }

        final int currentInput = this.input.getAsInt();

        int inputMask = 1 << keyCode;

        if (direction == Direction.DOWN) {
            boolean alreadyRegistered = (currentInput & inputMask) > 0;

            if (!alreadyRegistered) {
                final short newInput = ushort(currentInput | inputMask);

                this.input.set(newInput);
            }

        } else if (direction == Direction.UP) {
            boolean alreadyUnregistered = (currentInput & inputMask) == 0;

            if (!alreadyUnregistered) {
                final short newInput = ushort(~inputMask & currentInput);

                this.input.set(newInput);
            }
        }
    }

    @Override
    public void disconnect() {
        if (transmitter != null) {
            transmitter.disable();
        }

        transmitter = null;
        receiver = null;
    }

    static class Transmitter implements Consumer<InputPacket> {

        @Nullable
        KeyPortImpl keyPort;

        public Transmitter(KeyPortImpl keyPort) {
            this.keyPort = keyPort;
        }

        void disable() {
            keyPort = null;
        }

        @Override
        public void accept(InputPacket inputPacket) {
            //FIXME: schedule this instead of direct call
            if (keyPort != null) {
                keyPort.receive(inputPacket);
            }
        }
    }
}
