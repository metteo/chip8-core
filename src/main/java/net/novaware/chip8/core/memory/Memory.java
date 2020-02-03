package net.novaware.chip8.core.memory;

/**
 * Stores data + instructions
 */
public interface Memory {

    String getName();

    int getSize();

    void getBytes(short address, byte[] destination, int length); //TODO: length should be positive (add checks)

    byte getByte(short address); //TODO: address should be 0x0 - 0xFFF, add checks

    void setBytes(short address, byte[] source, int length);

    void setByte(short address, byte value);

    short getWord(short address);

    void setWord(short address, short value);
}
