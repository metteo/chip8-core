package net.novaware.chip8.core;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.Cpu;

@Deprecated(forRemoval = true)
@Module
public class BoardModule {

    @Provides
    static Cpu.Config provideCpuConfig(BoardConfig boardConfig) {
        return new Cpu.Config() {
            @Override public boolean isLegacyShift() { return boardConfig.isLegacyShift(); }
            @Override public boolean isLegacyLoadStore() { return boardConfig.isLegacyLoadStore(); }
            @Override public boolean isLegacyAddressSum() { return boardConfig.isLegacyAddressSum(); }
        };
    }
}
