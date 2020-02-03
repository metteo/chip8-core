package net.novaware.chip8.core.memory;

/**
 * Allows blocking of writes
 * TODO: write missing tests
 */
public class ReadOnlyMemory implements Memory {

    private final Memory memory;

    private boolean readOnly = false;

    protected ReadOnlyMemory(Memory memory) {
        this.memory = memory;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
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
    public void getBytes(short address, byte[] destination, int length) {
        memory.getBytes(address, destination, length);
    }

    @Override
    public byte getByte(short address) {
        return memory.getByte(address);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        if (readOnly) {
            throw new IllegalArgumentException(getName() + " is in RO mode");
        }

        memory.setBytes(address, source, length);
    }

    @Override
    public void setByte(short address, byte value) {
        if (readOnly) {
            throw new IllegalArgumentException(getName() + " is in RO mode");
        }

        memory.setByte(address, value);
    }

    @Override
    public short getWord(short address) {
        return memory.getWord(address);
    }

    @Override
    public void setWord(short address, short word) {
        if (readOnly) {
            throw new IllegalArgumentException(getName() + " is in RO mode");
        }

        memory.setWord(address, word);
    }
}
