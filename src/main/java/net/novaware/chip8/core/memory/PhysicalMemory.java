package net.novaware.chip8.core.memory;

import static net.novaware.chip8.core.util.UnsignedUtil.*;

public class PhysicalMemory extends AbstractMemory implements Memory {

    private byte[] memory;

    /**
     *
     * @param size in bytes
     */
    public PhysicalMemory(final String name, final int size) {
        super(name);

        this.memory = new byte[size];
    }

    @Override
    public int getSize() {
        return memory.length;
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        final int arrayIndex = getArrayIndex(address);

        System.arraycopy(memory, arrayIndex, destination, 0, length);
    }

    @Override
    public byte getByte(short address) {
        return memory[getArrayIndex(address)];
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        final int arrayIndex = getArrayIndex(address);

        System.arraycopy(source, 0, memory, arrayIndex, length);
    }

    @Override
    public void setByte(short address, byte source) {
        memory[getArrayIndex(address)] = source;
    }

    @Override
    public short getWord(short address) {
        int arrayIndex = getArrayIndex(address);

        //FIXME getting / setting word at the last byte should be forbidden

        byte instrHi = memory[arrayIndex];
        byte instrLo = memory[arrayIndex + 1];

        int instrHiUint =  uint(instrHi);
        int instrLoUint = uint(instrLo);

        return ushort(instrHiUint << 8 | instrLoUint);
    }

    private int getArrayIndex(short address) {
        int arrayIndex = uint(address);

        assert arrayIndex < memory.length : "memory access outside limits";

        return arrayIndex;
    }

    @Override
    public void setWord(short address, short instruction) {
        int arrayIndex = getArrayIndex(address);

        byte instrHi = ubyte((instruction & 0xFF00) >>> 8);
        byte instrLo = ubyte(instruction & 0x00FF);

        memory[arrayIndex] = instrHi;
        memory[arrayIndex + 1] = instrLo;
    }
}
