package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

import static java.lang.Short.toUnsignedInt;

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
        memory.setWord(registers.getStackPointer().get(), registers.getProgramCounter().get());
        registers.getProgramCounter().set(address);
    }

    /* package */ void returnFromSubroutine() {
        final short address = memory.getWord(registers.getStackPointer().get());
        registers.getStackPointer().increment(-2);
        registers.getProgramCounter().set(address);
    }

    /* package */ void jump(final short address) {
        registers.getProgramCounter().set(address);
    }

    /* package */ void jump(final short address, final short offset) {
        final int offsetValue = registers.getVariable(offset).getAsInt();
        int newPc = toUnsignedInt(address);

        newPc += offsetValue;

        registers.getProgramCounter().set(newPc);

        registers.getProgramCounter().set(address + offsetValue); //TODO: may overflow
    }
}
