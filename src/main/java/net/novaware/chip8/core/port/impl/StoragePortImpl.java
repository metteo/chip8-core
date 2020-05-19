package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.port.StoragePort;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public class StoragePortImpl implements StoragePort {

    private final StorageMemory storageMemory;
    private final TribbleRegister storageRegister;

    private @Nullable Supplier<Packet> packetSupplier;

    public StoragePortImpl(StorageMemory storageMemory, TribbleRegister storageRegister) {
        this.storageMemory = storageMemory;
        this.storageRegister = storageRegister;
    }

    @Override
    public void connect(Supplier<Packet> source) {
        assertArgument(source != null, "packet source can not be null");

        packetSupplier = source;
        Packet packet = packetSupplier.get();

        assertArgument(packet != null, "packet can not be null");

        storageMemory.setPacket(packet);
        storageRegister.set(packet.getSize());
    }

    @Override
    public void disconnect() {
        packetSupplier = null;
        storageMemory.setPacket(null);
        storageRegister.set(0);
    }

    public @Nullable Supplier<Packet> getPacketSupplier() {
        return packetSupplier;
    }
}
