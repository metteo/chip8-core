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

    public ByteRegister(String name, boolean preventRecursivePublish) {
        super(name, preventRecursivePublish);
    }

    public byte get() {
        return data;
    }

    public int getAsInt() {
        return uint(data);
    }

    public void set(byte data) {
        this.data = data;

        pubSub.publish();
    }

    public void set(short data) {
        set(ubyte(data));
    }

    public void set(int data) {
        set(ubyte(data));
    }
}
