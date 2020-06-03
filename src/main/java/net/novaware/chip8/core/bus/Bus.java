package net.novaware.chip8.core.bus;

import org.checkerframework.checker.signedness.qual.Unsigned;

/**
 * Bus interface hiding memory details from CPU
 *
 * https://stackoverflow.com/questions/8134545/difference-between-memory-bus-and-address-bus
 * http://www-mdp.eng.cam.ac.uk/web/library/enginfo/mdp_micro/lecture1/lecture1-3-1.html
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
