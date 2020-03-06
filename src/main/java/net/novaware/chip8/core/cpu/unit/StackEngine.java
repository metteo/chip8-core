package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Uses;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.cpu.register.RegisterFile.getVariable;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

@BoardScope
public class StackEngine {

    @Uses
    private final TribbleRegister stackPointer;

    @Uses
    private final TribbleRegister memoryAddress;

    @Uses
    private final TribbleRegister programCounter;

    @Uses
    private final ByteRegister[] variables;

    @Uses
    private final Memory memory;

    @Inject
    public StackEngine(
            @Named(STACK_POINTER) final TribbleRegister stackPointer,
            @Named(MEMORY_ADDRESS) final TribbleRegister memoryAddress,
            @Named(PROGRAM_COUNTER) final TribbleRegister programCounter,
            @Named(VARIABLES) final ByteRegister[] variables,
            @Named(MMU) final Memory memory
    ) {
        this.stackPointer = stackPointer;
        this.memoryAddress = memoryAddress;
        this.programCounter = programCounter;
        this.variables = variables;

        this.memory = memory;
    }

    /* package */ void call(final short address) {
        stackPointer.increment(2); // 2 byte address
        memory.setWord(stackPointer.get(), memoryAddress.get());
        programCounter.set(address);
    }

    /* package */ void returnFromSubroutine() {
        final short address = memory.getWord(stackPointer.get());
        stackPointer.increment(-2);

        programCounter.set(address);
        programCounter.increment(2);
    }

    /* package */ void jump(final short address) {
        programCounter.set(address);
    }

    /* package */ void jump(final short address, final short offset) {
        final int offsetValue = getVariable(variables, offset).getAsInt();
        int newPc = uint(address);

        newPc = newPc + offsetValue; //TODO: may overflow

        jump(ushort(newPc));
    }
}
