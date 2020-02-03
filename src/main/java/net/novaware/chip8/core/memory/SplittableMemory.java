package net.novaware.chip8.core.memory;

/**
 * Allows adding soft barriers to split memory into read only and rw regions
 * TODO: implement as decorator
 */
public abstract class SplittableMemory extends AbstractMemory {

    protected SplittableMemory(String name) {
        super(name);
    }
}
