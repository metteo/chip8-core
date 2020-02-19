package net.novaware.chip8.core.memory;

/**
 * Stores data + instructions
 */
public interface Memory {

    String getName();

    int getSize();

    byte getByte(short address);

    void setByte(short address, byte value);

    short getWord(short address);

    void setWord(short address, short value);

    void getBytes(short address, byte[] destination, int length);

    void setBytes(short address, byte[] source, int length);
}
