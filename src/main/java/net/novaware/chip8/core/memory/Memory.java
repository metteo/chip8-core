package net.novaware.chip8.core.memory;

/**
 * Stores data + instructions
 */
public interface Memory {

    String getName();

    int getSize();

    void getBytes(short address, byte[] destination, int length);

    byte getByte(short address);

    void setBytes(short address, byte[] source, int length);

    void setByte(short address, byte value);

    short getWord(short address);

    void setWord(short address, short value);
}
