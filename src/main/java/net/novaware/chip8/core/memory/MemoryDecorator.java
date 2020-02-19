package net.novaware.chip8.core.memory;

public abstract class MemoryDecorator implements Memory {

    protected Memory memory;

    protected MemoryDecorator(Memory memory) {
        this.memory = memory;
    }

    @Override
    public String getName() {
        return memory.getName();
    }

    @Override
    public int getSize() {
        return memory.getSize();
    }

    @Override
    public byte getByte(short address) {
        return memory.getByte(address);
    }

    @Override
    public void setByte(short address, byte value) {
        memory.setByte(address, value);
    }

    @Override
    public short getWord(short address) {
        return memory.getWord(address);
    }

    @Override
    public void setWord(short address, short value) {
        memory.setWord(address, value);
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        memory.getBytes(address, destination, length);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        memory.setBytes(address, source, length);
    }
}
