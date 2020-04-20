package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.memory.AdapterMemory;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.port.StoragePort;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;

public class StorageMemory extends AdapterMemory implements Memory {

    private final int maxSize;
    private StoragePort.Packet packet;

    public StorageMemory(final String name, final int maxSize) {
        super(name);

        this.maxSize = maxSize;
    }

    @Override
    public int getSize() {
        return maxSize;
    }

    @Override
    public byte getByte(short address) {
        int index = uint(address);

        assertArgument(index < maxSize, "address outside of memory range");

        if (packet != null && index < packet.getSize()) {
            return packet.getByte(address);
        } else {
            return 0;
        }
    }

    public void setPacket(StoragePort.Packet packet) {
        this.packet = packet;
    }
}
