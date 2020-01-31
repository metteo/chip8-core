package net.novaware.chip8.core.cpu.register;

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
        return Byte.toUnsignedInt(data);
    }

    public void set(byte data) {
        this.data = data;

        fireCallback();
    }

    public void set(short data) {
        set((byte) data);
    }

    public void set(int data) {
        set((byte) data);
    }
}
