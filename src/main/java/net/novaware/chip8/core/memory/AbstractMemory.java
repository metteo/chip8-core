package net.novaware.chip8.core.memory;

public abstract class AbstractMemory implements Memory {

    protected String name;

    protected AbstractMemory(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
