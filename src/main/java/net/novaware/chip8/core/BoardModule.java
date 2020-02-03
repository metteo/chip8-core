package net.novaware.chip8.core;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryMap;

import javax.inject.Named;

@Module
public class BoardModule {

    @Provides
    @Named("cpu")
    static Memory provideCpuMemory(MemoryMap memoryMap) {
        return memoryMap.getCpuMemory();
    }

    @Provides
    @Named("variables")
    static ByteRegister[] provideVariables(Registers registers) {
        return registers.getVariables();
    }
}
