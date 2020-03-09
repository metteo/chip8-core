package net.novaware.chip8.core.memory;

import net.novaware.chip8.core.cpu.register.TribbleRegister;

import java.util.Arrays;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Uses Word Registers as a backend
 */
public class TribbleRegisterMemory extends AbstractMemory {

    private TribbleRegister[] registers;

    protected TribbleRegisterMemory(final String name, final TribbleRegister[] registers) {
        super(name);

        //defensive copy, won't affect cpu loop
        this.registers = Arrays.copyOf(registers, registers.length, TribbleRegister[].class);
    }

    private int getArrayIndex(short address) {
        int arrayIndex = uint(address) / 2;
        boolean crossRegister = uint(address) % 2 != 0;

        assertArgument(!crossRegister, "register memory access unaligned");
        assertArgument(arrayIndex < registers.length, "register memory access outside limits");

        return arrayIndex;
    }

    @Override
    public int getSize() {
        return registers.length * 2;
    }

    @Override
    public byte getByte(short address) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }

    @Override
    public void setByte(short address, byte value) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }

    @Override
    public short getWord(short address) {
        final int index = getArrayIndex(address);

        return registers[index].get();
    }

    @Override
    public void setWord(short address, short word) {
        final int index = getArrayIndex(address);

        registers[index].set(word);
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        throw new UnsupportedOperationException("not implemented"); //TODO: implement
    }
}
