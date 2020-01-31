package net.novaware.chip8.core.cpu.register;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * 12 bit register type. Used to hold memory addresses: max 4096 bytes
 *
 * @see <a href="https://en.wikipedia.org/wiki/12-bit">Tribble - Wikipedia</a>
 */
public class TribbleRegister extends Register<TribbleRegister> {

    public static final int TRIBBLE_MASK = 0xFFF;
    private short data;

    protected TribbleRegister(String name) {
        super(name);
    }

    public short get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(short data) {
        int udata = uint(data);

        if (udata > TRIBBLE_MASK) {
            System.err.println("Attempt to set " + getName() + " to " + data + ", truncating to 12 bits");
            data = ushort(udata & TRIBBLE_MASK);
        }

        this.data = data;

        fireCallback();
    }

    public void set(int data) {
        set(ushort(data));
    }

    public void increment(int amount) {
        set(uint(data) + amount);
    }
}
