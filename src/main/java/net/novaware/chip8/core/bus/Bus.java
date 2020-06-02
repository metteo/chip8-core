package net.novaware.chip8.core.bus;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Bus interface hiding memory details from CPU
 * TODO: create MemoryBus implementation, optimize mapped memory access (specify method caches entry)
 */
public interface Bus {

    /**
     * Specify memory location for read or write.
     * @param address
     *
     * @see <a href="https://en.wikipedia.org/wiki/Bus_(computing)#Address_bus">Address Bus</a>
     */
    void specify(final @Unsigned short address);

    /**
     * Read byte from memory under address specified using {@link #specify(short)}
     * @return
     */
    byte readByte();

    /**
     * Write byte into memory under address specified using {@link #specify(short)}
     * @return
     */
    void writeByte(final @Unsigned byte data);

    //TODO: add methods for word, byte[]


}
