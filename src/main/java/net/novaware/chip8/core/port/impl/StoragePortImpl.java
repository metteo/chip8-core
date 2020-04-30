package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.port.StoragePort;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public class StoragePortImpl implements StoragePort {

    private final StorageMemory storageMemory;

    private @Nullable Supplier<Packet> packetSupplier;

    public StoragePortImpl(StorageMemory storageMemory) {
        this.storageMemory = storageMemory;
    }

    @Override
    public void connect(Supplier<Packet> source) {
        assertArgument(source != null, "packet source can not be null");

        packetSupplier = source;
        Packet packet = packetSupplier.get();

        assertArgument(packet != null, "packet can not be null");

        storageMemory.setPacket(packet);
    }

    @Override
    public void disconnect() {
        packetSupplier = null;
        storageMemory.setPacket(null);
    }

    public @Nullable Supplier<Packet> getPacketSupplier() {
        return packetSupplier;
    }
}
