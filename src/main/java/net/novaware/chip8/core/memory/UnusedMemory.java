package net.novaware.chip8.core.memory;

public class UnusedMemory implements Memory {

    private final int size;

    public UnusedMemory(int size) {
        this.size = size;
    }

    @Override
    public String getName() {
        return "Unused";
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public byte getByte(short address) {
        throw new UnsupportedOperationException(getName() + " memory can not be accessed.");
    }

    @Override
    public void setByte(short address, byte value) {
        throw new UnsupportedOperationException(getName() + " memory can not be accessed.");
    }

    @Override
    public short getWord(short address) {
        throw new UnsupportedOperationException(getName() + " memory can not be accessed.");
    }

    @Override
    public void setWord(short address, short value) {
        throw new UnsupportedOperationException(getName() + " memory can not be accessed.");
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        throw new UnsupportedOperationException(getName() + " memory can not be accessed.");
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        throw new UnsupportedOperationException(getName() + " memory can not be accessed.");
    }
}
