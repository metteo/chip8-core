package net.novaware.chip8.core;

import dagger.Module;
import dagger.Provides;
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
}
