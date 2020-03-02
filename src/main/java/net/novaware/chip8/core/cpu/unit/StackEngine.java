package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class StackEngine {

    // Accessible -------------------------------

    private final Registers registers;

    private final Memory memory;

    public StackEngine(Registers registers, Memory memory) {
        this.registers = registers;

        this.memory = memory;
    }

    /* package */ void call(final short address) {
        registers.getStackPointer().increment(2); // 2 byte address
        memory.setWord(registers.getStackPointer().get(), registers.getMemoryAddress().get());
        registers.getProgramCounter().set(address);
    }

    /* package */ void returnFromSubroutine() {
        final short address = memory.getWord(registers.getStackPointer().get());
        registers.getStackPointer().increment(-2);

        registers.getProgramCounter().set(address);
        registers.getProgramCounter().increment(2);
    }

    /* package */ void jump(final short address) {
        registers.getProgramCounter().set(address);
    }

    /* package */ void jump(final short address, final short offset) {
        final int offsetValue = registers.getVariable(offset).getAsInt();
        int newPc = uint(address);

        newPc = newPc + offsetValue; //TODO: may overflow

        jump(ushort(newPc));
    }
}
