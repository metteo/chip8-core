package net.novaware.chip8.core.port;

import net.novaware.chip8.core.cpu.register.Registers;

import java.util.function.BiConsumer;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

public interface DisplayPort extends OutputPort {

    //TODO: replace with enum or hide inside Gpu (heuristics should be part of the board)
    int GC_IDLE = uint(Registers.GC_IDLE);
    int GC_ERASE = uint(Registers.GC_ERASE);
    int GC_NOOP = uint(Registers.GC_NOOP);
    int GC_DRAW = uint(Registers.GC_DRAW);
    int GC_MIX = uint(Registers.GC_MIX);

    void attach(BiConsumer<Integer, byte[]> receiver);

}
