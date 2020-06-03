package net.novaware.chip8.core.bus;

import net.novaware.chip8.core.memory.Memory;
import org.checkerframework.checker.signedness.qual.Unsigned;

//TODO: optimize mapped memory access (specify method caches entry)
public class MemoryBus implements Bus {

    private final Memory memory;

    private @Unsigned short currentAddress;

    public MemoryBus(Memory memory) {
        this.memory = memory;
    }

    @Override
    public void specify(@Unsigned short address) {
        //TODO: assert within range?
        currentAddress = address;
    }

    @Override
    public byte readByte() {
        return memory.getByte(currentAddress);
    }

    @Override
    public void writeByte(@Unsigned byte data) {
        memory.setByte(currentAddress, data);
    }
}
