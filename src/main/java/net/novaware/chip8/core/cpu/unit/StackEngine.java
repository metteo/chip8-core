package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.TribbleRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryModule;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Used;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterFile.getVariable;
import static net.novaware.chip8.core.cpu.register.RegisterModule.*;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

@BoardScope
public class StackEngine implements Unit {

    @Used
    private final TribbleRegister stackSegment;

    @Used
    private final TribbleRegister stackPointer;

    @Used
    private final TribbleRegister memoryAddress;

    @Used
    private final TribbleRegister programCounter;

    @Used
    private final ByteRegister[] variables;

    @Used
    private final Memory memory;

    @Inject
    public StackEngine(
            @Named(STACK_SEGMENT) final TribbleRegister stackSegment,
            @Named(STACK_POINTER) final TribbleRegister stackPointer,
            @Named(MEMORY_ADDRESS) final TribbleRegister memoryAddress,
            @Named(PROGRAM_COUNTER) final TribbleRegister programCounter,
            @Named(VARIABLES) final ByteRegister[] variables,
            @Named(MMU) final Memory memory
    ) {
        this.stackSegment = stackSegment;
        this.stackPointer = stackPointer;
        this.memoryAddress = memoryAddress;
        this.programCounter = programCounter;
        this.variables = variables;

        this.memory = memory;
    }

    @Override
    public void initialize() {
        setupRegisters();
    }

    @Override
    public void reset() {
        setupRegisters();
    }

    private void setupRegisters() {
        stackPointer.set(getStackBottom());
    }

    private short getStackBottom() {
        return ushort(stackSegment.getAsInt() + MemoryModule.STACK_SIZE);
    }

    /* package */ void call(final short address) {
        if (stackSegment.get() == stackPointer.get()) {
            throw new IllegalStateException("Stack overflow at " + toHexString(memoryAddress.get()));
        }

        stackPointer.increment(-2); // 2 byte address
        memory.setWord(stackPointer.get(), memoryAddress.get());
        programCounter.set(address);
    }

    /* package */ void returnFromSubroutine() {
        final short sp = stackPointer.get();

        if (sp == getStackBottom()) {
            throw new IllegalStateException("Stack underflow at " + toHexString(memoryAddress.get()));
        }

        final short address = memory.getWord(sp);
        stackPointer.increment(2);

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
