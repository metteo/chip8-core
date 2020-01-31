package net.novaware.chip8.core.cpu.register;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * 16 bit register type. Used to hold 2 bytes or a single instruction.
 */
public class WordRegister extends Register<WordRegister> {

    private short data;

    public WordRegister(String name) {
        super(name);
    }

    public short get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(short data) {
        this.data = data;

        fireCallback();
    }

    public void set(int data) {
        set(ushort(data));
    }
}
