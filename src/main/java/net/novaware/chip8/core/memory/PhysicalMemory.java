package net.novaware.chip8.core.memory;

import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.util.AssertUtil.assertArgument;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * byte[] memory implementation
 */
public class PhysicalMemory extends AbstractMemory implements Memory {

    private final byte[] array;
    private final int size;

    public PhysicalMemory(final String name, final int size) {
        super(name);

        this.array = new byte[size];
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; ++i) {
            array[i] = 0;
        }
    }

    private int getArrayIndex(short address) {
        int arrayIndex = uint(address);

        assertArgument(arrayIndex < size, "address is outside memory limits");

        return arrayIndex;
    }

    @Override
    public byte getByte(short address) {
        return array[getArrayIndex(address)];
    }

    @Override
    public void setByte(short address, byte value) {
        array[getArrayIndex(address)] = value;
    }

    @Override
    public short getWord(short address) {
        int arrayIndex1 = getArrayIndex(address);
        int arrayIndex2 = getArrayIndex(ushort(uint(address) + 1)); // checks address + 1

        byte instrHi = array[arrayIndex1];
        byte instrLo = array[arrayIndex2];

        int instrHiUint =  uint(instrHi);
        int instrLoUint = uint(instrLo);

        return ushort(instrHiUint << 8 | instrLoUint);
    }

    @Override
    public void setWord(short address, short value) {
        int arrayIndex1 = getArrayIndex(address);
        int arrayIndex2 = getArrayIndex(ushort(uint(address) + 1)); // checks address + 1

        byte instrHi = ubyte((value & 0xFF00) >>> 8);
        byte instrLo = ubyte(value & 0x00FF);

        array[arrayIndex1] = instrHi;
        array[arrayIndex2] = instrLo;
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        requireNonNull(destination, "destination must not be null");
        assertArgument(length >= 0, "length must not be negative");

        final int arrayIndex = getArrayIndex(address);

        arraycopy(array, arrayIndex, destination, 0, length);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        requireNonNull(source, "source must not be null");
        assertArgument(length >= 0, "length must not be negative");

        final int arrayIndex = getArrayIndex(address);

        arraycopy(source, 0, array, arrayIndex, length);
    }
}
