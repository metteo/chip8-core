package net.novaware.chip8.core.memory;

public abstract class AdapterMemory extends AbstractMemory implements Memory {

    public AdapterMemory(String name) {
        super(name);
    }

    @Override
    public byte getByte(short address) {
        throw new UnsupportedOperationException(getName() + "'s is not supported.");
    }

    @Override
    public void setByte(short address, byte value) {
        throw new UnsupportedOperationException(getName() + "'s is not supported.");
    }

    @Override
    public short getWord(short address) {
        throw new UnsupportedOperationException(getName() + "'s is not supported.");
    }

    @Override
    public void setWord(short address, short value) {
        throw new UnsupportedOperationException(getName() + "'s is not supported.");
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        throw new UnsupportedOperationException(getName() + "'s is not supported.");
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        throw new UnsupportedOperationException(getName() + "'s is not supported.");
    }
}
