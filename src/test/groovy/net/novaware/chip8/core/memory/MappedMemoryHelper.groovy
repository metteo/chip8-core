package net.novaware.chip8.core.memory

import net.novaware.chip8.core.cpu.register.ByteRegister

import static net.novaware.chip8.core.memory.MemoryModule.*

class MappedMemoryHelper {

    static MappedMemory newMappedMemory(ByteRegister[] variables) {
        provideRam(
                provideBootloaderRom(),
                provideProgram(),
                provideStack(),
                provideBootloaderRam(),
                provideVariables(variables),
                provideDisplayIo()
        ) as MappedMemory
    }
}
