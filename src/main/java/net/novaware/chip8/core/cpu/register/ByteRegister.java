package net.novaware.chip8.core.cpu.register;

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * General purpose data register. Holds 8 bits of data.
 */
public class ByteRegister extends Register<ByteRegister> {

    private byte data;

    public ByteRegister(String name) {
        super(name);
    }

    public byte get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(byte data) {
        this.data = data;

        fireCallback();
    }

    //TODO: when setting value using bigger types, optionally log if data was truncated in the process
    public void set(short data) {
        set(ubyte(data));
    }

    public void set(int data) {
        set(ubyte(data));
    }
}
