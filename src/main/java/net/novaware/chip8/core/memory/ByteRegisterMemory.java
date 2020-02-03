package net.novaware.chip8.core.memory;

import net.novaware.chip8.core.cpu.register.ByteRegister;

import java.util.Arrays;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * Uses Byte Registers as a backend
 */
public class ByteRegisterMemory extends AbstractMemory {

    private ByteRegister[] registers;

    protected ByteRegisterMemory(final String name, final ByteRegister[] registers) {
        super(name);

        //defensive copy, won't affect cpu loop
        this.registers = Arrays.copyOf(registers, registers.length, ByteRegister[].class);
    }

    private int getArrayIndex(short address) {
        int arrayIndex = uint(address);

        if (arrayIndex >= registers.length) {
            throw new IllegalArgumentException("register memory access outside limits");
        }

        return arrayIndex;
    }

    @Override
    public int getSize() {
        return registers.length;
    }

    @Override
    public byte getByte(short address) {
        return registers[getArrayIndex(address)].get();
    }

    @Override
    public short getWord(short address) {
        final int indexHi = getArrayIndex(address);
        final int indexLo = getArrayIndex(ushort(uint(address) + 1));


        final int hi = registers[indexHi].getAsInt();
        final int lo = registers[indexLo].getAsInt();

        return ushort(hi << 8 | lo);
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }

    @Override
    public void setByte(short address, byte value) {
        registers[getArrayIndex(address)].set(value);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }

    @Override
    public void setWord(short address, short word) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }
}
