package net.novaware.chip8.core.memory;

//TODO: use it for stack space
public abstract class TribbleRegisterMemory extends AbstractMemory implements Memory {

    protected TribbleRegisterMemory(String name) {
        super(name);
    }

    //only 3 least significant bits will mean address per word, msb will be 0
    //24 levels per 48 bytes
}
