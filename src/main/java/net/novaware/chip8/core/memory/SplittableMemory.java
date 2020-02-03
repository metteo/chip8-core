package net.novaware.chip8.core.memory;

import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * Splits Memory into ROM and RAM region
 */
public class SplittableMemory implements Memory {

    private static final boolean LOG = false;

    private final Memory memory;

    private int split = 0; //start with whole memory being RW

    protected SplittableMemory(Memory memory) {
        this.memory = memory;
    }

    /**
     * Sets the split point.
     * 0     ... split - 1            = ROM
     * split ... memory.getSize() - 1 = RAM
     * @param split
     */
    public void setSplit(int split) {
        System.out.println("<0x0000, " + toHexString(ushort(split)) + ") " + getName() + " ROM");
        System.out.println("<" + toHexString(ushort(split)) + ", " + toHexString(ushort(getSize())) + ") " + getName() +" RAM");
        this.split = split;
    }

    @Override
    public String getName() {
        return memory.getName();
    }

    @Override
    public int getSize() {
        return memory.getSize();
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        if (LOG) System.out.println(memory.getName() + " " + getSubName(address) + " getBytes " + toHexString(address)); //TODO: replace with logging

        memory.getBytes(address, destination, length);
    }

    @Override
    public byte getByte(short address) {
        if (LOG) System.out.println(memory.getName() + " " + getSubName(address) + " getByte " + toHexString(address)); //TODO: replace with logging

        return memory.getByte(address);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        if (isRom(address)) {
            throw new IllegalArgumentException("can not write in ROM"); //TODO: better exception type?
        }

        if (LOG) System.out.println(memory.getName() + " RAM setBytes " + toHexString(address)); //TODO: replace with logging

        memory.setBytes(address, source, length);
    }

    @Override
    public void setByte(short address, byte value) {
        if (isRom(address)) {
            throw new IllegalArgumentException("can not write in ROM"); //TODO: better exception type?
        }

        if (LOG) System.out.println(memory.getName() + " RAM setByte " + toHexString(address)); //TODO: replace with logging

        memory.setByte(address, value);
    }

    private boolean isRom(short address) {
        return !isRam(address);
    }

    private boolean isRam(short address) {
        return uint(address) >= split;
    }

    @Override
    public short getWord(short address) {
        if (LOG) System.out.println(memory.getName() + " " + getSubName(address) + " getWord " + toHexString(address)); //TODO: replace with logging

        return memory.getWord(address);
    }

    private String getSubName(short address) {
        return isRom(address) ? "ROM" : "RAM"; //TODO: cover case when read crosses the boundary
    }

    @Override
    public void setWord(short address, short word) {
        if (isRom(address)) {
            throw new IllegalArgumentException("can not write in ROM"); //TODO: better exception type?
        }

        if (LOG) System.out.println(memory.getName() + " RAM setWord " + toHexString(address)); //TODO: replace with logging

        memory.setWord(address, word);
    }
}
