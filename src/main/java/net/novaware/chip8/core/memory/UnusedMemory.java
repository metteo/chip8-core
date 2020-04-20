package net.novaware.chip8.core.memory;

public class UnusedMemory extends AdapterMemory implements Memory {

    private final int size;

    public UnusedMemory(final int size) {
        super("Unused");
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }
}
