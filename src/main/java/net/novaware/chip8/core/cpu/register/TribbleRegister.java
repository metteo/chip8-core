package net.novaware.chip8.core.cpu.register;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * 12 bit register type. Used to hold memory addresses: max 4096 bytes
 *
 * @see <a href="https://en.wikipedia.org/wiki/12-bit">Tribble - Wikipedia</a>
 */
public class TribbleRegister extends Register<TribbleRegister> {

    private static final Logger LOG = LogManager.getLogger();

    public static final int TRIBBLE_MASK = 0xFFF;
    private short data;

    public TribbleRegister(String name) {
        super(name);
    }

    public short get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(final short data) {
        int udata = uint(data);

        if (udata > TRIBBLE_MASK) {
            short truncated = ushort(udata & TRIBBLE_MASK);
            LOG.warn(() -> "Attempting to set " + getName() + " to " + toHexString(data) +
                    ", truncating to " + toHexString(truncated));
            this.data = truncated;
        } else {
            this.data = data;
        }

        pubSub.publish();
    }

    public void set(int data) {
        set(ushort(data));
    }

    public void increment(int amount) {
        set(uint(data) + amount);
    }
}
